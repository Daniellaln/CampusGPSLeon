package com.leon.campusgps.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.leon.campusgps.model.*
import com.leon.campusgps.R

@Composable
fun MensajeError(error: UbicacionUi.Error, reintentar: () -> Unit, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    Column(modifier.padding(vertical=18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Icon(painterResource(if(error.tipo==TipoError.SIN_PERMISO)R.drawable.ic_permiso else R.drawable.ic_gps_error),null,
            Modifier.size(40.dp),tint=MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(when(error.tipo){TipoError.SIN_PERMISO->"Permite tu ubicación";TipoError.GPS_APAGADO->"Activa la ubicación";else->"No pudimos localizarte"},
            style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
        Text(error.mensaje,Modifier.fillMaxWidth().padding(vertical=10.dp),textAlign=TextAlign.Center,
            color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium)
        when (error.tipo) {
            TipoError.SIN_PERMISO -> OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), onClick = {
                contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", contexto.packageName, null)))
            }) { Text("Abrir ajustes") }
            TipoError.GPS_APAGADO, TipoError.OTRO -> OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), onClick = reintentar) { Text("Reintentar") }
            TipoError.SIN_PLAY_SERVICES -> Unit
        }
    }
}
