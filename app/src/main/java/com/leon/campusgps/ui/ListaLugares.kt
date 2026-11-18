package com.leon.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leon.campusgps.model.LugarConDistancia
import com.leon.campusgps.util.formatearDistancia

@Composable
fun ListaLugares(lugares: List<LugarConDistancia>, seleccionado: Int?, onClick: (Int) -> Unit,
    modifier: Modifier = Modifier) {
    LazyColumn(modifier, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(lugares, key = { it.lugar.id }) { item ->
            val elegida = item.lugar.id == seleccionado
            Card(onClick = { onClick(item.lugar.id) }, modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (elegida) Color(0xFFD3EAF7) else MaterialTheme.colorScheme.surfaceVariant),
                border = if (elegida) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(Color(0xFF3183C5), CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.lugar.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(item.lugar.categoria, color = Color(0xFF687785), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(item.metros?.let(::formatearDistancia) ?: "Sin calcular", color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
