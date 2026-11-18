package com.leon.campusgps.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Un único delegado por archivo evita crear varias instancias sobre los mismos datos.
private val Context.preferenciasCampus by preferencesDataStore(name = "campus_preferencias")

class PreferenciasDataStore(context: Context) : PreferenciasRepository {
    private val almacen = context.applicationContext.preferenciasCampus
    private val ultimo = intPreferencesKey("ultimo_lugar_id")
    override val ultimoLugarId = almacen.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { it[ultimo] }
    override suspend fun guardarUltimoLugar(id: Int) {
        require(id > 0)
        almacen.edit { it[ultimo] = id }
    }
}
