package com.leon.campusgps

import com.leon.campusgps.model.Posicion
import com.leon.campusgps.model.CampusTecsup
import com.leon.campusgps.util.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class RadioTest {
    @Test fun elLimiteSeConsideraDentroYLoDesconocidoNoSeInventa() {
        assertEquals(true, estaDentroDelRadio(100f,100))
        assertEquals(false, estaDentroDelRadio(100.01f,100))
        assertNull(estaDentroDelRadio(null,200))
    }
    @Test fun elPoligonoCierraYConservaElRadioEnCualquierDireccion() {
        val centro=Posicion(CampusTecsup.LATITUD,CampusTecsup.LONGITUD)
        val puntos=contornoDelRadio(centro,500)
        assertEquals(65,puntos.size)
        assertEquals(puntos.first().latitud,puntos.last().latitud,1e-8)
        assertEquals(puntos.first().longitud,puntos.last().longitud,1e-8)
        puntos.forEach { p ->
            val lat1=Math.toRadians(centro.latitud); val lat2=Math.toRadians(p.latitud)
            val a=sin((lat2-lat1)/2).pow(2)+cos(lat1)*cos(lat2)*sin(Math.toRadians(p.longitud-centro.longitud)/2).pow(2)
            assertEquals(500.0,6_371_000*2*atan2(sqrt(a),sqrt(1-a)),.1)
        }
    }
}
