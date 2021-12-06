package com.example.database

import com.squareup.moshi.Json


class PostJSON {
    @Json(name="userId")
    var userId = 0

    @Json(name="id")
    var id = 0

    @Json(name="title")
    var title: String? = null

    @Json(name="body")
    var body: String? = null
}