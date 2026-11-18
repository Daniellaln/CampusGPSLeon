package com.leon.campusgps.ui

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

data class CapturaMapa(val bitmap: Bitmap, val tamano: IntSize, val origen: Offset = Offset.Zero)

private const val LENTE_CAMPUS = """
uniform shader fondo;
uniform float2 tamano;
uniform float2 origen;
uniform float2 escala;
uniform float radio;
uniform float borde;
uniform float fuerza;
uniform float noche;
uniform float mate;
uniform float activo;
half4 main(float2 p) {
    float2 c = p - tamano * .5;
    float2 q = abs(c) - (tamano * .5 - radio);
    float d = length(max(q, 0.0)) + min(max(q.x,q.y),0.0) - radio;
    float2 n = normalize(max(q, 0.0) * sign(c) + float2(.0001));
    float e = 1.0 - smoothstep(0.0, borde, -d);
    half3 tinte = mix(half3(.985,1.0,.993),half3(.055,.115,.1),half(noche));
    tinte = mix(tinte,half3(.13,.61,.49),half(activo * .14));
    if (mate > .75 && d < -borde) return half4(tinte * .965,.965);
    float2 muestra = (origen + p - n * (e*e*fuerza)) * escala;
    float2 paso = escala * .8;
    half4 base = fondo.eval(muestra) * .6;
    base += fondo.eval(muestra + float2(paso.x,0.0)) * .1;
    base += fondo.eval(muestra - float2(paso.x,0.0)) * .1;
    base += fondo.eval(muestra + float2(0.0,paso.y)) * .1;
    base += fondo.eval(muestra - float2(0.0,paso.y)) * .1;
    base.r = mix(base.r, fondo.eval(muestra + n*escala*e*1.2).r, half(e*.35));
    base.b = mix(base.b, fondo.eval(muestra - n*escala*e*1.2).b, half(e*.35));
    half3 color = mix(base.rgb,tinte,half(.17 + mate*.68));
    float luz = .25 + .65 * max(dot(n,normalize(float2(-1.0,-1.3))),0.0);
    float reflejo = (1.0-smoothstep(.0,1.5,abs(d+1.2))) * luz;
    color = min(color + half3(reflejo*.65),half3(1.0));
    half alpha = half(min(.97,.22+mate*.7+e*.58));
    return half4(color*alpha,alpha);
}
"""

@RequiresApi(33)
private class LenteRuntime {
    val shader = RuntimeShader(LENTE_CAMPUS)
    val pintura = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = this@LenteRuntime.shader }
}

@Composable
fun SuperficieCristal(
    captura: CapturaMapa?, modifier: Modifier = Modifier, radio: Dp = 28.dp,
    mate: Float = .12f, activo: Boolean = false, content: @Composable BoxScope.() -> Unit
) {
    val oscuro = MaterialTheme.colorScheme.background.luminance() < .5f
    val lente = if (Build.VERSION.SDK_INT >= 33) remember {
        runCatching { LenteRuntime() }.onFailure { Log.w("CristalCampus", "Alternativa sin shader: ${it.message}") }.getOrNull()
    } else null
    var origen by remember { mutableStateOf(Offset.Zero) }
    val forma = RoundedCornerShape(radio)
    Box(modifier.graphicsLayer {
        shape = forma; shadowElevation = if (mate > .7f) 10.dp.toPx() else 7.dp.toPx()
        ambientShadowColor = Color(0xFF173F35).copy(alpha = .12f)
        spotShadowColor = Color(0xFF173F35).copy(alpha = .16f)
    }) {
        Canvas(Modifier.matchParentSize().onGloballyPositioned { origen = it.positionInRoot() }) {
            val redondeo = minOf(radio.toPx(), size.minDimension/2)
            if (Build.VERSION.SDK_INT >= 33 && lente != null && captura != null && !captura.bitmap.isRecycled) {
                val s=lente.shader
                s.setInputShader("fondo",BitmapShader(captura.bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP))
                s.setFloatUniform("tamano",size.width,size.height)
                s.setFloatUniform("origen",origen.x-captura.origen.x,origen.y-captura.origen.y)
                s.setFloatUniform("escala",captura.bitmap.width/captura.tamano.width.toFloat(),captura.bitmap.height/captura.tamano.height.toFloat())
                s.setFloatUniform("radio",redondeo)
                s.setFloatUniform("borde",14.dp.toPx())
                s.setFloatUniform("fuerza",8.dp.toPx())
                s.setFloatUniform("noche",if(oscuro)1f else 0f)
                s.setFloatUniform("mate",mate)
                s.setFloatUniform("activo",if(activo)1f else 0f)
                drawContext.canvas.nativeCanvas.drawRoundRect(0f,0f,size.width,size.height,redondeo,redondeo,lente.pintura)
            } else {
                val base=if(oscuro)Color(0xFF173B32) else Color(0xFFF5FFFA)
                drawRoundRect(Brush.linearGradient(listOf(base.copy(alpha = if(mate>.7f).97f else .85f),
                    base.copy(alpha = if(mate>.7f).94f else .55f))),cornerRadius=CornerRadius(redondeo))
            }
            drawRoundRect(Brush.linearGradient(listOf(Color.White.copy(alpha=if(oscuro).32f else .95f),
                Color.White.copy(alpha=.08f),Color.White.copy(alpha=if(oscuro).18f else .6f))),
                cornerRadius=CornerRadius(redondeo),style=Stroke(1.dp.toPx()))
        }
        content()
    }
}

@Composable
fun BotonCristal(
    captura: CapturaMapa?, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, activo: Boolean = false, content: @Composable RowScope.() -> Unit
) {
    val interacciones=remember { MutableInteractionSource() }
    val presionado by interacciones.collectIsPressedAsState()
    val escala by animateFloatAsState(if(presionado).945f else 1f,
        spring(dampingRatio=.78f,stiffness=650f),label="presionCristal")
    SuperficieCristal(captura,modifier.graphicsLayer { scaleX=escala;scaleY=escala;alpha=if(enabled)1f else .48f },
        radio=32.dp,activo=activo) {
        Button(onClick=onClick,enabled=enabled,interactionSource=interacciones,
            modifier=Modifier.fillMaxSize(),shape=RoundedCornerShape(32.dp),
            contentPadding=PaddingValues(horizontal=18.dp),
            colors=ButtonDefaults.buttonColors(containerColor=Color.Transparent,
                contentColor=if(activo)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                disabledContainerColor=Color.Transparent,disabledContentColor=MaterialTheme.colorScheme.onSurfaceVariant),content=content)
    }
}

@Composable
fun IconoCristal(captura: CapturaMapa?, @DrawableRes icono: Int, descripcion: String,
    onClick: () -> Unit, modifier: Modifier = Modifier.size(56.dp), activo: Boolean = false, enabled: Boolean = true) {
    BotonCristal(captura,onClick,modifier,enabled,activo) {
        Icon(painterResource(icono),contentDescription=descripcion,modifier=Modifier.size(24.dp))
    }
}
