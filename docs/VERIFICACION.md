# Verificación de CampusGPS

18 de noviembre de 2026. Windows, JDK 21, Pixel_8_Pro_2 con Android 17/API 37.

## Comprobado

APK debug compilado. Lint sin errores. 15 pruebas unitarias y 9 instrumentadas correctas.

El recorrido adicional usó la app normal: permisos de Android, FusedLocationProviderClient y un mapa descargado de OpenFreeMap. Las pruebas de interfaz aisladas usan repositorios de prueba y no cargan el mapa.

| Caso | Resultado |
|---|---|
| Primer inicio y permiso nativo | Correcto |
| Aceptar permiso y ver coordenadas y posición roja | Correcto |
| Denegar permiso y ofrecer Abrir ajustes | Correcto |
| Cinco lugares en el mapa | Correcto en MapLibre |
| Tocar marcador y ver nombre y categoría | Correcto: Cafetería Campus / Comida |
| Lista ordenada, metros y kilómetros | Correcto; km cubierto por pruebas de formato |
| Seleccionar tarjeta y centrar cámara | Correcto |
| Ir al más cercano y resaltarlo en verde | Correcto |
| Cambiar ubicación y actualizar orden y cercano | Correcto; Auditorio a 0 m |
| GPS apagado y Reintentar | Correcto |
| Rotación sin perder ubicación, lista y selección | Correcto |
| Archivos privados fuera del repositorio | Revisado antes de publicar |

El caso sin Play Services se comprobó con repositorios de prueba. No se utilizó otro emulador sin GMS. Los lugares son ejemplos fijos de la guía; las posiciones de prueba se enviaron con los controles del emulador.

La implementación del mapa usa MapLibre/OpenFreeMap. Estos resultados no verifican Google Maps Compose ni una autorización de Google Cloud.

## Evidencias

Diez capturas reales en capturas/. En pruebas/ están los resultados JUnit, la ejecución instrumentada, el recorrido de la app y el registro del mapa. La compilación de cada avance se comprueba antes de su commit; no se modifican las fechas de Git.
