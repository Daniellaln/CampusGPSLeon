package com.leon.campusgps.ui

import android.graphics.RectF
import android.animation.ValueAnimator
import android.os.Build
import android.provider.Settings
import android.graphics.Bitmap
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt
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
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.json.JSONArray
import org.json.JSONObject

private const val ESTILO_LIBRE = "https://tiles.openfreemap.org/styles/positron"
private const val ESTILO_OSCURO = "https://tiles.openfreemap.org/styles/dark"

@Composable
@SuppressLint("MissingPermission")
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier,
    revisionSeleccion: Int = 0,
    onSeleccionar: (Int) -> Unit = {},
    masCercanoId: Int? = null,
    contornoRadio: List<Posicion> = emptyList(),
    oscuro: Boolean = false,
    posicionActual: Posicion? = (estado as? UbicacionUi.Lista)?.posicion,
    seguirCamara: Boolean = true,
    onInteraccion: () -> Unit = {},
    onCaptura: (CapturaMapa?) -> Unit = {},
    margenInferior: Dp = 0.dp,
    margenDerecho: Dp = 0.dp,
    margenSuperior: Dp = 0.dp
) {
    val contexto = LocalContext.current
    val densidad = LocalDensity.current
    val ciclo = LocalLifecycleOwner.current.lifecycle
    val seleccionarActual by rememberUpdatedState(onSeleccionar)
    val interaccionActual by rememberUpdatedState(onInteraccion)
    val capturaActual by rememberUpdatedState(onCaptura)
    val scope = rememberCoroutineScope()
    var origen by remember { mutableStateOf(Offset.Zero) }
    var latitud by rememberSaveable { mutableDoubleStateOf(-12.0464) }
    var longitud by rememberSaveable { mutableDoubleStateOf(-77.0428) }
    var zoom by rememberSaveable { mutableDoubleStateOf(14.0) }
    var mapa by remember { mutableStateOf<MapLibreMap?>(null) }
    var errorMapa by remember { mutableStateOf(false) }
    var reintento by remember { mutableIntStateOf(0) }
    var propioSeleccionado by remember { mutableStateOf(false) }
    var idTocado by remember { mutableStateOf<Int?>(null) }
    var mapaInicializado by remember { mutableStateOf<MapLibreMap?>(null) }
    var estiloUbicacion by remember { mutableStateOf<Style?>(null) }
    var puntoAzulActivo by remember { mutableStateOf(false) }
    var versionEstilo by remember { mutableIntStateOf(0) }
    var ultimaCamaraAplicada by rememberSaveable { mutableStateOf<String?>(null) }
    val vista = remember(contexto) {
        MapLibre.getInstance(contexto.applicationContext)
        MapView(contexto, MapLibreMapOptions.createFromAttributes(contexto).textureMode(true)).apply {
            contentDescription = "Mapa del campus con ubicación y lugares"
            onCreate(null)
        }
    }
    val activo = remember(vista) { AtomicBoolean(true) }
    val capturando = remember(vista) { AtomicBoolean(false) }
    var ultimaCaptura by remember { mutableLongStateOf(0) }
    fun capturar(actual: MapLibreMap) {
        if (!activo.get() || SystemClock.elapsedRealtime() - ultimaCaptura < 400 || !capturando.compareAndSet(false,true)) return
        ultimaCaptura = SystemClock.elapsedRealtime()
        val revision = versionEstilo
        actual.snapshot { bitmap ->
            capturando.set(false)
            if (!activo.get() || revision != versionEstilo || vista.width == 0) return@snapshot
            val tamano = IntSize(vista.width,vista.height)
            val posicion = origen
            scope.launch {
                val pequena = withContext(Dispatchers.Default) {
                    val ancho = minOf(720,bitmap.width)
                    Bitmap.createScaledBitmap(bitmap,ancho,(bitmap.height*ancho/bitmap.width.toFloat()).toInt().coerceAtLeast(1),true)
                }
                if (activo.get() && revision == versionEstilo) capturaActual(CapturaMapa(pequena,tamano,posicion))
            }
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
        onDispose { activo.set(false); ciclo.removeObserver(observador); detener(); vista.onDestroy() }
    }

    DisposableEffect(vista) {
        val fallo = MapView.OnDidFailLoadingMapListener {
            errorMapa = true
            Log.w("CampusGPSMapa", "No se pudo descargar el mapa")
        }
        vista.addOnDidFailLoadingMapListener(fallo)
        onDispose { vista.removeOnDidFailLoadingMapListener(fallo) }
    }

    LaunchedEffect(vista, reintento, oscuro) {
        errorMapa = false
        mapa = null
        puntoAzulActivo = false
        capturaActual(null)
        vista.getMapAsync { actual ->
            if (mapaInicializado !== actual) {
            mapaInicializado = actual
            actual.cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitud, longitud)).zoom(zoom).build()
            actual.uiSettings.isAttributionEnabled = false
            actual.uiSettings.isLogoEnabled = false
            actual.addOnCameraMoveStartedListener { razon -> if (razon == 1) interaccionActual() }
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
            }
            actual.setStyle(Style.Builder().fromUri(if (oscuro) ESTILO_OSCURO else ESTILO_LIBRE)) { estilo ->
                estilo.addSource(GeoJsonSource("radio-datos", "{\"type\":\"FeatureCollection\",\"features\":[]}"))
                estilo.addLayer(FillLayer("radio-relleno", "radio-datos").withProperties(fillColor("#168B7A"), fillOpacity(.085f)))
                estilo.addLayer(LineLayer("radio-borde", "radio-datos").withProperties(lineColor("#168B7A"), lineWidth(1.5f), lineOpacity(.7f)))
                estilo.addSource(GeoJsonSource("campus-datos", "{\"type\":\"FeatureCollection\",\"features\":[]}"))
                estilo.addLayer(CircleLayer("campus-puntos", "campus-datos").withProperties(
                    circleColor(get("color")), circleRadius(get("radio")),
                    circleStrokeColor("#FFFFFF"), circleStrokeWidth(3f)))
                errorMapa = false
                mapa = actual
                versionEstilo++
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
                capturar(actual)
                val visibles = actual.queryRenderedFeatures(RectF(0f, 0f, vista.width.toFloat(), vista.height.toFloat()), "campus-puntos").size
                Log.d("CampusGPSMapa", "Cámara quieta zoom=$zoom, puntos visibles=$visibles")
            }
        }
        actual?.addOnCameraIdleListener(alTerminar)
        val cargado = MapView.OnDidFinishLoadingMapListener { actual?.let(::capturar) }
        vista.addOnDidFinishLoadingMapListener(cargado)
        actual?.let(::capturar)
        onDispose { actual?.removeOnCameraIdleListener(alTerminar); vista.removeOnDidFinishLoadingMapListener(cargado) }
    }

    LaunchedEffect(mapa, versionEstilo, posicionActual, lugares, masCercanoId, seleccionado, puntoAzulActivo) {
        val actual = mapa ?: return@LaunchedEffect
        val puntos = JSONArray()
        val lugaresVisibles = lugares
        fun agregar(id: Int, lat: Double, lon: Double, color: String) {
            puntos.put(JSONObject().put("type", "Feature")
                .put("geometry", JSONObject().put("type", "Point").put("coordinates", JSONArray().put(lon).put(lat)))
                .put("properties", JSONObject().put("id", id).put("color", color)
                    .put("radio", if (id == seleccionado) 12 else 9)))
        }
        if (posicionActual != null) {
            // Refuerzo vectorial del punto nativo: mantiene su visibilidad en GPU sin iconos bitmap.
            agregar(0, posicionActual.latitud, posicionActual.longitud, "#1674D1")
        }
        lugaresVisibles.forEach { item ->
            val lugar = item.lugar
            val color = if (lugar.id == masCercanoId) "#138A5B" else if(oscuro) "#94CDDD" else "#277CA5"
            agregar(lugar.id, lugar.latitud, lugar.longitud, color)
        }
        actual.style?.getSourceAs<GeoJsonSource>("campus-datos")?.setGeoJson(
            JSONObject().put("type", "FeatureCollection").put("features", puntos).toString())
        Log.d("CampusGPSMapa", "Marcadores campus=${lugaresVisibles.size}, propio=${estado is UbicacionUi.Lista}, cercano=$masCercanoId")
        capturar(actual)
    }

    LaunchedEffect(mapa, versionEstilo, contornoRadio) {
        val coordenadas = JSONArray()
        contornoRadio.forEach { coordenadas.put(JSONArray().put(it.longitud).put(it.latitud)) }
        val geojson = if (contornoRadio.isEmpty()) "{\"type\":\"FeatureCollection\",\"features\":[]}" else
            JSONObject().put("type", "Feature").put("properties", JSONObject())
                .put("geometry", JSONObject().put("type", "Polygon").put("coordinates", JSONArray().put(coordenadas))).toString()
        mapa?.style?.getSourceAs<GeoJsonSource>("radio-datos")?.setGeoJson(geojson)
    }
    LaunchedEffect(mapa,margenInferior,margenDerecho,margenSuperior,densidad) {
        with(densidad) {
            mapa?.setPadding(0,margenSuperior.toPx().roundToInt(),margenDerecho.toPx().roundToInt(),margenInferior.toPx().roundToInt())
        }
    }

    LaunchedEffect(mapa, posicionActual, versionEstilo) {
        val actual = mapa ?: return@LaunchedEffect
        val estilo = actual.style ?: return@LaunchedEffect
        val permitido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (permitido && posicionActual != null) {
            val componente = actual.locationComponent
            if (estiloUbicacion !== estilo) {
                componente.activateLocationComponent(LocationComponentActivationOptions.builder(contexto, estilo)
                    .useDefaultLocationEngine(false).build())
                componente.cameraMode = CameraMode.NONE
                componente.renderMode = RenderMode.NORMAL
                estiloUbicacion = estilo
            }
            componente.isLocationComponentEnabled = true
            componente.forceLocationUpdate(Location("campusgps").apply {
                latitude = posicionActual.latitud; longitude = posicionActual.longitud
                posicionActual.precisionMetros?.let { accuracy = it }
            })
            puntoAzulActivo = true
            Log.d("CampusGPSMapa", "Punto nativo activo, precisión=${posicionActual.precisionMetros}")
        } else if (estiloUbicacion != null) {
            actual.locationComponent.isLocationComponentEnabled = false
            puntoAzulActivo = false
        }
    }
    val claveCamara = if (seleccionado != null) "lugar:$seleccionado:$revisionSeleccion" else
        posicionActual?.let { "pos:${it.latitud}:${it.longitud}:$revisionSeleccion" }
    LaunchedEffect(mapa, claveCamara, seguirCamara) {
        val actual = mapa ?: return@LaunchedEffect
        if (claveCamara == null || claveCamara == ultimaCamaraAplicada) return@LaunchedEffect
        if (!seguirCamara && seleccionado == null) return@LaunchedEffect
        val animar = if (Build.VERSION.SDK_INT >= 26) ValueAnimator.areAnimatorsEnabled() else
            Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        fun mover(latitud: Double, longitud: Double, zoom: Double) {
            val cambio = CameraUpdateFactory.newLatLngZoom(LatLng(latitud, longitud), zoom)
            if (animar) actual.animateCamera(cambio, 500) else actual.moveCamera(cambio)
        }
        val lugar = lugares.firstOrNull { it.lugar.id == seleccionado }?.lugar
        if (lugar != null) {
            mover(lugar.latitud, lugar.longitud, 17.0)
            propioSeleccionado = false
            Log.d("CampusGPSMapa", "Cámara seleccionada id=${lugar.id}, zoom=17")
        } else if (posicionActual != null) {
            idTocado = null; propioSeleccionado = false
            mover(posicionActual.latitud, posicionActual.longitud, 16.0)
        }
        ultimaCamaraAplicada = claveCamara
    }

    Box(modifier.onGloballyPositioned { origen = it.positionInRoot() }.semantics {
        contentDescription = "Mapa del campus"
        stateDescription = when { errorMapa -> "Sin conexión al mapa"; mapa == null -> "Cargando mapa"; else -> "Mapa listo" }
    }) {
        AndroidView(factory = { vista }, modifier = Modifier.fillMaxSize())
        if (mapa == null && !errorMapa) {
            CircularProgressIndicator(Modifier.align(Alignment.Center).size(32.dp))
        }
        if (errorMapa) {
            Surface(Modifier.align(Alignment.Center).padding(24.dp), shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se pudo cargar el mapa. Revisa tu conexión.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { reintento++ }) { Text("Reintentar mapa") }
                }
            }
        }
    }
}

