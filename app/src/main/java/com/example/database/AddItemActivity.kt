package com.example.database

import android.content.Intent
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
            intent.putExtra("list",list)
            intent.putExtra("title",binding.nameText.text.toString())
            intent.putExtra("body",binding.bodyText.text.toString())
            intent.putExtra("userId",binding.userIdText.text.toString())
            startActivity(intent)
        }
    }
}