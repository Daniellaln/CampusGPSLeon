# Prompts

Estos prompts se pueden utilizar uno por uno siguiendo este orden para replicar los resultados. Parte de la fase 1 en `main` y trabaja en `mejora-ia`.

Son instrucciones de reproducción para Gemini en Android Studio. La referencia de este repositorio se implementó con Codex; las respuestas de una sesión de Gemini deben registrarse cuando se ejecuten, sin inventarlas.

## 1. Categorías

```text
Compórtate como un experto en Kotlin, Jetpack Compose y arquitectura MVVM para Android. Ayúdame a mejorar CampusGPS partiendo de la fase 1 del repositorio Daniellaln/CampusGPSLeon, paquete com.leon.campusgps. Trabaja en mejora-ia; conserva main y el proveedor gratuito MapLibre Native 13.5.4 con OpenFreeMap. No agregues Google Maps, claves ni facturación.

Implementa chips Todo, Estudio, Comida, Eventos y Servicios. La categoría seleccionada debe vivir en MapaViewModel como StateFlow. Combina ese estado con la lista de lugares para exponer lugaresFiltrados, usados tanto por el mapa como por ListaLugares. Conserva el orden por distancia. El lugar más cercano debe calcularse sobre la lista visible; si la selección anterior queda fuera del filtro, límpiala.

Usa un enum CategoriaLugar en model/Modelos.kt, con etiquetas en español y una función incluye(Lugar). La UI solo envía filtrar(categoria) y observa el ViewModel: no consultes repositorios ni calcules distancias dentro de Compose. No pongas Context en el ViewModel. En esta etapa conserva la composición Material 3 de la base y coloca los chips en una fila horizontal desplazable. No añadas una pantalla de acceso ni un perfil que la app no necesita.

Verifica las cinco categorías sin ubicación y después de obtenerla; prueba que Estudio muestre Biblioteca y Laboratorio y que volver a Todo recupere los cinco. Compila, ejecuta las pruebas y explica los archivos modificados antes de dar el paso por terminado.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 1. Categorías | Pendiente de ejecutar en Gemini; referencia disponible en este commit. | El filtro y el más cercano se resuelven en el ViewModel, compartidos por mapa y lista. | Evita estados contradictorios y conserva MVVM. |

## 2. Radio de cercanía

```text
Compórtate como un experto en geolocalización, Kotlin y Jetpack Compose. Ayúdame a añadir el radio de cercanía a CampusGPS después del prompt de categorías, dentro de mejora-ia y sin cambiar MapLibre/OpenFreeMap ni el paquete com.leon.campusgps.

Añade en MapaViewModel un StateFlow radio con valor inicial 200 y un método que admita solo 100, 200 o 500 metros. Combina radio, estado de ubicación y categoría: conserva todos los lugares de la categoría y marca si están dentro del radio; no ocultes automáticamente los de fuera. Una distancia igual al límite cuenta como dentro y, si aún no hay posición, su estado es desconocido.

Crea util/Radio.kt: genera un contorno geodésico cerrado de 64 segmentos usando radio terrestre de 6.371.000 metros. Expón ese contorno desde el ViewModel. En MapaUbicacion representa un Polygon con GeoJsonSource, FillLayer verde agua al 12% y LineLayer de 1,5 píxeles, debajo de los marcadores. No uses una clave ni el Circle de Google Maps, porque este proyecto usa MapLibre.

Añade tres chips Material 3 de 100 m, 200 m y 500 m y comunica dentro/fuera también en la lista, con texto además del color. Mantén posición y selección al cambiar el radio. Prueba el límite de 100 metros, un punto apenas fuera, distancia desconocida y que los puntos del contorno realmente estén a 500 metros del centro. Compila y revisa el radio real en el emulador.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 2. Radio | Pendiente de ejecutar en Gemini; referencia disponible en este commit. | Contorno geodésico y distancias en util/ViewModel; representación Polygon en MapLibre. | Los grados de latitud y longitud no son metros; el mapa y la lista comparten el radio. |

## 3. Seguimiento en vivo

```text
Compórtate como un experto en Kotlin Flow, ubicación Android y arquitectura MVVM. Ayúdame a incorporar seguimiento en vivo a CampusGPS después de categorías y radio.

Amplía UbicacionRepository con observarPosiciones(): Flow<Posicion>. Implementa callbackFlow en UbicacionRepositoryGoogle, registrando requestLocationUpdates de FusedLocationProviderClient, intervalo de 3 segundos y mínimo de 2 segundos/2 metros. Respeta FINE o COARSE; usa precisión equilibrada si solo hay permiso aproximado. Antes de registrar comprueba permiso, GPS y Google Play Services.

Usa awaitClose para retirar el LocationCallback al cancelar. Si la tarea de registro termina después de haberse cancelado el Flow, retira también ese callback en addOnSuccessListener. Propaga fallos mediante close(error), sin convertir CancellationException en un error de GPS.

El ViewModel debe tener iniciarSeguimiento y detenerSeguimiento, un StateFlow de seguimiento activo y como máximo un Job de ubicación. Cada posición actualiza distancias, orden y cercano. Conserva la última posición al detener. Si no llega el primer punto en 15 segundos, termina la espera con un error recuperable. No hagas seguimiento en segundo plano ni solicites permiso background.

Pon botones Iniciar/Detener en la UI y pide permiso con el lanzador existente. Observa ProcessLifecycleOwner para detener al ir al fondo, preservando una rotación breve. Usa el ViewModel sin Context. Prueba inicio duplicado, cancelación, dos posiciones sucesivas y timeout; después camina en los controles del emulador y comprueba que cambie la lista sin pulsar Buscar.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 3. Seguimiento | Pendiente de ejecutar en Gemini; referencia disponible en este commit. | Se limpia el callback incluso si el registro termina después de cancelar; se detiene en foreground lifecycle. Retiré Log de Android del ViewModel al detectar que impedía probar el Flow en la JVM. | Evita suscripciones duplicadas y ubicación en segundo plano, y reduce dependencias de Android en el ViewModel. |
