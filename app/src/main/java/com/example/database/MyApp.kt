package com.example.database

import android.app.Application
import android.widget.Toast
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.Interceptor

import okhttp3.OkHttpClient
import android.content.Intent
import okhttp3.Request
import okhttp3.Response
import java.net.SocketTimeoutException
import androidx.room.RoomDatabase

import androidx.room.Database
import androidx.room.Room


class MyApp : Application() {

    private val BASE_URL = "https://jsonplaceholder.typicode.com"
    lateinit var mRetrofit: Retrofit
    lateinit var database: AppDatabase

    @Database(entities = [PostDAO::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun userDao(): PostDaoApi
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database-name").build()
    }

    companion object {
        lateinit var instance: MyApp
            private set
    }

}
