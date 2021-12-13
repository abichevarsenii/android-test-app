package com.example.database

import androidx.room.*


@Dao
interface PostDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg people: PostEntity?)

    @Delete
    suspend fun delete(person: PostEntity?)

    @Query("SELECT * FROM PostEntity")
    fun getAllPosts(): List<PostEntity?>?

    @Query("SELECT * FROM PostEntity WHERE id LIKE :id")
    suspend fun getById(id: Int): List<PostEntity?>?

    @Query("DELETE FROM PostEntity")
    fun deleteAll()
}
