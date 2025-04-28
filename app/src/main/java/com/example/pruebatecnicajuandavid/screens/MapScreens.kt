package com.example.pruebatecnicajuandavid.screens

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.pruebatecnicajuandavid.R
import com.example.pruebatecnicajuandavid.data.AppDatabase
import com.example.pruebatecnicajuandavid.repository.PointRepository
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    lat: Double? = null,
    lon: Double? = null
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = PointRepository(database.favoritePointDao())
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var clickedPoint by remember { mutableStateOf<Point?>(null) }
    var mapView: MapView? by remember { mutableStateOf(null) }
    var pointAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var userLocation by remember { mutableStateOf<Point?>(null) }
    var currentStyle by remember { mutableStateOf(Style.MAPBOX_STREETS) }
    val alertaPoints = remember { mutableStateListOf<PointAnnotation>() }
    val pulsatingSize = remember { Animatable(0.07f) }

    setupPulseAnimation(pulsatingSize, alertaPoints, pointAnnotationManager)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) mapView?.location?.updateSettings { enabled = true; pulsingEnabled = true }
    }
    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    Scaffold(
        floatingActionButton = { MapActions(navController, mapView, userLocation, currentStyle) { currentStyle = it } }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            AndroidView(factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this

                    setupMap(this, context, repository, pulsatingSize, alertaPoints, scope) { manager ->
                        pointAnnotationManager = manager
                    }

                    location.addOnIndicatorPositionChangedListener { pos ->
                        userLocation = Point.fromLngLat(pos.longitude(), pos.latitude())
                    }

                    gestures.addOnMapClickListener { point ->
                        clickedPoint = point
                        showDialog = true
                        true
                    }
                }
            }, modifier = Modifier.fillMaxSize())

            // Centrar automáticamente si vienen coordenadas
            LaunchedEffect(lat, lon, mapView) {
                if (lat != null && lon != null && mapView != null) {
                    mapView?.getMapboxMap()?.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(lon, lat))
                            .zoom(14.0)
                            .build()
                    )
                }
            }

            if (showDialog && clickedPoint != null) {
                AddPointDialog(
                    onDismiss = { showDialog = false },
                    onPointSelected = { name, type ->
                        scope.launch {
                            clickedPoint?.let { point ->
                                val annotation = createAnnotation(pointAnnotationManager, point, name, type, pulsatingSize.value)
                                if (type == "alerta") alertaPoints.add(annotation!!)
                                repository.insertFavorite(name, point.latitude(), point.longitude(), type)
                            }
                        }
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MapActions(
    navController: NavHostController,
    mapView: MapView?,
    userLocation: Point?,
    currentStyle: String,
    onStyleChange: (String) -> Unit
) {
    Column {
        FloatingActionButton(onClick = {
            mapView?.getMapboxMap()?.loadStyleUri(
                if (currentStyle == Style.MAPBOX_STREETS) Style.SATELLITE else Style.MAPBOX_STREETS
            ) { onStyleChange(if (currentStyle == Style.MAPBOX_STREETS) Style.SATELLITE else Style.MAPBOX_STREETS) }
        }, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("🗺️")
        }

        FloatingActionButton(onClick = {
            userLocation?.let { point ->
                mapView?.getMapboxMap()?.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(point.longitude(), point.latitude()))
                        .zoom(14.0)
                        .build()
                )
            }
        }, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("🎯")
        }

        FloatingActionButton(onClick = {
            navController.navigate("favorites")
        }) {
            Text("★")
        }
    }
}

@Composable
private fun setupPulseAnimation(
    pulsatingSize: Animatable<Float, *>,
    alertaPoints: MutableList<PointAnnotation>,
    pointAnnotationManager: PointAnnotationManager?
) {
    LaunchedEffect(Unit) {
        while (true) {
            pulsatingSize.animateTo(0.09f, animationSpec = tween(500))
            pulsatingSize.animateTo(0.07f, animationSpec = tween(500))
        }
    }

    LaunchedEffect(pulsatingSize.value) {
        alertaPoints.forEach { annotation ->
            annotation.iconSize = pulsatingSize.value.toDouble()
            pointAnnotationManager?.update(annotation)
        }
    }
}

private fun setupMap(
    mapView: MapView,
    context: Context,
    repository: PointRepository,
    pulsatingSize: Animatable<Float, *>,
    alertaPoints: MutableList<PointAnnotation>,
    scope: CoroutineScope,
    onManagerReady: (PointAnnotationManager) -> Unit
) {
    mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
        val manager = mapView.annotations.createPointAnnotationManager()
        onManagerReady(manager)

        style.addImage("custom-marker", BitmapFactory.decodeResource(context.resources, R.drawable.ic_marker))
        style.addImage("alert-marker", BitmapFactory.decodeResource(context.resources, R.drawable.ic_alert_marker))

        scope.launch {
            val savedPoints = withContext(Dispatchers.IO) { repository.getAllFavorites() }
            savedPoints.forEach { favorite ->
                val annotation = createAnnotation(manager, Point.fromLngLat(favorite.longitude, favorite.latitude), favorite.name, favorite.type, pulsatingSize.value)
                if (favorite.type == "alerta") alertaPoints.add(annotation!!)
            }
        }

        try {
            val geoJsonStr = context.assets.open("initial_points.geojson").bufferedReader().use { it.readText() }
            val geoJsonFeatures = FeatureCollection.fromJson(geoJsonStr)
            geoJsonFeatures.features()?.forEach { feature ->
                (feature.geometry() as? Point)?.let { point ->
                    createAnnotation(manager, point, "GeoJSON Point", "normal", 0.05f)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun createAnnotation(
    manager: PointAnnotationManager?,
    point: Point,
    name: String,
    type: String,
    size: Float
): PointAnnotation? {
    return manager?.create(
        PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(if (type == "alerta") "alert-marker" else "custom-marker")
            .withIconSize(if (type == "alerta") size.toDouble() else 0.05)
            .withTextField(name)
            .withTextSize(12.0)
            .withTextOffset(listOf(0.0, 1.5))
    )
}
