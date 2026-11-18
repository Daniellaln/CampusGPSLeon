# Mapa sin tarjeta

Esta variante usa MapLibre Native 13.5.4 y el estilo Liberty de OpenFreeMap, con datos de OpenStreetMap. No necesita cuenta, tarjeta, prepago ni clave de mapas.

Abre el proyecto, sincroniza Gradle y ejecútalo con Internet. Android Studio crea local.properties con la ruta de tu SDK. Acepta el permiso de ubicación al pulsar el botón.

La localización conserva FusedLocationProviderClient, que no requiere activar facturación ni obtener una clave de Google Maps. En dispositivos sin Google Play Services se muestra el error previsto por la guía.

La guía pide específicamente Google Maps Compose. Este cambio de proveedor requiere autorización del profesor; no se presenta como cumplimiento de ese punto. El 18/11/2026 se abandonó el alta de Google Cloud al aparecer un prepago de PEN 100.00.

Fuentes: https://openfreemap.org/quick_start/ y https://maplibre.org/maplibre-native/android/examples/data/rendering-engine/
