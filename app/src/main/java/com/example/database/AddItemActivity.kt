package com.example.database

import android.content.Intent
import android.content.Intent.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.database.databinding.ActivityAddItemActivityBinding

class AddItemActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddItemActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val list = this.intent.getParcelableArrayExtra("list")
            intent.putExtra(LIST_KEY,list)
            intent.putExtra(TITLE_KEY,binding.nameText.text.toString())
            intent.putExtra(BODY_KEY,binding.bodyText.text.toString())
            intent.putExtra(USER_ID_KEY,binding.userIdText.text.toString())
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}