package com.example.pruebatecnicajuandavid.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoritePoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String // "normal" o "alerta"
)
