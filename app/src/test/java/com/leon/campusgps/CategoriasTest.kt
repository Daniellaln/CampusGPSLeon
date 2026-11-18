package com.leon.campusgps

import com.leon.campusgps.data.LugaresRepositoryLocal
import com.leon.campusgps.model.CategoriaLugar
import org.junit.Assert.*
import org.junit.Test

class CategoriasTest {
    private val lugares = LugaresRepositoryLocal().obtenerLugares()

    @Test fun todosConservaLosCincoLugares() {
        assertEquals(5, lugares.count(CategoriaLugar.TODOS::incluye))
    }
    @Test fun estudioIncluyeSoloBibliotecaYLaboratorio() {
        assertEquals(listOf(1, 3), lugares.filter(CategoriaLugar.ESTUDIO::incluye).map { it.id })
    }
    @Test fun cadaLugarPerteneceAUnaCategoriaEspecifica() {
        lugares.forEach { lugar ->
            assertEquals(1, CategoriaLugar.entries.filter { it != CategoriaLugar.TODOS }.count { it.incluye(lugar) })
        }
    }
}
