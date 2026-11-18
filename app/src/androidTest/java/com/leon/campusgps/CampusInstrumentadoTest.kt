package com.leon.campusgps

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leon.campusgps.data.*
import com.leon.campusgps.model.*
import com.leon.campusgps.ui.PantallaMapa
import com.leon.campusgps.ui.MensajeError
import com.leon.campusgps.ui.theme.CampusGPSTheme
import com.leon.campusgps.util.distanciaMetros
import com.leon.campusgps.viewmodel.MapaViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampusInstrumentadoTest {
    @get:Rule val pantalla = createComposeRule()
    private class Repo : UbicacionRepository {
        var posicion=Posicion(-12.0464,-77.0428)
        var fallo: Exception?=null
        var disponible=true
        override fun playServicesDisponible()=disponible
        override fun observarPosiciones() = kotlinx.coroutines.flow.flow { emit(obtenerPosicion().getOrThrow()) }
        override suspend fun obtenerPosicion(): Result<Posicion> = fallo?.let { Result.failure(it) } ?: Result.success(posicion)
    }
    private fun mostrar(repo: Repo=Repo()): MapaViewModel {
        val vm=MapaViewModel(repo,LugaresRepositoryLocal())
        pantalla.setContent { CampusGPSTheme { PantallaMapa(vm, mostrarMapa = false) } }
        return vm
    }
    private fun buscar(vm: MapaViewModel) {
        pantalla.runOnIdle { vm.buscar() }
        pantalla.waitUntil(10000) { vm.estado.value is UbicacionUi.Lista || vm.estado.value is UbicacionUi.Error }
        pantalla.waitForIdle()
    }
    @Test fun distanciaCeroEnElMismoPunto() {
        val l=LugaresRepositoryLocal().obtenerLugares().first()
        assertEquals(0f,distanciaMetros(Posicion(l.latitud,l.longitud),l),0.1f)
    }
    @Test fun distanciasDelPuntoBaseSeOrdenanCorrectamente() {
        val vm=mostrar();buscar(vm)
        assertEquals(listOf(1,2,3,4,5),vm.lugares.value.map { it.lugar.id })
        assertTrue(vm.lugares.value.all { it.metros!! > 0 })
        assertTrue(vm.lugares.value.first().metros!! in 70f..110f)
        pantalla.onNodeWithText("Latitud: -12.0464").assertIsDisplayed()
    }
    @Test fun masCercanoEstaDeshabilitadoSinUbicacion() {
        mostrar();pantalla.onNodeWithText("Ir al más cercano").assertIsNotEnabled()
    }
    @Test fun botonMasCercanoSeleccionaLaBiblioteca() {
        val vm=mostrar();buscar(vm)
        pantalla.onNodeWithText("Ir al más cercano").assertIsEnabled().performClick()
        pantalla.runOnIdle { assertEquals(1,vm.seleccionado.value) }
    }
    @Test fun tocarLaTarjetaSeleccionaSuId() {
        val vm=mostrar();buscar(vm)
        pantalla.onNodeWithText("Cafetería Campus").performClick()
        pantalla.runOnIdle { assertEquals(2,vm.seleccionado.value) }
    }
    @Test fun nuevaUbicacionCambiaOrdenYCercano() {
        val repo=Repo();val vm=mostrar(repo);buscar(vm)
        repo.posicion=Posicion(-12.0480,-77.0440);buscar(vm)
        assertEquals(4,vm.lugares.value.first().lugar.id)
        pantalla.waitUntil { vm.masCercano.value?.id == 4 }
        assertEquals(0f,vm.lugares.value.first().metros!!,0.1f)
    }
    @Test fun errorSinPermisoOfreceAbrirAjustes() {
        val vm=mostrar();pantalla.runOnIdle { vm.permisoDenegado() }
        pantalla.onNodeWithText("Abrir ajustes").assertIsDisplayed()
    }
    @Test fun errorGPSOfreceReintentar() {
        val repo=Repo();repo.fallo=GpsApagadoException();val vm=mostrar(repo);buscar(vm)
        pantalla.onNodeWithText("Reintentar").assertIsDisplayed()
        pantalla.onNodeWithText("Activa el GPS e intenta de nuevo").assertIsDisplayed()
    }
    @Test fun sinPlayServicesNoOfreceReintentar() {
        val repo=Repo();repo.disponible=false;val vm=mostrar(repo);buscar(vm)
        pantalla.onNodeWithText("Este dispositivo no tiene Google Play Services").assertIsDisplayed()
        pantalla.onNodeWithText("Reintentar").assertDoesNotExist()
    }

}
