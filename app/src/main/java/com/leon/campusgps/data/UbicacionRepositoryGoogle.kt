package com.leon.campusgps.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.leon.campusgps.model.Posicion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class UbicacionRepositoryGoogle(context: Context) : UbicacionRepository {
    private val contexto = context.applicationContext
    private val cliente by lazy { LocationServices.getFusedLocationProviderClient(contexto) }

    override fun playServicesDisponible(): Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(contexto) == ConnectionResult.SUCCESS

    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> {
        val cancelacion = CancellationTokenSource()
        return try {
            if (!playServicesDisponible()) throw SinPlayServicesException()
            val manager = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!LocationManagerCompat.isLocationEnabled(manager)) throw GpsApagadoException()
            val fina = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val aproximada = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!fina && !aproximada) throw SecurityException("Falta el permiso de ubicación")
            val solicitud = CurrentLocationRequest.Builder()
                .setPriority(if (fina) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setMaxUpdateAgeMillis(0)
                .setDurationMillis(10_000)
                .build()
            val ubicacion = withTimeoutOrNull(12_000) {
                cliente.getCurrentLocation(solicitud, cancelacion.token).await()
            } ?: throw GpsApagadoException()
            Result.success(Posicion(ubicacion.latitude, ubicacion.longitude))
        } catch (e: CancellationException) {
            // La cancelación de una corrutina no es un fallo del GPS.
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            cancelacion.cancel()
        }
    }
}
