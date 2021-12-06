package com.example.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyItem(val name: String?, val description: String?, val index: Int) : Parcelable {
    constructor(postDAO: PostDAO) : this(postDAO.title,postDAO.body,postDAO.id)
}