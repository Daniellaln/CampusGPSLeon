package com.leon.campusgps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.leon.campusgps.model.ModoTema

interface PreferenciasRepository {
    val ultimoLugarId: Flow<Int?>
    suspend fun guardarUltimoLugar(id: Int)
    val modoTema: Flow<ModoTema>
    suspend fun guardarTema(modo: ModoTema)
}

// Permite probar el ViewModel sin Context, archivos ni una instancia real de Android.
class PreferenciasEnMemoria(inicial: Int? = null, temaInicial: ModoTema = ModoTema.SISTEMA) : PreferenciasRepository {
    private val valor = MutableStateFlow(inicial)
    override val ultimoLugarId: Flow<Int?> = valor
    override suspend fun guardarUltimoLugar(id: Int) { require(id > 0); valor.value = id }
    private val tema = MutableStateFlow(temaInicial)
    override val modoTema: Flow<ModoTema> = tema
    override suspend fun guardarTema(modo: ModoTema) { tema.value = modo }
}
