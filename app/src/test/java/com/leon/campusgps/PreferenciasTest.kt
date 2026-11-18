package com.leon.campusgps

import com.leon.campusgps.data.*
import com.leon.campusgps.model.*
import com.leon.campusgps.viewmodel.MapaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenciasTest {
    private val dispatcher=StandardTestDispatcher()
    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun terminar() { Dispatchers.resetMain() }
    private val ubicacion=object : UbicacionRepository {
        override fun playServicesDisponible()=true
        override suspend fun obtenerPosicion()=Result.failure<Posicion>(Exception())
        override fun observarPosiciones()=emptyFlow<Posicion>()
    }
    @Test fun seleccionarPersisteYUnNuevoViewModelRecuperaElLugar() = runTest(dispatcher) {
        val prefs=PreferenciasEnMemoria()
        val primero=MapaViewModel(ubicacion,LugaresRepositoryLocal(),prefs)
        runCurrent();primero.seleccionar(2);runCurrent()
        val segundo=MapaViewModel(ubicacion,LugaresRepositoryLocal(),prefs);runCurrent()
        assertEquals(2,segundo.seleccionado.value)
        assertEquals("Cafetería Campus",segundo.ultimoLugar.value?.nombre)
    }
    @Test fun unIdAntiguoQueYaNoExisteNoSeRestaura() = runTest(dispatcher) {
        val vm=MapaViewModel(ubicacion,LugaresRepositoryLocal(),PreferenciasEnMemoria(999));runCurrent()
        assertNull(vm.seleccionado.value);assertNull(vm.ultimoLugar.value)
    }
    @Test fun volverAlUltimoLugarAbreLaCategoriaNecesaria() = runTest(dispatcher) {
        val vm=MapaViewModel(ubicacion,LugaresRepositoryLocal(),PreferenciasEnMemoria(2));runCurrent()
        vm.filtrar(CategoriaLugar.ESTUDIO);runCurrent()
        vm.volverAlUltimoLugar();runCurrent()
        assertEquals(CategoriaLugar.TODOS,vm.categoria.value)
        assertEquals(2,vm.seleccionado.value)
    }
}
