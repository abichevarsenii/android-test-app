package com.example.database

import androidx.room.*


@Dao
interface RoomApi {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg people: PostDAO?)

    @Delete
    suspend fun delete(person: PostDAO?)

    @Query("SELECT * FROM PostDAO")
    fun getAllPosts(): List<PostDAO?>?

    @Query("SELECT * FROM PostDAO WHERE id LIKE :id")
    suspend fun getById(id: Int): List<PostDAO?>?

    @Query("DELETE FROM PostDAO")
    fun deleteAll()
}
