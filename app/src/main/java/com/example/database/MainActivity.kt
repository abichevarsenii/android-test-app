package com.example.database

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast
import java.io.IOException
import java.util.prefs.AbstractPreferences

const val USER_ID_KEY = "userId"
const val BODY_KEY = "body"
const val TITLE_KEY = "title"
const val MAX_ID_KEY = "max_id"
const val PREFERENCES_KEY = "preferences"
const val HAS_BD_KEY = "has_bd"

class MainActivity : AppCompatActivity() {
    lateinit var myRecyclerView: RecyclerView
    private var myItemsList: MutableList<MyItem> = mutableListOf()
    lateinit var scope: CoroutineScope
    private lateinit var binding: ActivityMainBinding
    private var numberPosts = 10
    private var maxId = 0
    private lateinit var myPreferences: SharedPreferences
    private var isDatabaseNew = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myRecyclerView = binding.myRecyclerView
        supportActionBar?.hide()
        scope = CoroutineScope(Dispatchers.Default)

        val buttonAdd: FloatingActionButton = binding.floatingActionButton
        buttonAdd.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra("list", myItemsList.toTypedArray())
            startActivity(intent)
        }



        myPreferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        maxId = myPreferences.getInt(MAX_ID_KEY, 0)
        isDatabaseNew = myPreferences.getBoolean(HAS_BD_KEY,true)



        binding.refreshDataButton.setOnClickListener {
            isDatabaseNew = true
            maxId = 0
            myItemsList.clear()
            buildRecyclerView(myItemsList)
            binding.progressBar.visibility = View.VISIBLE
            val editor: SharedPreferences.Editor = myPreferences.edit()
            editor.putInt(MAX_ID_KEY, 0)
            editor.putBoolean(HAS_BD_KEY,true)
            editor.apply()
            refreshData()
        }

        if (intent.hasExtra("list")) {
            unpackBundle()
            return
        }

        refreshData()
    }

    private fun refreshData(){
        scope.launch {
            if (!isDatabaseNew) {
                val result = async(SupervisorJob()) {
                    MyApp.instance.database.userDao().getAllPosts()
                }
                val await = result.await()
                if (await != null) {
                    myItemsList = MutableList(await.size) { MyItem(await[it]!!) }
                }
            } else {
                val resultDelete = async(SupervisorJob()) {
                    MyApp.instance.database.clearAllTables()
                }
                resultDelete.await()
                for (i in 1..numberPosts) {
                    try {

                        val result = async(SupervisorJob()) {
                            MyApp.instance.mRetrofit.create(JSONPlaceHolderApi::class.java)
                                .getPostWithID(i)
                        }
                        val await = result.await()

                        maxId = await.id
                        val editor: SharedPreferences.Editor = myPreferences.edit()
                        editor.putInt(MAX_ID_KEY, maxId)
                        editor.putBoolean(HAS_BD_KEY,false)
                        editor.apply()
                        addItem(await.userId, await.body, await.title)


                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            SendToast(resources.getString(R.string.error_message) + e.message)
                        }
                        return@launch
                    }
                }
            }
            withContext(Dispatchers.Main) {
                buildRecyclerView(myItemsList)
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val list = savedInstanceState.getParcelableArray("list")!!
        val list2 = mutableListOf<MyItem>()
        for (item in list) {
            list2.add(item as MyItem)
        }
        myItemsList = list2
        buildRecyclerView(myItemsList)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray("list", myItemsList.toTypedArray())
        super.onSaveInstanceState(outState)
    }

    private fun unpackBundle() {
        val list = intent.getParcelableArrayExtra("list")!!
        val list2 = mutableListOf<MyItem>()
        for (item in list) {
            list2.add(item as MyItem)
        }

        myItemsList = list2
        buildRecyclerView(myItemsList)


        if (intent.hasExtra(USER_ID_KEY)) {
            var userId = intent.getStringExtra(USER_ID_KEY)!!

            val body = intent.getStringExtra(BODY_KEY)!!
            val title = intent.getStringExtra(TITLE_KEY)!!

            intent.removeExtra(USER_ID_KEY)
            intent.removeExtra(BODY_KEY)
            intent.removeExtra(TITLE_KEY)

            if (userId == "") {
                userId = "0"
            }
            addItem(userId.toInt(), body, title)
        }
    }

    private fun addItem(userId: Int, body: String?, title: String?) {
        val answer = PostDAO()
        answer.title = title
        answer.body = body
        answer.userId = userId
        answer.id = maxId

        scope.launch {
            try {
                val result = async(SupervisorJob()) {
                    MyApp.instance.database.userDao().insertAll(answer)
                }
                result.await()
                withContext(Dispatchers.Main) {
                    myItemsList.add(MyItem(title, body, maxId++))
                    val editor: SharedPreferences.Editor = myPreferences.edit()
                    editor.putInt(MAX_ID_KEY, maxId)
                    editor.apply()
                    buildRecyclerView(myItemsList)
                    SendToast(resources.getString(R.string.added_post_message) + (maxId - 1))
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    SendToast(resources.getString(R.string.error_message) + e.message)
                }
                return@launch
            }
        }
    }

    private fun SendToast(message: String) {
        val duration = Toast.LENGTH_LONG
        Toast.makeText(applicationContext, message, duration).show()
    }

    private fun deleteItem(id: Int) {
        scope.launch {
            try {
                val result = async(SupervisorJob()) {
                    MyApp.instance.database.userDao().delete(PostDAO().apply { this.id = id })
                }
                result.await()
                withContext(Dispatchers.Main) {
                    myItemsList.remove(myItemsList.find { it.index == id })
                    SendToast(resources.getString(R.string.deleted_post_message))
                    buildRecyclerView(myItemsList)
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    SendToast(resources.getString(R.string.error_message) + e.message)
                }
                return@launch
            }
        }
    }

    private fun buildRecyclerView(myItems: MutableList<MyItem>) {
        binding.progressBar.visibility = View.INVISIBLE

        myItemsList = myItems;
        val viewManager = LinearLayoutManager(this@MainActivity)
        myRecyclerView.apply {
            layoutManager = viewManager
            adapter = UserAdapter(myItemsList) {
                deleteItem(it.index)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}