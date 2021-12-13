package com.example.database

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast
import java.io.IOException

const val USER_ID_KEY = "userId"
const val BODY_KEY = "body"
const val TITLE_KEY = "title"
const val MAX_ID_KEY = "max_id"
const val PREFERENCES_KEY = "preferences"
const val HAS_BD_KEY = "has_bd"
const val LIST_KEY = "list"


class MainActivity : AppCompatActivity() {
    lateinit var myRecyclerView: RecyclerView
    private var myItemsList: MutableList<MyItem> = mutableListOf()
    lateinit var scope: CoroutineScope
    private lateinit var binding: ActivityMainBinding
    private var numberPosts = 10
    private var maxId = 0
    private lateinit var myPreferences: SharedPreferences
    private val animationDuration = 100L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myRecyclerView = binding.myRecyclerView
        supportActionBar?.hide()
        scope = CoroutineScope(Dispatchers.Default)
        myPreferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        maxId = myPreferences.getInt(MAX_ID_KEY, 0)

        binding.addItemButton.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra(LIST_KEY, myItemsList.toTypedArray())
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.refreshDataButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.refreshDataButton.isClickable = false
            scope.launch {
                if (refreshData()) {
                    withContext(Dispatchers.Main) {
                        buildRecyclerView(myItemsList)
                    }
                } else {
                    showData()
                }
                withContext(Dispatchers.Main){
                    binding.refreshDataButton.isClickable = true
                }
            }
        }

        binding.myRecyclerView.addOnScrollListener( object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    binding.refreshDataButton.animate().alpha(0f).setDuration(animationDuration).start()
                    binding.addItemButton.animate().setDuration(animationDuration).alpha(0f).start()
                } else {
                    binding.refreshDataButton.animate().setDuration(animationDuration).alpha(1f).start()
                    binding.addItemButton.animate().setDuration(animationDuration).alpha(1f).start()
                }
            }
        })

        if (intent.hasExtra(LIST_KEY)) {
            unpackBundle()
            unpackItem()
        } else {
            scope.launch {
                if (!showData()) {
                    refreshData()
                }
            }
        }
    }

    private fun unpackBundle() {
        val list = intent.getParcelableArrayExtra(LIST_KEY)!!
        val list2 = mutableListOf<MyItem>()
        for (item in list) {
            list2.add(item as MyItem)
        }
        myItemsList = list2
        buildRecyclerView(myItemsList)
    }

    private fun unpackItem() {
        if (intent.hasExtra(USER_ID_KEY)) {
            var userId = intent.getStringExtra(USER_ID_KEY)!!
            val body = intent.getStringExtra(BODY_KEY)!!
            val title = intent.getStringExtra(TITLE_KEY)!!

            intent.removeExtra(USER_ID_KEY)
            intent.removeExtra(BODY_KEY)
            intent.removeExtra(TITLE_KEY)

            if (userId == "") userId = "0"

            scope.launch {
                addItem(userId.toInt(), body, title, true)
            }
        }
    }

    private fun deleteData() {
        MyApp.instance.database.userDao().deleteAll()
        maxId = 0
        myItemsList.clear()
        myPreferences.edit().putInt(MAX_ID_KEY, 0).apply()
    }

    private suspend fun showData(): Boolean {
        val await = MyApp.instance.database.userDao().getAllPosts()
        if (await != null) {
            myItemsList = MutableList(await.size) { MyItem(await[it]!!) }
        }
        withContext(Dispatchers.Main) {
            buildRecyclerView(myItemsList)
        }
        return myItemsList.size > 0
    }

    private suspend fun refreshData(): Boolean {
        try {
            val bufferList = mutableListOf<PostJSON>()
            for (i in 1..numberPosts) {
                val await = MyApp.instance.mRetrofit.create(JSONPlaceHolderApi::class.java).getPostWithID(i)
                bufferList.add(await)
                maxId = await.id
            }
            deleteData()
            bufferList.forEach { addItem(it.userId, it.body, it.title, false) }
            myPreferences.edit().putInt(MAX_ID_KEY, maxId++).apply()
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                sendToast(resources.getString(R.string.error_message) + e.message)
                binding.progressBar.visibility = View.INVISIBLE
            }
            return false
        }
        withContext(Dispatchers.Main) {
            sendToast(resources.getString(R.string.finish_refreshing_message))
        }
        return true
    }

    private suspend fun addItem(userId: Int, body: String?, title: String?, logging: Boolean) {
        try {
            val answer = PostEntity().apply {
                this.title = title
                this.body = body
                this.userId = userId
                this.id = maxId
            }
            MyApp.instance.database.userDao().insertAll(answer)
            withContext(Dispatchers.Main) {
                myItemsList.add(MyItem(title, body, maxId++))
                myPreferences.edit().putInt(MAX_ID_KEY, maxId).apply()
                if (logging) {
                    sendToast(resources.getString(R.string.added_post_message) + (maxId - 1))
                }
                buildRecyclerView(myItemsList)
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                sendToast(resources.getString(R.string.error_message) + e.message)
            }
        }
    }

    private suspend fun deleteItem(id: Int) {
        try {
            MyApp.instance.database.userDao().delete(PostEntity().apply { this.id = id })
            withContext(Dispatchers.Main) {
                myItemsList.remove(myItemsList.find { it.index == id })
                sendToast(resources.getString(R.string.deleted_post_message))
                buildRecyclerView(myItemsList)
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                sendToast(resources.getString(R.string.error_message) + e.message)
            }
            return
        }
    }

    private fun buildRecyclerView(myItems: MutableList<MyItem>) {
        binding.progressBar.visibility = View.INVISIBLE

        myItemsList = myItems
        val viewManager = LinearLayoutManager(this@MainActivity)
        myRecyclerView.apply {
            layoutManager = viewManager
            adapter = UserAdapter(myItemsList) {
                scope.launch {
                    deleteItem(it.index)
                }
            }
        }
    }

    private fun sendToast(message: String) {
        val duration = Toast.LENGTH_LONG
        Toast.makeText(applicationContext, message, duration).show()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val list = savedInstanceState.getParcelableArray(LIST_KEY)!!
        val list2 = mutableListOf<MyItem>()
        for (item in list) {
            list2.add(item as MyItem)
        }
        myItemsList = list2
        buildRecyclerView(myItemsList)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray(LIST_KEY, myItemsList.toTypedArray())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}