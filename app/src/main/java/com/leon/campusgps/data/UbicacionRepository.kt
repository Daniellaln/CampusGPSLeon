package com.leon.campusgps.data

import com.leon.campusgps.model.Posicion
import kotlinx.coroutines.flow.Flow

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean
    fun observarPosiciones(): Flow<Posicion>
}

class GpsApagadoException : Exception("Activa el GPS e intenta de nuevo")
class SinPlayServicesException : Exception("Este dispositivo no tiene Google Play Services")
class PosicionNoDisponibleException : Exception("No se pudo obtener tu ubicación. Intenta en un lugar con mejor señal.")
