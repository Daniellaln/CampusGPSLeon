disposition: ship

Alcance: corrección a Tecsup Santa Anita y regresiones materiales de ese cambio. Dirección visual aceptada y revisión anterior cerrada. Construcción Android desde código, sin comp aprobado ni detector HTML/CSS; tarjeta QUALITY BAR no aportada en este pase acotado.

## persistence

pass. PRODUCT.md, DESIGN.md y docs/CAMPUS.md reflejan la sede. Se abrieron primero las cinco capturas requeridas: phone.png, phone_dark.png, phone_large_text.png, phone_landscape.png y docs/capturas_fase2/01_inicio.png. Son válidas, sin regiones vacías ni error cartográfico, del Pixel 8 Pro/API 37 indicado. Los resultados guardados confirman 29 pruebas unitarias y 12 instrumentadas sin fallos; lint registra 31 advertencias, sin errores.

## fidelity

| Elemento | Estado | Evidencia |
|---|---|---|
| Sede | match | «Tecsup · Santa Anita» visible en las cinco capturas, incluido texto grande y horizontal. |
| Centro inicial | match | CampusTecsup.kt usa exactamente -12.0439002, -76.9529147 y zoom 16. El entorno de Huarochirí/Cascanueces y los edificios coinciden con las dos referencias proporcionadas. |
| Cinco destinos | match | Biblioteca, Cafetería, Laboratorio de Cómputo, Auditorio y Estacionamiento conservan exactamente las coordenadas dadas por Daniella; Kotlin y docs/campus_tecsup.json coinciden. |
| Mapa y GPS | match | MapLibre sigue cargando OpenFreeMap. La aplicación inyecta UbicacionRepositoryGoogle: getCurrentLocation y requestLocationUpdates entregan las coordenadas del dispositivo al ViewModel, al punto azul y al cálculo de distancias. El centro fijo se utiliza únicamente como cámara inicial. La simulación del emulador está declarada en docs/VERIFICACION.md. |
| TYPE | match | Se conserva la escala Material 3; el nuevo rótulo de sede es legible en las variantes requeridas. |
| MATERIAL | match | Se conserva el mapa real y el vidrio aceptado. La ilustración inicial sigue siendo un recurso ilustrativo, sin sustituir la cartografía. |
| GROUND | match | Blanco frío y verde agua, con equivalente oscuro, conforme a la dirección aceptada. |

## ceiling

reached para el cambio solicitado. Sin regresiones materiales introducidas por la sede, el centro o los nuevos destinos en las capturas de teléfono revisadas.

## material_fixes

Ninguna.

## keep

Conservar GPS real, cartografía y atribución visibles, coordenadas facilitadas por Daniella y dirección verde agua/vidrio claro.
