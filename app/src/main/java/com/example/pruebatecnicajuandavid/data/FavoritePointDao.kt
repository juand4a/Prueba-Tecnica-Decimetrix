package com.example.pruebatecnicajuandavid.data

import androidx.room.*

@Dao
interface FavoritePointDao {
    @Query("SELECT * FROM FavoritePoint")
    suspend fun getAll(): List<FavoritePoint> // <-- Ahora es suspend y devuelve directamente la lista

    @Insert
    suspend fun insert(point: FavoritePoint)
}
