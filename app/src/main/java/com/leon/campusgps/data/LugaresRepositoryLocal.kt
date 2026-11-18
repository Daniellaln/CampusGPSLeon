package com.leon.campusgps.data

import com.leon.campusgps.model.Lugar

class LugaresRepositoryLocal : LugaresRepository {
    override fun obtenerLugares(): List<Lugar> = listOf(
        // Coordenadas proporcionadas por Daniella para Tecsup Santa Anita.
        Lugar(1, "Biblioteca", "Estudio", -12.044947716677237, -76.95291464372102),
        Lugar(2, "Cafetería", "Comida", -12.044048206861511, -76.9521813705978),
        Lugar(3, "Laboratorio de Cómputo", "Estudio", -12.043717829954366, -76.95320316878995),
        Lugar(4, "Auditorio", "Eventos", -12.0447880312089, -76.95276423232795),
        Lugar(5, "Estacionamiento", "Servicios", -12.044383822992161, -76.95174238785984)
    )
}
