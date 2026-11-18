package com.leon.campusgps

import com.leon.campusgps.data.*
import com.leon.campusgps.model.*
import com.leon.campusgps.viewmodel.MapaViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.*

@OptIn(ExperimentalCoroutinesApi::class)
class SeguimientoTest {
    private val dispatcher=StandardTestDispatcher()
    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun terminar() { Dispatchers.resetMain() }
    private class Repo : UbicacionRepository {
        val eventos=MutableSharedFlow<Posicion>(extraBufferCapacity=4)
        var inicios=0;var paradas=0
        override fun playServicesDisponible()=true
        override suspend fun obtenerPosicion()=Result.success(Posicion(-12.0464,-77.0428))
        override fun observarPosiciones()=eventos.onStart { inicios++ }.onCompletion { paradas++ }
    }
    @Test fun iniciarDosVecesMantieneUnaSolaSuscripcionYPararLaLimpia() = runTest(dispatcher) {
        val repo=Repo(); val vm=MapaViewModel(repo,LugaresRepositoryLocal())
        vm.iniciarSeguimiento();vm.iniciarSeguimiento();runCurrent()
        assertEquals(1,repo.inicios);assertTrue(vm.seguimiento.value)
        vm.detenerSeguimiento();runCurrent()
        assertEquals(1,repo.paradas);assertFalse(vm.seguimiento.value)
        assertEquals(UbicacionUi.Inicial,vm.estado.value)
    }
    @Test fun unaSegundaPosicionLlegaSinVolverAPulsarBuscar() = runTest(dispatcher) {
        val repo=Repo()
        // Esta prueba JVM comprueba el Flow. Las distancias Android se prueban en el emulador.
        val sinLugares = object : LugaresRepository { override fun obtenerLugares() = emptyList<Lugar>() }
        val vm=MapaViewModel(repo,sinLugares)
        vm.iniciarSeguimiento();runCurrent()
        val uno=Posicion(-12.0464,-77.0428);repo.eventos.emit(uno);runCurrent()
        assertEquals(uno,(vm.estado.value as UbicacionUi.Lista).posicion)
        val dos=Posicion(-12.048,-77.044);repo.eventos.emit(dos);runCurrent()
        assertEquals(dos,(vm.estado.value as UbicacionUi.Lista).posicion)
        vm.detenerSeguimiento();runCurrent()
        assertEquals(dos,(vm.estado.value as UbicacionUi.Lista).posicion)
    }
    @Test fun sinPrimeraPosicionElTiempoDeEsperaTerminaConError() = runTest(dispatcher) {
        val repo=Repo();val vm=MapaViewModel(repo,LugaresRepositoryLocal())
        vm.iniciarSeguimiento();runCurrent();advanceTimeBy(15_001);runCurrent()
        assertFalse(vm.seguimiento.value)
        assertTrue(vm.estado.value is UbicacionUi.Error)
        assertEquals(1,repo.paradas)
    }
}
