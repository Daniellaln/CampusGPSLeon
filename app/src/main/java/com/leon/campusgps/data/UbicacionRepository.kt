package com.leon.campusgps.data

import com.leon.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean
}

class GpsApagadoException : Exception("Activa el GPS e intenta de nuevo")
class SinPlayServicesException : Exception("Este dispositivo no tiene Google Play Services")
