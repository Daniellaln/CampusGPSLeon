package com.leon.campusgps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leon.campusgps.model.UbicacionUi
import com.leon.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel, mostrarMapa: Boolean = true) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    fun alPulsar() = vm.buscar()
    Scaffold(topBar = {
        TopAppBar(title = { Text("CampusGPS") }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary))
    }) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val compacta = maxHeight < 430.dp
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                is UbicacionUi.Error -> Text(actual.mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
        }
        }
    }
}
