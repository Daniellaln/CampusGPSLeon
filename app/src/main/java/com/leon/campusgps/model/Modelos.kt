package com.leon.campusgps.model

data class Posicion(val latitud: Double, val longitud: Double, val precisionMetros: Float? = null)

data class Lugar(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val latitud: Double,
    val longitud: Double
)

// null indica que todavía no se ha obtenido la posición del dispositivo.
data class LugarConDistancia(val lugar: Lugar, val metros: Float?, val dentroDelRadio: Boolean? = null)

enum class CategoriaLugar(val etiqueta: String) {
    TODOS("Todo"), ESTUDIO("Estudio"), COMIDA("Comida"), EVENTOS("Eventos"), SERVICIOS("Servicios");

    fun incluye(lugar: Lugar): Boolean = this == TODOS || lugar.categoria == etiqueta
}

enum class TipoError { SIN_PERMISO, GPS_APAGADO, SIN_PLAY_SERVICES, OTRO }
enum class ModoTema(val etiqueta: String) { SISTEMA("Del sistema"), CLARO("Claro"), OSCURO("Oscuro") }

sealed interface UbicacionUi {
    data object Inicial : UbicacionUi
    data object Buscando : UbicacionUi
    data class Lista(val posicion: Posicion) : UbicacionUi
    data class Error(val mensaje: String, val tipo: TipoError) : UbicacionUi
}
