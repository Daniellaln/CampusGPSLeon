package com.leon.campusgps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leon.campusgps.data.LugaresRepository
import com.leon.campusgps.data.UbicacionRepository
import com.leon.campusgps.data.PreferenciasRepository
import com.leon.campusgps.data.PreferenciasEnMemoria
import com.leon.campusgps.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import com.leon.campusgps.util.distanciaMetros
import com.leon.campusgps.util.contornoDelRadio
import com.leon.campusgps.util.estaDentroDelRadio
import com.leon.campusgps.data.GpsApagadoException
import com.leon.campusgps.data.SinPlayServicesException

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository,
    private val preferenciasRepo: PreferenciasRepository = PreferenciasEnMemoria()
) : ViewModel() {
    private var seguimientoJob: Job? = null
    private var esperaInicialJob: Job? = null
    private var ultimaPosicion: Posicion? = null
    private var generacionSeguimiento = 0
    private val _seguimiento = MutableStateFlow(false)
    val seguimiento: StateFlow<Boolean> = _seguimiento.asStateFlow()
    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()
    private val _lugares = MutableStateFlow(lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) })
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()
    private val _categoria = MutableStateFlow(CategoriaLugar.TODOS)
    val categoria: StateFlow<CategoriaLugar> = _categoria.asStateFlow()
    private val _radio = MutableStateFlow(200)
    val radio: StateFlow<Int> = _radio.asStateFlow()
    val contornoRadio: StateFlow<List<Posicion>> = combine(estado, radio) { actual, metros ->
        if (actual is UbicacionUi.Lista) contornoDelRadio(actual.posicion, metros) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val lugaresFiltrados: StateFlow<List<LugarConDistancia>> = combine(lugares, categoria, radio) { lista, filtro, metros ->
        lista.filter { filtro.incluye(it.lugar) }.map { it.copy(dentroDelRadio = estaDentroDelRadio(it.metros, metros)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _lugares.value)

    fun cambiarRadio(metros: Int) {
        if (metros in listOf(100, 200, 500)) _radio.value = metros
    }

    fun filtrar(categoria: CategoriaLugar) {
        _categoria.value = categoria
        val actual = _lugares.value.firstOrNull { it.lugar.id == _seleccionado.value }
        if (actual != null && !categoria.incluye(actual.lugar)) seleccionar(null)
    }

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()
    private val _revisionSeleccion = MutableStateFlow(0)
    val revisionSeleccion: StateFlow<Int> = _revisionSeleccion.asStateFlow()
    private val _avisos = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val avisos = _avisos.asSharedFlow()
    val modoTema = preferenciasRepo.modoTema.stateIn(viewModelScope, SharingStarted.Eagerly, ModoTema.SISTEMA)

    fun cambiarTema(modo: ModoTema) {
        viewModelScope.launch {
            try { preferenciasRepo.guardarTema(modo) }
            catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) { _avisos.emit("No se pudo guardar el tema") }
        }
    }
    val ultimoLugar: StateFlow<Lugar?> = preferenciasRepo.ultimoLugarId.map { id ->
        _lugares.value.firstOrNull { it.lugar.id == id }?.lugar
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            val id = preferenciasRepo.ultimoLugarId.first()
            if (_revisionSeleccion.value == 0 && _lugares.value.any { it.lugar.id == id }) {
                _seleccionado.value = id
                _revisionSeleccion.value++
            }
        }
    }

    fun seleccionar(id: Int?) {
        if (id != null && _lugares.value.none { it.lugar.id == id }) return
        _seleccionado.value = id
        // Permite volver al mismo marcador después de desplazar el mapa a mano.
        _revisionSeleccion.value++
        if (id != null) viewModelScope.launch {
            try { preferenciasRepo.guardarUltimoLugar(id) }
            catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) { _avisos.emit("No se pudo guardar el último lugar") }
        }
    }

    fun volverAlUltimoLugar() {
        ultimoLugar.value?.let { lugar ->
            _categoria.value = CategoriaLugar.TODOS
            seleccionar(lugar.id)
        }
    }

    val masCercano: StateFlow<Lugar?> = lugaresFiltrados.map { lista ->
        lista.firstOrNull { it.metros != null }?.lugar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun buscar() {
        if (_seguimiento.value) { seleccionar(null); return }
        if (_estado.value == UbicacionUi.Buscando) return
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = errorDe(SinPlayServicesException())
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess { posicion ->
                    ultimaPosicion = posicion
                    _seleccionado.value = null
                    actualizarDistancias(posicion)
                    _estado.value = UbicacionUi.Lista(posicion)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun iniciarSeguimiento() {
        if (_seguimiento.value) return
        if (!ubicacionRepo.playServicesDisponible()) { _estado.value = errorDe(SinPlayServicesException()); return }
        _seleccionado.value = null
        _seguimiento.value = true
        _estado.value = UbicacionUi.Buscando
        val generacion = ++generacionSeguimiento
        seguimientoJob = viewModelScope.launch {
            try {
                ubicacionRepo.observarPosiciones().collect { posicion ->
                    ultimaPosicion = posicion
                    actualizarDistancias(posicion)
                    _estado.value = UbicacionUi.Lista(posicion)
                    esperaInicialJob?.cancel()
                }
            } catch (cancelacion: CancellationException) { throw cancelacion }
            catch (error: Exception) { _estado.value = errorDe(error) }
            finally {
                if (generacion == generacionSeguimiento) {
                    _seguimiento.value = false
                    esperaInicialJob?.cancel()
                }
            }
        }
        esperaInicialJob = viewModelScope.launch {
            delay(15_000)
            if (generacion == generacionSeguimiento && _seguimiento.value && _estado.value == UbicacionUi.Buscando) {
                seguimientoJob?.cancel()
                _seguimiento.value = false
                _estado.value = errorDe(com.leon.campusgps.data.PosicionNoDisponibleException())
            }
        }
    }

    fun detenerSeguimiento() {
        generacionSeguimiento++
        seguimientoJob?.cancel()
        esperaInicialJob?.cancel()
        _seguimiento.value = false
        if (_estado.value == UbicacionUi.Buscando)
            _estado.value = ultimaPosicion?.let { UbicacionUi.Lista(it) } ?: UbicacionUi.Inicial
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error("Sin permiso de ubicación no puedo mostrar dónde estás", TipoError.SIN_PERMISO)
    }

    private fun actualizarDistancias(posicion: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(posicion, it)) }
            .sortedBy { it.metros }
    }

    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error("Sin permiso de ubicación no puedo mostrar dónde estás", TipoError.SIN_PERMISO)
        is GpsApagadoException -> UbicacionUi.Error("Activa el GPS e intenta de nuevo", TipoError.GPS_APAGADO)
        is SinPlayServicesException -> UbicacionUi.Error("Este dispositivo no tiene Google Play Services", TipoError.SIN_PLAY_SERVICES)
        else -> UbicacionUi.Error(e.message ?: "No se pudo obtener la ubicación", TipoError.OTRO)
    }
}
