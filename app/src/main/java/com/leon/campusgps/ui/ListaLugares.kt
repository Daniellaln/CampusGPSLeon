package com.leon.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leon.campusgps.model.LugarConDistancia
import com.leon.campusgps.util.formatearDistancia
import com.leon.campusgps.R

@Composable
fun ListaLugares(lugares: List<LugarConDistancia>, seleccionado: Int?, onClick: (Int) -> Unit,
    modifier: Modifier = Modifier, masCercanoId: Int? = null) {
    val lista = rememberLazyListState()
    LaunchedEffect(seleccionado,lugares) {
        val indice = lugares.indexOfFirst { it.lugar.id == seleccionado }
        if (indice >= 0 && !lista.isScrollInProgress) lista.scrollToItem(indice)
    }
    LazyColumn(modifier,state=lista,contentPadding=PaddingValues(vertical=4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        items(lugares, key = { it.lugar.id }) { item ->
            val elegida = item.lugar.id == seleccionado
            val cercana = item.lugar.id == masCercanoId
            Surface(color=if(elegida)MaterialTheme.colorScheme.primaryContainer.copy(alpha=.8f) else Color.Transparent,
                shape=MaterialTheme.shapes.medium) {
                Row(Modifier.fillMaxWidth().heightIn(min=72.dp).clickable(role=Role.Button) { onClick(item.lugar.id) }
                    .semantics { selected=elegida;stateDescription=when(item.dentroDelRadio){true->"Dentro del radio";false->"Fuera del radio";null->"Distancia pendiente"} }
                    .padding(horizontal=10.dp,vertical=10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(when(item.lugar.categoria){"Estudio"->R.drawable.ic_estudio;"Comida"->R.drawable.ic_comida;"Eventos"->R.drawable.ic_eventos;else->R.drawable.ic_servicios}),
                        null,Modifier.size(25.dp),tint=if(cercana)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.lugar.nombre,fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.bodyLarge,
                            maxLines=2,overflow=TextOverflow.Ellipsis,color=if(cercana)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        Text(item.lugar.categoria+when(item.dentroDelRadio){true->" · En el radio";false->" · Fuera del radio";null->""},
                            color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall,maxLines=2)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(item.metros?.let(::formatearDistancia) ?: "—",color=MaterialTheme.colorScheme.onSurface,
                        fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
