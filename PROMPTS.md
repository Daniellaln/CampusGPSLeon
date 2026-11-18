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

## 6. Diseño verde agua y cristal

```text
Compórtate como un experto en diseño de interfaces Android, Material 3, Kotlin y efectos gráficos AGSL. Necesito que rediseñes CampusGPS sin perder las seis mejoras anteriores ni MVVM. Trabaja en mejora-ia. Lee PRODUCT.md y toma como referencia el código del commit de diseño de esta rama, especialmente ui/CristalCampus.kt, ui/PantallaMapa.kt y ui/MapaUbicacion.kt. No cambies a Google Maps ni agregues claves.

Usa estos colores exactos: claro primary #087768, fondo #F5FAF8, surface #FAFDFB, texto #142D29, secundario #44625C; oscuro primary #83DAC5, fondo #0D1B19, surface #12231F, texto #E1F1E9 y secundario #B0CDC0. Define también secondaryContainer verde y LocalContentColor para evitar títulos negros en oscuro. Usa tipografía Material 3 nativa, sin serif, sin beige, sin rosa y sin encabezados decorativos.

Los recursos ya están en app/src/main/res: drawable-nodpi/campus_vista.png es una ilustración con transparencia para el estado inicial, no reemplaza el mapa. Todos los iconos ic_marca, ic_ubicacion, ic_filtros, ic_luna, ic_sol, ic_abajo, ic_flecha, ic_seguir, ic_pausa, ic_reciente, ic_estudio, ic_comida, ic_eventos, ic_servicios y demás están como VectorDrawable en drawable; los SVG originales y el prompt de la ilustración están en docs/recursos. Reutilízalos con painterResource(R.drawable...), no pongas emojis ni PNG aleatorios. Los archivos pueden obtenerse de la rama pública mejora-ia; no dependen de carpetas privadas del equipo.

Composición: mapa real a pantalla completa; cabecera con marca de 30 dp, CampusGPS en 24 sp y botón circular de tema de 52 dp. Debajo, píldora de 214 por 52 dp con filtros, categoría y radio (Todo · 200 m). Los filtros se abren en ModalBottomSheet con FlowRow de cinco FilterChip, selector de radio 100/200/500 y botón Ver lugares. Evita tener todas las opciones permanentemente en el mapa. En el lateral inferior coloca Centrar mi ubicación de 60 dp y Seguir/Detener de 132 por 52 dp.

En vertical usa BottomSheetScaffold nativo, radio superior 30 dp, peek de 370 dp inicial o 338 dp localizado, máximo 55% de la altura. El encabezado Cerca de ti, contador y flecha permiten expandir; Back lo contrae. Lista plana de filas de al menos 72 dp, icono de categoría, nombre, dentro/fuera y distancia; no anides tarjetas. El estado inicial muestra la ilustración de 92 dp, Activa tu ubicación y un único botón verde Obtener mi ubicación de 54 dp. Con posición muestra Ubicación lista/En vivo e Ir al más cercano. En ancho >=600 dp o alto <520 dp reemplaza la hoja por panel lateral de hasta 360 dp. Ajusta padding de cámara al espacio realmente visible. Deja los créditos del mapa visibles.

El cristal debe refractar contenido real, no ser un rectángulo blanco borroso. Reproduce SuperficieCristal de ui/CristalCampus.kt: RuntimeShader desde API 33, BitmapShader alimentado por snapshot del MapView, distancia a rectángulo redondeado, refracción hacia el borde, ligera separación cromática y reflejo direccional. Captura solo al terminar cámara/carga, máximo ancho 720 px, intervalo mínimo 400 ms y una captura en vuelo. Mantén texto e iconos fuera del shader. Las zonas de lectura son mate; las píldoras y círculos dejan ver el mapa. Antes de API33 usa gradiente translúcido legible, sin afirmar que tiene refracción. Cada Button nativo conserva semántica, foco y estado disabled. Al presionar escala a .945 y vuelve con spring(.78,650). El selector de radio mueve una lente con spring(.78,500).

Conserva la última posición al buscar de nuevo para no borrar el círculo ni el mapa. Usa locationComponent con motor desactivado y un punto GeoJSON azul de apoyo para GPU que no dibujan bien iconos bitmap. No inventes coordenadas ni precisión. Comprueba permisos, lista, filtros, persistencia, tema oscuro y ampliación de texto. Compila y ejecuta las pruebas antes de terminar.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 6. Diseño | Pendiente de ejecutar en Gemini; implementación de referencia realizada con Codex. | Cristal propio sobre capturas reales, panel mate, iconos vectoriales propios e ilustración generada solo en el inicio. Se corrigieron títulos oscuros y punto tenue tras revisar capturas nativas. | Reduce opciones visibles y mantiene legibilidad y orientación sobre un mapa real. |

## 7. Accesibilidad y comprobación

```text
Compórtate como un experto en accesibilidad y pruebas de aplicaciones Android con Compose. Ayúdame a cerrar CampusGPS con el diseño del prompt anterior, sin cambiar su composición ni añadir nuevas funciones.

Respeta la opción de quitar animaciones: las animaciones Compose siguen la escala del sistema; antes de mover la cámara nativa consulta ValueAnimator.areAnimatorsEnabled desde API26 y Settings.Global.ANIMATOR_DURATION_SCALE en API24/25. Usa moveCamera cuando esté desactivada y animateCamera de 500 ms cuando esté activa. Esta adaptación pertenece al renderizador, no al ViewModel.

Comprueba texto al 130%, temas claro y oscuro, teléfono vertical y horizontal. El panel debe permitir desplazamiento en estados inicial y de error, el listado conservar su scroll propio y todos los botones medir al menos 48 dp de toque. Títulos y créditos deben heredar colores legibles del tema; los créditos usan 9 sp con lineHeight 12 sp. Conserva descripciones de los botones de icono y semántica de selección/estado en filas. La cámara respeta el espacio del panel; girar no pide permiso de nuevo ni reinicia un seguimiento.

Actualiza CampusInstrumentadoTest a la interfaz final. Verifica resultados visibles y comportamiento: Ir al más cercano deshabilitado antes de ubicarse; elegirlo selecciona Biblioteca; tocar Cafetería selecciona su ID; filtro Estudio y 100 m muestra Biblioteca dentro y Laboratorio fuera; la hoja se expande para alcanzar cinco lugares; errores ofrecen recuperación correcta. Prueba también PreferenciasDataStore real con dos adaptadores sobre el mismo applicationContext y confirma que comparten lugar y tema. Mantén las pruebas unitarias de filtros, radio, cancelación del seguimiento y restauración tardía.

Ejecuta assembleDebug, testDebugUnitTest, lintDebug y connectedDebugAndroidTest. Después instala la app normal y prueba posiciones sucesivas en el emulador, fuerza cierre/reapertura para DataStore, GPS apagado y denegación. Revisa que el log registre retiro del callback al detener y al salir al escritorio. Guarda solo capturas reales: no dibujes una interfaz encima de una imagen. Describe lo probado y limita las conclusiones al dispositivo usado.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 7. Comprobación | Pendiente de ejecutar en Gemini; referencia realizada y probada con Codex. | Cámara sin movimiento cuando el sistema lo pide, panel desplazable, pruebas de lista/hoja y DataStore real. | Permite usar la app con otra orientación o preferencias de accesibilidad y comprueba comportamiento, no solo apariencia. |

## 8. Legibilidad del mapa y panel lateral

```text
Compórtate como un experto en MapLibre Native y adaptación de interfaces Android. Ayúdame a corregir dos detalles del diseño de CampusGPS sin cambiar las funciones ni la dirección verde agua y cristal.

Conserva org.maplibre.gl:android-sdk-opengl:13.5.4, el proveedor gratuito y los estilos reales de OpenFreeMap. Si el emulador no dibuja los textos, configura su apartado Graphics en Hardware o Automatic y haz Cold Boot. En esta referencia los rótulos reaparecieron usando la GPU del equipo; no alteres las capturas ni inventes nombres encima. La incidencia oficial maplibre/maplibre-native#3648 describe un problema de textos con OpenGL y emuladores. Comprueba el resultado real antes de atribuirlo al código.

Al cargar el estilo oscuro en MapaUbicacion, aumenta contraste de las capas reales: BackgroundLayer #10221C; edificios FillLayer #294338 con contorno #3B5B4D; agua #143C44; vías highway/road LineLayer #527564 y casing #203C30; SymbolLayer texto #D7EBE0 con halo #10221C de 1.2. Respeta geometrías y reglas de visibilidad del estilo. Comprueba que los nombres de calles se ven realmente en claro y oscuro con el mismo encuadre.

En PantallaMapa distingue la hoja inferior desplegable del panel lateral persistente. Añade a PanelLugares un parámetro desplegable=true y pásalo como false en horizontal/ancho. En lateral elimina tirador, flecha, clickable del encabezado y descripciones de expandir/contraer. Conserva encabezado, contador, lista desplazable, ubicación y acciones. En vertical mantén arrastre, flecha y Back para contraer.

Protege las etiquetas dentro del cristal cuando haya nombres de calles detrás: en el shader usa nueve muestras (centro .20, cuatro ejes .12 cada uno y cuatro diagonales .08 cada una), difusión de 8dp y alfa de salida .97. La transparencia óptica viene del mapa real muestreado; no mezcles otra vez ese resultado con letras nítidas del fondo. Conserva refracción y reflejos. En ListaLugares reserva 64dp para la distancia alineada al final y 12dp de margen al nombre, con máximo dos líneas.

Compila, prueba y recaptura las mismas vistas: teléfono claro, oscuro, texto 130%, horizontal, inicio y filtros. Comprueba la apariencia sobre mapa cargado, no durante una transición. No añadas adornos ni cambies la composición fuera de estas correcciones de legibilidad.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 8. Legibilidad | Pendiente de ejecutar en Gemini; corrección de referencia realizada con Codex tras revisión visual. | Configuración gráfica del emulador para ver rótulos reales, contraste del mapa oscuro y retirada de controles sin acción en lateral. | Los rótulos cartográficos y las acciones honestas son necesarios para orientarse. |

## 9. Documentación y entrega

```text
Compórtate como un experto en documentación de proyectos Android para una estudiante. Ayúdame a ordenar la entrega final de CampusGPS, con lenguaje breve, claro y sin frases de publicidad. No cambies la app ni la rama main.

Actualiza README.md para la fase 2 en mejora-ia. Describe cómo localizarse, filtrar categoría/radio, seguir/detener, recuperar el último lugar y cambiar tema. Incluye ejecución con JDK21/SDK37, estructura model/data/viewmodel/ui/util y cinco respuestas cortas de reflexión sobre Repository, permisos frente a claves, secretos, distancias y denegación. No inventes usuarios ni autenticación: no existen en esta app.

Muestra una comparación de fase 1 y fase 2 con capturas reales una al lado de la otra. Usa docs/capturas/02_ubicacion_y_distancias.png para la base y docs/capturas_fase2/03_mapa_lugares.png para la mejora. Después organiza inicio, permiso, filtros, radio, seguimiento, reapertura, oscuro, texto grande, horizontal y error GPS desde docs/capturas_fase2. No uses mockups generados como evidencia ni tapes fallos con imágenes.

Mantén PROMPTS.md como guía única, con estos nueve bloques en orden y una tabla de prompt, respuesta de Gemini, adaptación y motivo. El título es Prompts y la introducción: Estos prompts se pueden utilizar uno por uno siguiendo este orden para replicar los resultados. Si todavía no se ejecutaron en Gemini, indícalo y no fabriques respuestas. Todos los recursos deben encontrarse en esta repo: imágenes en app/src/main/res/drawable-nodpi, vectores en drawable, originales en docs/recursos. No remitas a carpetas privadas del PC ni a descargas inexistentes.

Antes del cierre estabiliza el contraste nocturno del cristal: la mezcla del shader debe usar max(.17 + mate*.68, noche*.86), conservando las nueve muestras y alfa .97. Así el texto claro se mantiene legible aunque la imagen bajo el control contenga zonas claras. Comprueba el mismo filtro en oscuro vertical y horizontal; no cambies la interfaz diurna.

Documenta pruebas ejecutadas y sus límites en docs/VERIFICACION.md; deja la guía de archivos por avance en docs/GUIA_CODIGO.md y los tokens de diseño en DESIGN.md y .impeccable/design.json. Conserva la procedencia de la ilustración. Verifica enlaces, archivos necesarios y ausencia de .git, cachés, local.properties o claves en las carpetas de entrega. No garantices un porcentaje exacto de reproducción por otro modelo; el código, las capturas, recursos y especificaciones concretas son la referencia.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 9. Entrega | Pendiente de ejecutar en Gemini; documentación de referencia preparada con Codex. | Una guía ordenada, recursos dentro de la repo, comparación real y comprobaciones con límites explícitos. | Cualquier persona puede seguir el proceso sin acceder al equipo de Daniella. |

## 10. Tecsup Santa Anita y ubicaciones reales

```text
Compórtate como un experto en geolocalización Android, Kotlin y arquitectura MVVM. Ayúdame a adaptar CampusGPS al campus real de Tecsup Santa Anita después de las mejoras anteriores, trabajando solo en mejora-ia. Este prompt sustituye las posiciones de demostración del centro de Lima y sus expectativas de distancia en los prompts anteriores. Conserva MapLibre OpenGL13.5.4/OpenFreeMap y todo el diseño ya construido; no rediseñes ni cambies el proveedor.

Crea model/CampusTecsup.kt con LATITUD=-12.0439002, LONGITUD=-76.9529147, ZOOM_INICIAL=16.0 y NOMBRE="Tecsup · Santa Anita". Es el centro del recinto OpenStreetMap way357287696. Úsalo únicamente como cámara inicial en MapaUbicacion y añade el nombre de la sede debajo de CampusGPS en la cabecera. Al localizar o seguir, la posición y distancias deben continuar llegando del Repository mediante FusedLocationProviderClient: no sustituyas el GPS real por la constante del campus.

En data/LugaresRepositoryLocal.kt conserva IDs estables y coloca exactamente estos destinos indicados por Daniella:
1 Biblioteca, Estudio: -12.044947716677237, -76.95291464372102
2 Cafetería, Comida: -12.044048206861511, -76.9521813705978
3 Laboratorio de Cómputo, Estudio: -12.043717829954366, -76.95320316878995
4 Auditorio, Eventos: -12.0447880312089, -76.95276423232795
5 Estacionamiento, Servicios: -12.044383822992161, -76.95174238785984

No inventes puntos interiores ni traslades los anteriores con una suma fija. Mantén los IDs para que DataStore restaure el mismo destino tras actualizar la app. Registra las coordenadas y su procedencia en docs/CAMPUS.md y docs/campus_tecsup.json; la ilustración inicial sigue siendo un recurso generado, no una foto del campus.

Actualiza las pruebas: desde el centro del recinto el orden esperado es Laboratorio, Cafetería, Auditorio, Biblioteca y Estacionamiento; el más cercano es el ID3, aproximadamente37m. En Estudio/100m, Laboratorio está dentro y Biblioteca fuera. Una nueva posición exactamente en el Auditorio debe dar0m y convertirlo en el más cercano. Conserva pruebas de permisos, cancelación y persistencia; actualiza las etiquetas simplificadas sin Central/Principal/Norte inventados.

Compila, ejecuta las pruebas y recaptura los estados sobre Tecsup. Solo en el emulador envía el centro del campus y luego el Auditorio para demostrar movimiento. Actualiza README y verificación para no presentar destinos ficticios ni capturas del centro de Lima como la versión actual. Conserva main y los avances históricos; añade esta corrección como un nuevo commit con mensaje detallado en primera persona.
```

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| 10. Campus real | Pendiente de ejecutar en Gemini; referencia actualizada con Codex usando las coordenadas enviadas por Daniella. | Centro real de Tecsup, cinco destinos precisos, nombres simples y nuevas expectativas de orden/radio. | Las posiciones de demostración de la guía apuntaban a otra zona de Lima. |
