package com.example.database

import retrofit2.http.*


interface JSONPlaceHolderApi {
    @GET("/posts/{id}")
    suspend fun getPostWithID(@Path("id") id: Int): PostJSON

    @DELETE("/posts/{id}")
    suspend fun deletePostWithID(@Path("id") id: Int): PostJSON

    @POST("/posts")
    suspend fun addItem(@Query("body")body : String): PostJSON


    /*@GET("/posts/{id}")
    fun getPostWithID(@Path("id") id: Int): Call<Post>

    @DELETE("/posts/{id}")
    fun deletePostWithID(@Path("id") id: Int): Call<Post>

    @POST("/posts")
    fun addItem(@Query("body")body : String): Call<Post>*/
}