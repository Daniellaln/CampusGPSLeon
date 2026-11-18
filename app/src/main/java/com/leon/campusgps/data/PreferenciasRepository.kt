package com.leon.campusgps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface PreferenciasRepository {
    val ultimoLugarId: Flow<Int?>
    suspend fun guardarUltimoLugar(id: Int)
}

// Permite probar el ViewModel sin Context, archivos ni una instancia real de Android.
class PreferenciasEnMemoria(inicial: Int? = null) : PreferenciasRepository {
    private val valor = MutableStateFlow(inicial)
    override val ultimoLugarId: Flow<Int?> = valor
    override suspend fun guardarUltimoLugar(id: Int) { require(id > 0); valor.value = id }
}
