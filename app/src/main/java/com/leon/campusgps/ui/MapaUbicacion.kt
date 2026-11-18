package com.leon.campusgps.ui

import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.leon.campusgps.model.*
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.json.JSONArray
import org.json.JSONObject

private const val ESTILO_LIBRE = "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier,
    revisionSeleccion: Int = 0,
    onSeleccionar: (Int) -> Unit = {},
    masCercanoId: Int? = null
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle
    val seleccionarActual by rememberUpdatedState(onSeleccionar)
    var latitud by rememberSaveable { mutableDoubleStateOf(-12.0464) }
    var longitud by rememberSaveable { mutableDoubleStateOf(-77.0428) }
    var zoom by rememberSaveable { mutableDoubleStateOf(12.0) }
    var mapa by remember { mutableStateOf<MapLibreMap?>(null) }
    var errorMapa by remember { mutableStateOf(false) }
    var reintento by remember { mutableIntStateOf(0) }
    var propioSeleccionado by remember { mutableStateOf(false) }
    var idTocado by remember { mutableStateOf<Int?>(null) }
    val vista = remember(contexto) {
        MapLibre.getInstance(contexto.applicationContext)
        MapView(contexto, MapLibreMapOptions.createFromAttributes(contexto).textureMode(true)).apply {
            contentDescription = "Mapa del campus con ubicación y lugares"
            onCreate(null)
        }
    }

    DisposableEffect(vista, ciclo) {
        var iniciado = false
        var reanudado = false
        fun iniciar() { if (!iniciado) { vista.onStart(); iniciado = true } }
        fun reanudar() { iniciar(); if (!reanudado) { vista.onResume(); reanudado = true } }
        fun pausar() { if (reanudado) { vista.onPause(); reanudado = false } }
        fun detener() { pausar(); if (iniciado) { vista.onStop(); iniciado = false } }
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_START -> iniciar()
                Lifecycle.Event.ON_RESUME -> reanudar()
                Lifecycle.Event.ON_PAUSE -> pausar()
                Lifecycle.Event.ON_STOP -> detener()
                else -> Unit
            }
        }
        ciclo.addObserver(observador)
        if (ciclo.currentState.isAtLeast(Lifecycle.State.STARTED)) iniciar()
        if (ciclo.currentState.isAtLeast(Lifecycle.State.RESUMED)) reanudar()
        onDispose { ciclo.removeObserver(observador); detener(); vista.onDestroy() }
    }

    DisposableEffect(vista) {
        val fallo = MapView.OnDidFailLoadingMapListener {
            errorMapa = true
            Log.w("CampusGPSMapa", "No se pudo descargar el mapa")
        }
        vista.addOnDidFailLoadingMapListener(fallo)
        onDispose { vista.removeOnDidFailLoadingMapListener(fallo) }
    }

    LaunchedEffect(vista, reintento) {
        errorMapa = false
        vista.getMapAsync { actual ->
            actual.cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitud, longitud)).zoom(zoom).build()
            actual.uiSettings.isAttributionEnabled = true
            actual.uiSettings.isLogoEnabled = false
            actual.addOnMapClickListener { punto ->
                val pixel = actual.projection.toScreenLocation(punto)
                val margen = 24 * contexto.resources.displayMetrics.density
                val encontrado = actual.queryRenderedFeatures(
                    RectF(pixel.x-margen, pixel.y-margen, pixel.x+margen, pixel.y+margen), "campus-puntos"
                ).firstOrNull()
                encontrado?.let {
                    val id = it.getNumberProperty("id").toInt()
                    idTocado = id.takeIf { valor -> valor != 0 }
                    propioSeleccionado = id == 0
                    if (id != 0) seleccionarActual(id)
                    Log.d("CampusGPSMapa", "Marcador tocado id=$id")
                }
                encontrado != null
            }
            actual.setStyle(Style.Builder().fromUri(ESTILO_LIBRE)) { estilo ->
                estilo.addSource(GeoJsonSource("campus-datos", "{\"type\":\"FeatureCollection\",\"features\":[]}"))
                estilo.addLayer(CircleLayer("campus-puntos", "campus-datos").withProperties(
                    circleColor(get("color")), circleRadius(get("radio")),
                    circleStrokeColor("#FFFFFF"), circleStrokeWidth(3f)))
                errorMapa = false
                mapa = actual
                Log.d("CampusGPSMapa", "Estilo OpenFreeMap cargado")
            }
        }
    }

    DisposableEffect(mapa) {
        val actual = mapa
        val alTerminar = MapLibreMap.OnCameraIdleListener {
            actual?.cameraPosition?.let { posicion ->
                posicion.target?.let { latitud = it.latitude; longitud = it.longitude }
                zoom = posicion.zoom
                val visibles = actual.queryRenderedFeatures(RectF(0f, 0f, vista.width.toFloat(), vista.height.toFloat()), "campus-puntos").size
                Log.d("CampusGPSMapa", "Cámara quieta zoom=$zoom, puntos visibles=$visibles")
            }
        }
        actual?.addOnCameraIdleListener(alTerminar)
        onDispose { actual?.removeOnCameraIdleListener(alTerminar) }
    }

    LaunchedEffect(mapa, estado, lugares, masCercanoId, seleccionado) {
        val actual = mapa ?: return@LaunchedEffect
        val puntos = JSONArray()
        val lugaresVisibles = lugares
        fun agregar(id: Int, lat: Double, lon: Double, color: String) {
            puntos.put(JSONObject().put("type", "Feature")
                .put("geometry", JSONObject().put("type", "Point").put("coordinates", JSONArray().put(lon).put(lat)))
                .put("properties", JSONObject().put("id", id).put("color", color)
                    .put("radio", if (id == seleccionado) 12 else 9)))
        }
        if (estado is UbicacionUi.Lista) {
            agregar(0, estado.posicion.latitud, estado.posicion.longitud, "#D63031")
        }
        lugaresVisibles.forEach { item ->
            val lugar = item.lugar
            val color = if (lugar.id == masCercanoId) "#289342" else "#1C7ED6"
            agregar(lugar.id, lugar.latitud, lugar.longitud, color)
        }
        actual.style?.getSourceAs<GeoJsonSource>("campus-datos")?.setGeoJson(
            JSONObject().put("type", "FeatureCollection").put("features", puntos).toString())
        Log.d("CampusGPSMapa", "Marcadores campus=${lugaresVisibles.size}, propio=${estado is UbicacionUi.Lista}, cercano=$masCercanoId")
    }

    LaunchedEffect(mapa, estado) {
        val actual = mapa ?: return@LaunchedEffect
        if (estado is UbicacionUi.Lista && seleccionado == null) {
            idTocado = null
            propioSeleccionado = false
            actual.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(estado.posicion.latitud, estado.posicion.longitud), 16.0), 600)
        }
    }
    LaunchedEffect(mapa, seleccionado, revisionSeleccion) {
        val actual = mapa ?: return@LaunchedEffect
        lugares.firstOrNull { it.lugar.id == seleccionado }?.lugar?.let { lugar ->
            actual.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lugar.latitud, lugar.longitud), 17.0), 500)
            propioSeleccionado = false
            Log.d("CampusGPSMapa", "Cámara seleccionada id=${lugar.id}, zoom=17")
        }
    }

    Box(modifier.semantics {
        contentDescription = "Mapa del campus"
        stateDescription = when { errorMapa -> "Sin conexión al mapa"; mapa == null -> "Cargando mapa"; else -> "Mapa listo" }
    }) {
        AndroidView(factory = { vista }, modifier = Modifier.fillMaxSize())
        val lugarElegido = lugares.firstOrNull { it.lugar.id == (seleccionado ?: idTocado) }?.lugar
        if (propioSeleccionado || lugarElegido != null) {
            Surface(Modifier.align(Alignment.TopCenter).padding(12.dp), shape = MaterialTheme.shapes.medium,
                shadowElevation = 4.dp) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (propioSeleccionado) "Estás aquí" else lugarElegido!!.nombre, style = MaterialTheme.typography.titleMedium)
                    if (!propioSeleccionado) Text(lugarElegido!!.categoria, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (mapa == null && !errorMapa) {
            CircularProgressIndicator(Modifier.align(Alignment.Center).size(32.dp))
        }
        if (errorMapa) {
            Surface(Modifier.align(Alignment.TopCenter).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se pudo cargar el mapa. Revisa tu conexión.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { reintento++ }) { Text("Reintentar mapa") }
                }
            }
        }
        Column(Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 28.dp)) {
            FilledTonalIconButton(onClick = { mapa?.animateCamera(CameraUpdateFactory.zoomIn()) }) { Text("+", fontSize = 24.sp) }
            FilledTonalIconButton(onClick = { mapa?.animateCamera(CameraUpdateFactory.zoomOut()) }) { Text("−", fontSize = 24.sp) }
        }
        Surface(Modifier.align(Alignment.BottomStart), color = MaterialTheme.colorScheme.surface.copy(alpha = .92f)) {
            Text("© OpenStreetMap · OpenMapTiles · OpenFreeMap", fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
        }
    }
}

