package com.leon.campusgps.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.leon.campusgps.model.ModoTema
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Un único delegado por archivo evita crear varias instancias sobre los mismos datos.
private val Context.preferenciasCampus by preferencesDataStore(name = "campus_preferencias")

class PreferenciasDataStore(context: Context) : PreferenciasRepository {
    private val almacen = context.applicationContext.preferenciasCampus
    private val ultimo = intPreferencesKey("ultimo_lugar_id")
    private val tema = stringPreferencesKey("modo_tema")
    private val datos = almacen.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }
    override val ultimoLugarId = datos.map { it[ultimo] }
    override val modoTema = datos.map { preferencias ->
        ModoTema.entries.firstOrNull { it.name == preferencias[tema] } ?: ModoTema.SISTEMA
    }
    override suspend fun guardarUltimoLugar(id: Int) {
        require(id > 0)
        almacen.edit { it[ultimo] = id }
    }
    override suspend fun guardarTema(modo: ModoTema) { almacen.edit { it[tema] = modo.name } }
}
