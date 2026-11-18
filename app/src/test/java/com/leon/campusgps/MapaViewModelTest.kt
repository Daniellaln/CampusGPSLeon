package com.leon.campusgps

import androidx.lifecycle.viewModelScope
import com.leon.campusgps.data.*
import com.leon.campusgps.model.*
import com.leon.campusgps.viewmodel.MapaViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After

@OptIn(ExperimentalCoroutinesApi::class)
class MapaViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun terminar() { Dispatchers.resetMain() }
    private class Repo(var disponible: Boolean = true,
        val respuesta: suspend () -> Result<Posicion>) : UbicacionRepository {
        var llamadas = 0
        override fun playServicesDisponible() = disponible
        override fun observarPosiciones() = kotlinx.coroutines.flow.flow { emit(obtenerPosicion().getOrThrow()) }
        override suspend fun obtenerPosicion(): Result<Posicion> { llamadas++; return respuesta() }
    }
    private fun vm(repo: UbicacionRepository) = MapaViewModel(repo, LugaresRepositoryLocal())

    @Test fun filtroDeEstudioComparteListaConMapaYLimpiaSeleccionOculta() = runTest(dispatcher) {
        val m=vm(Repo { Result.failure(Exception()) })
        m.seleccionar(2)
        m.filtrar(CategoriaLugar.ESTUDIO)
        runCurrent()
        assertEquals(listOf(1,3),m.lugaresFiltrados.value.map { it.lugar.id })
        assertNull(m.seleccionado.value)
        assertEquals(UbicacionUi.Inicial,m.estado.value)
    }
    @Test fun volverATodoRecuperaLugaresSinConsultarGPS() = runTest(dispatcher) {
        val r=Repo { Result.failure(Exception()) };val m=vm(r)
        m.filtrar(CategoriaLugar.COMIDA);runCurrent()
        assertEquals(listOf(2),m.lugaresFiltrados.value.map { it.lugar.id })
        m.filtrar(CategoriaLugar.TODOS);runCurrent()
        assertEquals(5,m.lugaresFiltrados.value.size)
        assertEquals(0,r.llamadas)
    }

    @Test fun inicialmenteHayCincoLugaresSinDistancia() {
        val m=vm(Repo { Result.failure(Exception()) })
        assertEquals(5,m.lugares.value.size)
        assertTrue(m.lugares.value.all { it.metros == null })
        assertNull(m.masCercano.value)
    }
    @Test fun denegacionTieneTipoYMensaje() {
        val m=vm(Repo { Result.failure(Exception()) });m.permisoDenegado()
        assertEquals(TipoError.SIN_PERMISO,(m.estado.value as UbicacionUi.Error).tipo)
    }
    @Test fun sinPlayServicesNoSePideLaPosicion() = runTest(dispatcher) {
        val r=Repo(false) { Result.failure(Exception()) };val m=vm(r);m.buscar();runCurrent()
        assertEquals(0,r.llamadas)
        assertEquals(TipoError.SIN_PLAY_SERVICES,(m.estado.value as UbicacionUi.Error).tipo)
    }
    @Test fun gpsApagadoEsUnErrorRecuperable() = runTest(dispatcher) {
        val m=vm(Repo { Result.failure(GpsApagadoException()) });m.buscar();runCurrent()
        assertEquals(TipoError.GPS_APAGADO,(m.estado.value as UbicacionUi.Error).tipo)
    }
    @Test fun permisoRevocadoSeClasifica() = runTest(dispatcher) {
        val m=vm(Repo { Result.failure(SecurityException()) });m.buscar();runCurrent()
        assertEquals(TipoError.SIN_PERMISO,(m.estado.value as UbicacionUi.Error).tipo)
    }
    @Test fun errorInesperadoPermiteReintentar() = runTest(dispatcher) {
        val m=vm(Repo { Result.failure(IllegalStateException("Sin señal")) });m.buscar();runCurrent()
        val e=m.estado.value as UbicacionUi.Error
        assertEquals(TipoError.OTRO,e.tipo);assertEquals("Sin señal",e.mensaje)
    }
    @Test fun pulsacionesRepetidasNoDuplicanSolicitudes() = runTest(dispatcher) {
        val r=Repo { delay(1000);Result.failure(GpsApagadoException()) };val m=vm(r)
        m.buscar();m.buscar();runCurrent();assertEquals(1,r.llamadas)
        assertEquals(UbicacionUi.Buscando,m.estado.value);advanceUntilIdle()
        assertTrue(m.estado.value is UbicacionUi.Error)
    }
    @Test fun sePuedeReintentarTrasUnError() = runTest(dispatcher) {
        val r=Repo { Result.failure(GpsApagadoException()) };val m=vm(r)
        m.buscar();runCurrent();m.buscar();runCurrent();assertEquals(2,r.llamadas)
    }
    @Test fun seleccionInvalidaNoAlteraElEstado() {
        val m=vm(Repo { Result.failure(Exception()) });m.seleccionar(99);assertNull(m.seleccionado.value)
        m.seleccionar(2);assertEquals(2,m.seleccionado.value)
        m.seleccionar(null);assertNull(m.seleccionado.value)
    }
    @Test fun volverATocarElMismoLugarSolicitaRecentraLaCamara() {
        val m=vm(Repo { Result.failure(Exception()) });m.seleccionar(2)
        val revision=m.revisionSeleccion.value;m.seleccionar(2)
        assertTrue(m.revisionSeleccion.value>revision)
    }
}
