package com.leon.campusgps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leon.campusgps.data.LugaresRepository
import com.leon.campusgps.data.UbicacionRepository
import com.leon.campusgps.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.leon.campusgps.util.distanciaMetros
import android.util.Log
import com.leon.campusgps.data.GpsApagadoException
import com.leon.campusgps.data.SinPlayServicesException

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {
    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()
    private val _lugares = MutableStateFlow(lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) })
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()
    private val _categoria = MutableStateFlow(CategoriaLugar.TODOS)
    val categoria: StateFlow<CategoriaLugar> = _categoria.asStateFlow()
    val lugaresFiltrados: StateFlow<List<LugarConDistancia>> = combine(lugares, categoria) { lista, filtro ->
        lista.filter { filtro.incluye(it.lugar) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _lugares.value)

    fun filtrar(categoria: CategoriaLugar) {
        _categoria.value = categoria
        val actual = _lugares.value.firstOrNull { it.lugar.id == _seleccionado.value }
        if (actual != null && !categoria.incluye(actual.lugar)) seleccionar(null)
    }

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()
    private val _revisionSeleccion = MutableStateFlow(0)
    val revisionSeleccion: StateFlow<Int> = _revisionSeleccion.asStateFlow()

    fun seleccionar(id: Int?) {
        if (id != null && _lugares.value.none { it.lugar.id == id }) return
        _seleccionado.value = id
        // Permite volver al mismo marcador después de desplazar el mapa a mano.
        _revisionSeleccion.value++
    }

    val masCercano: StateFlow<Lugar?> = lugaresFiltrados.map { lista ->
        lista.firstOrNull { it.metros != null }?.lugar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun buscar() {
        if (_estado.value == UbicacionUi.Buscando) return
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = errorDe(SinPlayServicesException())
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess { posicion ->
                    _seleccionado.value = null
                    actualizarDistancias(posicion)
                    _estado.value = UbicacionUi.Lista(posicion)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error("Sin permiso de ubicación no puedo mostrar dónde estás", TipoError.SIN_PERMISO)
    }

    private fun actualizarDistancias(posicion: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(posicion, it)) }
            .sortedBy { it.metros }
        Log.d("CampusGPS", _lugares.value.joinToString { "${it.lugar.nombre}: ${it.metros} m" })
    }

    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error("Sin permiso de ubicación no puedo mostrar dónde estás", TipoError.SIN_PERMISO)
        is GpsApagadoException -> UbicacionUi.Error("Activa el GPS e intenta de nuevo", TipoError.GPS_APAGADO)
        is SinPlayServicesException -> UbicacionUi.Error("Este dispositivo no tiene Google Play Services", TipoError.SIN_PLAY_SERVICES)
        else -> UbicacionUi.Error(e.message ?: "No se pudo obtener la ubicación", TipoError.OTRO)
    }
}
