package com.example.pruebatecnicajuandavid.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoritePoint::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritePointDao(): FavoritePointDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "favorites_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
