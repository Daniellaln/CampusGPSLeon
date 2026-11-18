package com.leon.campusgps.util

import com.leon.campusgps.model.Posicion
import kotlin.math.*

fun estaDentroDelRadio(metros: Float?, radio: Int): Boolean? = metros?.let { it <= radio }

// Contorno geodésico cerrado; evita confundir grados de latitud con metros.
fun contornoDelRadio(centro: Posicion, metros: Int): List<Posicion> {
    require(metros > 0)
    val lat = Math.toRadians(centro.latitud)
    val lon = Math.toRadians(centro.longitud)
    val distancia = metros / 6_371_000.0
    return (0..64).map { indice ->
        val rumbo = indice * 2 * PI / 64
        val nuevaLat = asin(sin(lat) * cos(distancia) + cos(lat) * sin(distancia) * cos(rumbo))
        val nuevaLon = lon + atan2(sin(rumbo) * sin(distancia) * cos(lat), cos(distancia) - sin(lat) * sin(nuevaLat))
        Posicion(Math.toDegrees(nuevaLat), Math.toDegrees(nuevaLon))
    }
}
