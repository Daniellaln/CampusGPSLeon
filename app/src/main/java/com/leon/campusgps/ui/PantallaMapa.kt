package com.leon.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leon.campusgps.model.UbicacionUi
import com.leon.campusgps.model.CategoriaLugar
import com.leon.campusgps.viewmodel.MapaViewModel
import com.leon.campusgps.util.formatearDistancia
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel, mostrarMapa: Boolean = true) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val lugares by vm.lugaresFiltrados.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val radio by vm.radio.collectAsStateWithLifecycle()
    val contorno by vm.contornoRadio.collectAsStateWithLifecycle()
    val seleccionado by vm.seleccionado.collectAsStateWithLifecycle()
    val revision by vm.revisionSeleccion.collectAsStateWithLifecycle()
    val masCercano by vm.masCercano.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }
    fun alPulsar() {
        val fina = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val aproximada = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fina || aproximada) vm.buscar()
        else lanzador.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("CampusGPS") }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary))
    }) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val compacta = maxHeight < 430.dp
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoriaLugar.entries.forEach { opcion ->
                    FilterChip(selected = categoria == opcion, onClick = { vm.filtrar(opcion) },
                        label = { Text(opcion.etiqueta) })
                }
            }
            if (estado !is UbicacionUi.Error) {
            if (mostrarMapa) MapaUbicacion(
                estado = estado,
                lugares = lugares,
                seleccionado = seleccionado,
                modifier = Modifier.fillMaxWidth().weight(1f),
                revisionSeleccion = revision,
                onSeleccionar = vm::seleccionar,
                masCercanoId = masCercano?.id,
                contornoRadio = contorno
            ) else Spacer(Modifier.fillMaxWidth().weight(1f))
            }
            Button(onClick = ::alPulsar, enabled = estado != UbicacionUi.Buscando,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(if (estado == UbicacionUi.Buscando) "Buscando…" else "Obtener mi ubicación")
            }
            when (val actual = estado) {
                UbicacionUi.Inicial -> Text("Pulsa el botón para empezar", style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp))
                UbicacionUi.Buscando -> LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                is UbicacionUi.Lista -> {
                    if (!compacta) {
                    Text("Latitud: ${actual.posicion.latitud}", style = MaterialTheme.typography.bodySmall)
                    Text("Longitud: ${actual.posicion.longitud}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is UbicacionUi.Error -> MensajeError(actual, ::alPulsar)
            }
            if (estado !is UbicacionUi.Error) {
            OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), 
                onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                enabled = masCercano != null && estado != UbicacionUi.Buscando,
                modifier = Modifier.padding(top = 2.dp)
            ) { Text("Ir al más cercano") }
            masCercano?.let { lugar ->
                val metros = lugares.firstOrNull { it.lugar.id == lugar.id }?.metros
                Text("Más cercano: ${lugar.nombre} (${metros?.let(::formatearDistancia) ?: "Sin calcular"})",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(100, 200, 500).forEach { metros ->
                    FilterChip(selected = radio == metros, onClick = { vm.cambiarRadio(metros) },
                        label = { Text("$metros m") })
                }
            }
            ListaLugares(lugares, seleccionado, vm::seleccionar,
                modifier = Modifier.fillMaxWidth().height(if (compacta) 96.dp else 280.dp), masCercanoId = masCercano?.id)
            }
        }
        }
    }
}
