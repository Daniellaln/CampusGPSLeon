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

## 4. Último lugar guardado

```text
Compórtate como un experto en DataStore, Kotlin Flow y MVVM. Ayúdame a guardar el último lugar seleccionado en CampusGPS después del seguimiento en vivo.

Añade androidx.datastore:datastore-preferences:1.3.0. Crea PreferenciasRepository con un Flow<Int?> ultimoLugarId y una función suspend guardarUltimoLugar(id). Implementa PreferenciasDataStore con un delegado preferencesDataStore global sobre applicationContext, archivo campus_preferencias y clave ultimo_lugar_id. No crees múltiples DataStore para el mismo archivo. Conserva local.properties fuera de Git.

Inyecta la interfaz en MapaViewModel mediante CampusViewModelFactory. Para las pruebas usa PreferenciasEnMemoria. Guarda al seleccionar un lugar válido y restaura el primero al abrir; si el ID ya no existe, ignóralo. La restauración tardía no debe sobrescribir una selección que el usuario acaba de hacer. Expón ultimoLugar como StateFlow y una acción volverAlUltimoLugar que abra Todo y lo seleccione, sin consultar DataStore desde un Composable.

La UI debe ofrecer Volver a [nombre]. Si falla una escritura, muestra un aviso breve recuperable; propaga la cancelación. Comprueba guardar, crear otro ViewModel, recuperar, descartar un ID desconocido y volver al lugar estando en otra categoría. Además, selecciona un lugar en la app real, ciérrala por completo y vuelve a abrirla para comprobar persistencia.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 4. Último lugar | Pendiente de ejecutar en Gemini; referencia disponible en este commit. | Inyección de interfaz, delegado único y restauración que respeta selecciones nuevas. | Guarda fuera de la UI y evita que una lectura tardía cambie una decisión del usuario. |

## 5. Temas y punto azul

```text
Compórtate como un experto en Material 3, MapLibre Native y permisos Android. Ayúdame a añadir el tema claro/oscuro y la ubicación azul nativa a CampusGPS, conservando MVVM y el proveedor sin claves.

Guarda ModoTema SISTEMA/CLARO/OSCURO en el Repository de DataStore, con clave modo_tema. Expónlo desde el ViewModel e incluye un menú Material 3 para elegirlo. Define dos ColorScheme completos: claro blanco frío y verde agua profundo; oscuro verde muy profundo con acento menta legible. Ajusta también los iconos de las barras del sistema. No inviertas los colores de forma mecánica.

Cambia el estilo del mapa a https://tiles.openfreemap.org/styles/positron en claro y https://tiles.openfreemap.org/styles/dark en oscuro. Recrea las fuentes de lugares/radio al terminar de cargar el nuevo estilo. Mantén la cámara al cambiar tema y evita registrar otra vez los listeners de toque en cada recarga.

Para el punto azul usa locationComponent de MapLibre: comprueba FINE o COARSE antes de activarlo, LocationComponentActivationOptions con useDefaultLocationEngine(false), CameraMode.NONE y RenderMode.NORMAL. Alimenta forceLocationUpdate con la posición que ya entrega el ViewModel. Así no se consultan ubicaciones desde el Composable ni se duplica el motor del Repository. Sustituye el marcador rojo propio solo cuando el punto nativo esté activo.

Comprueba persistencia del tema, estilos reales de día/noche, texto legible, permiso denegado sin cierre y que el punto azul se mueva con el seguimiento. No agregues una clave de Maps ni APIs de Apple.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 5. Temas y punto azul | Pendiente de ejecutar en Gemini; referencia disponible en este commit. | Estilos de MapLibre y componente nativo con motor desactivado; las posiciones llegan desde el Repository. | Conserva la arquitectura y evita solicitudes de GPS duplicadas o una dependencia de facturación. |
