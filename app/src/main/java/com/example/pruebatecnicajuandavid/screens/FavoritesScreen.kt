package com.example.pruebatecnicajuandavid.screens


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebatecnicajuandavid.data.AppDatabase
import com.example.pruebatecnicajuandavid.repository.PointRepository
import com.example.pruebatecnicajuandavid.components.FavoriteCard
import kotlinx.coroutines.launch

class FavoritesScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PointRepository(database.favoritePointDao())

        setContent {
            FavoritesScreenContent(repository)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(repository: PointRepository) {
    val scope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<com.example.pruebatecnicajuandavid.data.FavoritePoint>>(emptyList()) }

    // Cargar los favoritos cuando entra a la pantalla
    LaunchedEffect(Unit) {
        favorites = repository.getAllFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") }
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(favorites) { favorite ->
                FavoriteCard(favorite)
            }
        }
    }
}
