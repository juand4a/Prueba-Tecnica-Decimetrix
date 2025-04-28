package com.example.pruebatecnicajuandavid.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.example.pruebatecnicajuandavid.data.AppDatabase
import com.example.pruebatecnicajuandavid.repository.PointRepository
import com.example.pruebatecnicajuandavid.components.FavoriteCard

@Composable
fun FavoritesScreen(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = PointRepository(database.favoritePointDao())

    FavoritesScreenContent(repository = repository, navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(repository: PointRepository, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<com.example.pruebatecnicajuandavid.data.FavoritePoint>>(emptyList()) }

    // Cargar los favoritos cuando entra a la pantalla
    LaunchedEffect(Unit) {
        favorites = repository.getAllFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos(Presiona la carta para ir a la ubicacion que deseas)") }
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(favorites) { favorite ->
                FavoriteCard(point = favorite) {
                    navController.navigate("home/${favorite.latitude}/${favorite.longitude}")
                }
            }
        }
    }
}
