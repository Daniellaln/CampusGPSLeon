# Dónde está cada avance

Todos los caminos de código parten de `app/src/main/java/com/leon/campusgps/`.

**1 · Categorías.** `model/Modelos.kt` define CategoriaLugar. En `viewmodel/MapaViewModel.kt`, filtrar y lugaresFiltrados actualizan el mismo conjunto para mapa y lista.

**2 · Radio.** `util/Radio.kt` crea el círculo geodésico. cambiarRadio y contornoRadio están en MapaViewModel. `ui/MapaUbicacion.kt` lo dibuja con FillLayer/LineLayer.

**3 · Seguimiento.** `data/UbicacionRepositoryGoogle.kt`, observarPosiciones, conecta requestLocationUpdates con callbackFlow y retira el callback en awaitClose. MapaViewModel inicia/detiene el Job. PantallaMapa observa el paso a segundo plano.

**4 · Persistencia / preferencias.** `data/PreferenciasDataStore.kt` guarda ultimo_lugar_id. MapaViewModel restaura el lugar sin sobrescribir una selección nueva; volverAlUltimoLugar lo recupera.

**5 · Tema / aspecto y punto azul.** `ui/theme/Theme.kt` define colores; MainActivity aplica el tema. MapaUbicacion cambia el estilo del mapa y alimenta locationComponent con la posición del Repository. No crea otro motor GPS.

**6 · Rediseño / interfaz.** `ui/PantallaMapa.kt` organiza mapa, filtros y hoja desplegable. `ui/CristalCampus.kt` contiene refracción AGSL, reflejos y presión con resorte. `ui/ListaLugares.kt` muestra las filas. Iconos en `app/src/main/res/drawable`, ilustración en `drawable-nodpi`.

**7 · Accesibilidad / comprobaciones.** MapaUbicacion respeta quitar animaciones y PantallaMapa permite desplazar el panel. `app/src/androidTest/java/com/leon/campusgps/CampusInstrumentadoTest.kt` comprueba UI y DataStore; pruebas de lógica en `app/src/test/java/com/leon/campusgps/`.

**8 · Legibilidad del mapa.** Se conserva MapLibre en `app/build.gradle.kts`. El emulador usa Graphics Hardware/Automatic para dibujar rótulos reales. MapaUbicacion mejora el contraste nocturno. PantallaMapa retira los controles de expansión en el panel lateral persistente.

**9 · Entrega.** README, PROMPTS, capturas y verificación describen la versión comprobada. Para ver qué cambió exactamente, abre Git → Log en Android Studio, elige el commit y usa Show Diff. Para ejecutar una versión anterior, haz Checkout Revision en una copia o vuelve después a mejora-ia.

**10 · Campus real.** `model/CampusTecsup.kt` define el centro inicial y `data/LugaresRepositoryLocal.kt` guarda los cinco destinos indicados por Daniella. [Coordenadas completas](CAMPUS.md). El GPS sigue obteniendo la ubicación real del teléfono.

La arquitectura MVVM se reconoce por model, viewmodel y ui. Los repositorios están en data. Las dependencias de Material 3, ubicación, DataStore y MapLibre están en `app/build.gradle.kts`.
