package com.example.database;

import androidx.room.*


@Dao
interface PostDaoApi {
    // Добавление Person в бд
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg people: PostDAO?)

    // Удаление Person из бд
    @Delete
    suspend fun delete(person: PostDAO?)


    @Query("SELECT * FROM PostDAO")
    fun getAllPosts(): List<PostDAO?>?

   /* // Получение всех Person из бд
    @get:Query("SELECT MAX(id) FROM PostDAO")
    val max: PostDAO?*/

    // Получение всех Person из бд с условием
    @Query("SELECT * FROM PostDAO WHERE id LIKE :id")
    suspend fun getById(id: Int): List<PostDAO?>?

    @Query("DELETE FROM PostDAO")
    fun deleteAll()
}
