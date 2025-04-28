package com.example.pruebatecnicajuandavid.repository

import com.example.pruebatecnicajuandavid.data.FavoritePoint
import com.example.pruebatecnicajuandavid.data.FavoritePointDao

class PointRepository(private val dao: FavoritePointDao) {

    suspend fun getAllFavorites(): List<FavoritePoint> = dao.getAll() // <-- También suspend y devuelve List

    suspend fun insertFavorite(name: String, latitude: Double, longitude: Double, type: String) {
        dao.insert(FavoritePoint(name = name, latitude = latitude, longitude = longitude, type = type))
    }
}
