package com.leon.campusgps.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leon.campusgps.R
import com.leon.campusgps.model.*
import com.leon.campusgps.util.formatearDistancia
import com.leon.campusgps.viewmodel.MapaViewModel
import kotlinx.coroutines.launch
import java.util.Locale

private data class DatosVista(
    val estado: UbicacionUi, val lugares: List<LugarConDistancia>, val categoria: CategoriaLugar,
    val radio: Int, val seleccionado: Int?, val cercano: Lugar?, val seguimiento: Boolean,
    val ultimo: Lugar?, val posicion: Posicion?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel, mostrarMapa: Boolean = true) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val lugares by vm.lugaresFiltrados.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val radio by vm.radio.collectAsStateWithLifecycle()
    val seleccionado by vm.seleccionado.collectAsStateWithLifecycle()
    val revision by vm.revisionSeleccion.collectAsStateWithLifecycle()
    val cercano by vm.masCercano.collectAsStateWithLifecycle()
    val seguimiento by vm.seguimiento.collectAsStateWithLifecycle()
    val ultimo by vm.ultimoLugar.collectAsStateWithLifecycle()
    val posicion by vm.posicion.collectAsStateWithLifecycle()
    val contorno by vm.contornoRadio.collectAsStateWithLifecycle()
    val modo by vm.modoTema.collectAsStateWithLifecycle()
    val datos=DatosVista(estado,lugares,categoria,radio,seleccionado,cercano,seguimiento,ultimo,posicion)
    val contexto=LocalContext.current
    val scope=rememberCoroutineScope()
    val avisos=remember { SnackbarHostState() }
    var captura by remember { mutableStateOf<CapturaMapa?>(null) }
    var filtros by rememberSaveable { mutableStateOf(false) }
    var temaAbierto by remember { mutableStateOf(false) }
    var seguirTrasPermiso by remember { mutableStateOf(false) }
    var seguirCamara by rememberSaveable { mutableStateOf(true) }
    val oscuro=MaterialTheme.colorScheme.background.luminance()<.5f
    val lanzador=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        if(resultado.values.any { it }) {
            vm.seleccionar(null)
            if(seguirTrasPermiso) vm.iniciarSeguimiento() else vm.buscar()
        } else vm.permisoDenegado()
        seguirTrasPermiso=false
    }
    fun ubicar(continuo: Boolean=false) {
        seguirCamara=true
        val permitido=ContextCompat.checkSelfPermission(contexto,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(contexto,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
        if(permitido) {
            vm.seleccionar(null)
            if(continuo) vm.iniciarSeguimiento() else vm.buscar()
        } else {
            seguirTrasPermiso=continuo
            lanzador.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    LaunchedEffect(vm) { vm.avisos.collect { avisos.showSnackbar(it) } }
    DisposableEffect(vm) {
        val ciclo=ProcessLifecycleOwner.get().lifecycle
        val observador=LifecycleEventObserver { _,evento -> if(evento==Lifecycle.Event.ON_STOP) vm.detenerSeguimiento() }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val altoVentana=maxHeight
        val lateral=maxWidth>=600.dp || maxHeight<520.dp
        val anchoPanel=minOf(360.dp,maxWidth*.43f)
        val alturaPeek=minOf(if(posicion==null)370.dp else 338.dp,maxHeight*.55f)
        val hoja=rememberStandardBottomSheetState(initialValue=SheetValue.PartiallyExpanded,skipHiddenState=true)
        val andamio=rememberBottomSheetScaffoldState(bottomSheetState=hoja)
        val densidad=LocalDensity.current
        val altoPx=with(densidad) { maxHeight.toPx() }
        val panelVisible by remember(hoja,altoPx,alturaPeek,densidad) {
            derivedStateOf {
                val desplazamiento=runCatching { hoja.requireOffset() }.getOrNull()
                if(desplazamiento==null) alturaPeek else with(densidad) { (altoPx-desplazamiento).coerceIn(0f,altoPx).toDp() }
            }
        }
        val expandida=hoja.currentValue==SheetValue.Expanded
        BackHandler(!lateral && expandida && !filtros) { scope.launch { hoja.partialExpand() } }
        val expandir:()->Unit={ scope.launch { if(expandida) hoja.partialExpand() else hoja.expand() } }
        val contenidoMapa: @Composable ()->Unit = {
            Box(Modifier.fillMaxSize()) {
                if(mostrarMapa) MapaUbicacion(estado,lugares,seleccionado,Modifier.fillMaxSize(),revision,vm::seleccionar,
                    cercano?.id,contorno,oscuro,posicion,seguirCamara,{ seguirCamara=false },{ captura=it },
                    margenInferior=if(lateral)20.dp else panelVisible,
                    margenDerecho=if(lateral)anchoPanel+32.dp else 0.dp,margenSuperior=155.dp)
                Box(Modifier.fillMaxWidth().height(170.dp).background(Brush.verticalGradient(listOf(
                    MaterialTheme.colorScheme.background.copy(alpha=.98f),MaterialTheme.colorScheme.background.copy(alpha=.60f),Color.Transparent))))
                Column(Modifier.statusBarsPadding().padding(start=20.dp,top=8.dp,end=if(lateral)anchoPanel+36.dp else 20.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_marca),null,Modifier.size(30.dp),tint=MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("CampusGPS",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                        }
                        Box {
                            IconoCristal(captura,if(oscuro)R.drawable.ic_luna else R.drawable.ic_sol,"Cambiar tema",{ temaAbierto=true },Modifier.size(52.dp))
                            DropdownMenu(temaAbierto,{ temaAbierto=false }) {
                                ModoTema.entries.forEach { opcion ->
                                    DropdownMenuItem(text={ Text(opcion.etiqueta) },onClick={ vm.cambiarTema(opcion);temaAbierto=false },
                                        trailingIcon={ if(modo==opcion) Icon(painterResource(R.drawable.ic_check),null,Modifier.size(20.dp)) })
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    BotonCristal(captura,{ filtros=true },Modifier.width(214.dp).height(52.dp)) {
                        Icon(painterResource(R.drawable.ic_filtros),null,Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("${categoria.etiqueta} · $radio m",style=MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.weight(1f));Icon(painterResource(R.drawable.ic_abajo),null,Modifier.size(16.dp))
                    }
                }
                Column(Modifier.align(Alignment.BottomEnd).padding(
                    end=if(lateral)anchoPanel+36.dp else 20.dp,bottom=if(lateral)32.dp else panelVisible+44.dp),
                    horizontalAlignment=Alignment.End,verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    if(posicion!=null) IconoCristal(captura,R.drawable.ic_ubicacion,"Centrar mi ubicación",{ ubicar() },Modifier.size(60.dp),activo=true,
                        enabled=estado!=UbicacionUi.Buscando || seguimiento)
                    if(posicion!=null || seguimiento) BotonCristal(captura,{
                        if(seguimiento) vm.detenerSeguimiento() else ubicar(true)
                    },Modifier.height(52.dp).width(132.dp),activo=seguimiento) {
                        Icon(painterResource(if(seguimiento)R.drawable.ic_pausa else R.drawable.ic_seguir),null,Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp));Text(if(seguimiento)"Detener" else "Seguir")
                    }
                }
                Surface(Modifier.align(Alignment.BottomStart).padding(start=12.dp,bottom=if(lateral)16.dp else panelVisible+8.dp),
                    color=MaterialTheme.colorScheme.surface.copy(alpha=.94f),shape=RoundedCornerShape(5.dp)) {
                    Text("© OpenStreetMap · OpenMapTiles · OpenFreeMap",fontSize=9.sp,lineHeight=12.sp,
                        modifier=Modifier.clickable { contexto.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://openfreemap.org/"))) }.padding(horizontal=5.dp,vertical=3.dp))
                }
            }
        }

        if(lateral) {
            contenidoMapa()
            SuperficieCristal(captura,Modifier.align(Alignment.CenterEnd).statusBarsPadding().navigationBarsPadding()
                .padding(top=12.dp,end=16.dp,bottom=12.dp).width(anchoPanel).fillMaxHeight(),mate=.94f) {
                PanelLugares(datos,vm,captura,altoVentana-100.dp,{ ubicar() },{ filtros=true },{},true)
            }
            SnackbarHost(avisos,Modifier.align(Alignment.BottomStart).padding(16.dp))
        } else {
            BottomSheetScaffold(scaffoldState=andamio,sheetPeekHeight=alturaPeek,
                sheetContainerColor=Color.Transparent,sheetShadowElevation=0.dp,sheetTonalElevation=0.dp,
                sheetShape=RoundedCornerShape(topStart=30.dp,topEnd=30.dp),sheetDragHandle=null,
                containerColor=Color.Transparent,snackbarHost={ SnackbarHost(avisos) },sheetContent={
                    SuperficieCristal(captura,Modifier.fillMaxWidth(),radio=30.dp,mate=.94f) {
                        PanelLugares(datos,vm,captura,altoVentana-155.dp,{ ubicar() },{ filtros=true },expandir,expandida)
                    }
                }) { contenidoMapa() }
        }
    }
    if(filtros) FiltrosCampus(vm,categoria,radio,captura) { filtros=false }
}

@Composable
private fun PanelLugares(datos: DatosVista,vm: MapaViewModel,captura: CapturaMapa?,altoMaximo: Dp,
    ubicar: ()->Unit,filtrar: ()->Unit,expandir: ()->Unit,expandida: Boolean) {
    Column(Modifier.fillMaxWidth().heightIn(max=altoMaximo).navigationBarsPadding().padding(horizontal=20.dp)
        .then(if(datos.posicion==null)Modifier.verticalScroll(rememberScrollState()) else Modifier)) {
        Box(Modifier.fillMaxWidth().height(19.dp),contentAlignment=Alignment.Center) {
            Box(Modifier.width(36.dp).height(4.dp).background(MaterialTheme.colorScheme.outlineVariant,RoundedCornerShape(2.dp)))
        }
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable(onClick=expandir),verticalAlignment=Alignment.CenterVertically) {
            Text("Cerca de ti",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
            Text("${datos.lugares.size} lugares",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick=expandir) { Icon(painterResource(R.drawable.ic_abajo),if(expandida)"Contraer lugares" else "Mostrar todos los lugares",Modifier.size(20.dp).rotate(if(expandida)0f else 180f)) }
        }
        if(datos.estado is UbicacionUi.Error) {
            MensajeError(datos.estado,ubicar)
            Spacer(Modifier.height(16.dp))
        } else if(datos.posicion==null) {
            Image(painterResource(R.drawable.campus_vista),null,Modifier.fillMaxWidth().height(92.dp))
            Text(if(datos.estado==UbicacionUi.Buscando)"Buscando tu ubicación" else "Activa tu ubicación",
                style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
            Text("Encuentra los lugares que quedan más cerca.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Button(onClick=ubicar,enabled=datos.estado!=UbicacionUi.Buscando,modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(27.dp)) {
                Icon(painterResource(R.drawable.ic_ubicacion),null,Modifier.size(22.dp));Spacer(Modifier.width(10.dp))
                Text(if(datos.estado==UbicacionUi.Buscando)"Buscando…" else "Obtener mi ubicación")
            }
            TextButton(onClick={},enabled=false,modifier=Modifier.align(Alignment.CenterHorizontally).heightIn(min=48.dp)) { Text("Ir al más cercano") }
        } else {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Row(Modifier.weight(1f),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary,CircleShape));Spacer(Modifier.width(6.dp))
                    Text(if(datos.seguimiento)"En vivo" else "Ubicación lista",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick={ datos.cercano?.let { vm.seleccionar(it.id) } },enabled=datos.cercano!=null) {
                    Text("Ir al más cercano",style=MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(6.dp));Icon(painterResource(R.drawable.ic_flecha),null,Modifier.size(18.dp))
                }
            }
            if(datos.estado==UbicacionUi.Buscando) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
            ListaLugares(datos.lugares,datos.seleccionado,vm::seleccionar,
                Modifier.fillMaxWidth().height(minOf((datos.lugares.size*78).dp,(altoMaximo-165.dp).coerceAtLeast(130.dp))),datos.cercano?.id)
            Text("Tu ubicación: ${String.format(Locale.US,"%.5f",datos.posicion.latitud)}, ${String.format(Locale.US,"%.5f",datos.posicion.longitud)}",
                style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,
                modifier=Modifier.padding(top=8.dp,bottom=12.dp))
        }
        if(datos.posicion==null && datos.ultimo!=null) TextButton(onClick=vm::volverAlUltimoLugar) {
            Icon(painterResource(R.drawable.ic_reciente),null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Último: ${datos.ultimo.nombre}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable
private fun FiltrosCampus(vm: MapaViewModel,categoria: CategoriaLugar,radio: Int,captura: CapturaMapa?,cerrar: ()->Unit) {
    ModalBottomSheet(onDismissRequest=cerrar,containerColor=MaterialTheme.colorScheme.surface,
        dragHandle={ BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal=24.dp).navigationBarsPadding()) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("Mostrar lugares",Modifier.weight(1f),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                IconButton(onClick=cerrar) { Icon(painterResource(R.drawable.ic_cerrar),"Cerrar filtros",Modifier.size(22.dp)) }
            }
            Spacer(Modifier.height(20.dp))
            Text("Categoría",style=MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                CategoriaLugar.entries.forEach { opcion ->
                    FilterChip(categoria==opcion,{ vm.filtrar(opcion) },label={ Text(opcion.etiqueta) },
                        leadingIcon={ if(categoria==opcion) Icon(painterResource(R.drawable.ic_check),null,Modifier.size(18.dp)) },
                        modifier=Modifier.heightIn(min=48.dp))
                }
            }
            Spacer(Modifier.height(18.dp));Text("Radio de cercanía",style=MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().height(54.dp)) {
                val opciones=listOf(100,200,500)
                val ancho=maxWidth/3
                val izquierda by animateDpAsState(ancho*opciones.indexOf(radio).coerceAtLeast(0),spring(dampingRatio=.78f,stiffness=500f),label="radioCristal")
                SuperficieCristal(captura,Modifier.offset(x=izquierda).width(ancho).fillMaxHeight(),activo=true) {}
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxSize()) {
                    opciones.forEachIndexed { indice,metros ->
                        SegmentedButton(selected=radio==metros,onClick={ vm.cambiarRadio(metros) },
                            shape=SegmentedButtonDefaults.itemShape(indice,3),
                            colors=SegmentedButtonDefaults.colors(activeContainerColor=Color.Transparent,inactiveContainerColor=Color.Transparent,
                                activeBorderColor=Color.Transparent,inactiveBorderColor=Color.Transparent,
                                activeContentColor=MaterialTheme.colorScheme.primary,inactiveContentColor=MaterialTheme.colorScheme.onSurface),
                            icon={}) { Text("$metros m") }
                    }
                }
            }
            Text("Los lugares fuera del radio siguen visibles.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,
                modifier=Modifier.padding(top=10.dp,bottom=24.dp))
            Button(onClick=cerrar,modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(27.dp)) { Text("Ver lugares") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
