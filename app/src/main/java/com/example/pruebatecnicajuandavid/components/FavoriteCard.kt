package com.example.pruebatecnicajuandavid.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pruebatecnicajuandavid.data.FavoritePoint

@Composable
fun FavoriteCard(point: FavoritePoint) {
    val cardColor = when (point.type) {
        "alerta" -> Color(0xFFFFCDD2) // rojo claro
        "normal" -> Color(0xFFBBDEFB) // azul claro
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = point.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (point.type == "alerta") Color.Red else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tipo: ${point.type}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Lat: ${point.latitude}, Lon: ${point.longitude}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
