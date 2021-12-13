package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
class PostEntity {
    @PrimaryKey
    var id = 0
    var userId = 0
    var title: String? = null
    var body: String? = null
}