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

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {
    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()
    private val _lugares = MutableStateFlow(lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) })
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

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

    val masCercano: StateFlow<Lugar?> = lugares.map { lista ->
        lista.firstOrNull { it.metros != null }?.lugar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun buscar() {
        if (_estado.value == UbicacionUi.Buscando) return
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
        _estado.value = UbicacionUi.Error("Sin permiso de ubicación no puedo mostrar dónde estás")
    }

    private fun actualizarDistancias(posicion: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(posicion, it)) }
            .sortedBy { it.metros }
        Log.d("CampusGPS", _lugares.value.joinToString { "${it.lugar.nombre}: ${it.metros} m" })
    }

    private fun errorDe(e: Throwable) = UbicacionUi.Error(
        if (e is SecurityException) "Falta el permiso de ubicación" else e.message ?: "No se pudo obtener la ubicación"
    )
}
