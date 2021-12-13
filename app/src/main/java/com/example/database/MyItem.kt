package com.example.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyItem(val name: String?, val description: String?, val index: Int) : Parcelable {
    constructor(postEntity: PostEntity) : this(postEntity.title,postEntity.body,postEntity.id)
}