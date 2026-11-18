package com.leon.campusgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leon.campusgps.model.ModoTema
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leon.campusgps.ui.PantallaMapa
import com.leon.campusgps.ui.theme.CampusGPSTheme
import com.leon.campusgps.viewmodel.CampusViewModelFactory
import com.leon.campusgps.viewmodel.MapaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MapaViewModel = viewModel(factory = CampusViewModelFactory(applicationContext))
            val modo by vm.modoTema.collectAsStateWithLifecycle()
            val oscuro = when (modo) { ModoTema.SISTEMA -> isSystemInDarkTheme(); ModoTema.OSCURO -> true; ModoTema.CLARO -> false }
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { oscuro },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { oscuro })
            }
            CampusGPSTheme(oscuro) {
                PantallaMapa(vm)
            }
        }
    }
}
