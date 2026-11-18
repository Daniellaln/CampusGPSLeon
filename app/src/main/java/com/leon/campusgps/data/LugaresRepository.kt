package com.leon.campusgps.data

import com.leon.campusgps.model.Lugar

interface LugaresRepository { fun obtenerLugares(): List<Lugar> }
