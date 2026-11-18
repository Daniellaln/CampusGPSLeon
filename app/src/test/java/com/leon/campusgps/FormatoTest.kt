package com.leon.campusgps

import com.leon.campusgps.util.formatearDistancia
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatoTest {
    @Test fun cero() = assertEquals("0 m", formatearDistancia(0f))
    @Test fun metros() = assertEquals("350 m", formatearDistancia(350.2f))
    @Test fun limiteEnMetros() = assertEquals("999 m", formatearDistancia(999f))
    @Test fun kilometro() = assertEquals("1.0 km", formatearDistancia(1000f))
    @Test fun decimal() = assertEquals("1.5 km", formatearDistancia(1500f))
}
