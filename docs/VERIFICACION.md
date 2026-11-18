# Verificación de CampusGPS

18 de noviembre de 2026. Windows, JDK 21 y emulador Pixel_8_Pro_2 con Android 17/API 37.

## Fase 2

`assembleDebug`, `testDebugUnitTest`, `lintDebug` y `connectedDebugAndroidTest` finalizaron correctamente. Son 29 pruebas unitarias y 12 pruebas instrumentadas, sin fallos. Los resultados JUnit y el resumen de lint están en [pruebas_fase2](pruebas_fase2).

Las pruebas instrumentadas aíslan la pantalla del mapa y del GPS con repositorios controlados. También verifican dos adaptadores reales sobre el mismo DataStore. El recorrido adicional usa la app normal, Fused Location y el mapa descargado de OpenFreeMap.

Se comprobaron permiso nativo, posición y distancias, categorías compartidas entre mapa/lista, radio de 100/200/500 metros, selección y lugar más cercano. Se enviaron posiciones distintas sin pulsar Buscar para verificar seguimiento; el registro confirma alta y retirada de callbacks.

El último lugar reaparece tras un force-stop y una nueva apertura. Se comprobaron claro/oscuro, texto al 130%, giro real a horizontal, GPS apagado y recuperación. Salir al escritorio detiene el seguimiento. Los mensajes de permiso/GPS/Play Services también están cubiertos por las pruebas de pantalla.

El material AGSL refracta capturas del mapa en API33 o superior; el emulador no registró errores de shader. Versiones anteriores usan la alternativa translúcida. El punto de ubicación nativo recibe precisión real, y un círculo GeoJSON refuerza su visibilidad en renderizadores que omiten iconos bitmap.

## Alcance

No se probó en un dispositivo físico ni en API24. Las posiciones de las capturas se enviaron desde los controles del emulador. Los lugares son ficticios. El mapa necesita Internet y la ubicación requiere Google Play Services. La alternativa anterior a API33 está en código, no tiene una captura de otro emulador.

Las capturas finales usan aceleración Hardware del emulador. Con SwiftShader faltaban los rótulos; al usar la GPU del equipo aparecieron, conservando MapLibre OpenGL 13.5.4. Si ocurre en otro emulador, revisa Graphics Hardware/Automatic y haz Cold Boot. La [incidencia oficial de MapLibre](https://github.com/maplibre/maplibre-native/issues/3648) describe este problema con emuladores. La configuración de hardware de un equipo no forma parte del proyecto entregado.

La guía menciona Google Maps Compose; esta implementación usa MapLibre/OpenFreeMap por decisión de la usuaria. Estas pruebas comprueban ese proveedor y no certifican uso de las APIs de Google Maps ni aprobación del cambio por el docente.

La fase 1 y sus evidencias permanecen en main. Cada avance de mejora-ia se compiló y probó antes de publicarse, con fechas reales. Las respuestas de Gemini no se han inventado: PROMPTS.md distingue reproducción e implementación de referencia.
