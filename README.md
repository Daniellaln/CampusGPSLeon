# Variante sin tarjeta

Cambio autorizado por Daniella el 18/11/2026 al aparecer un prepago de S/100 en Google Cloud. MapLibre + OpenFreeMap sustituyen Google Maps Compose; la equivalencia académica está pendiente del profesor.

# CampusGPS

Daniella Leon · Laboratorio 13 · Fase 1 · Avance 2/10.

Declaré ubicación aproximada y precisa y conecté la clave privada al Manifest mediante manifestPlaceholders.

Proyecto reconstruido a partir del código y los contratos de la guía; no se recibió un ZIP base.
Paquete personalizado: `com.leon.campusgps`. Kotlin, Compose, Material 3 y MVVM con Repository.

Abre esta carpeta en Android Studio. Usa JDK 21 y sincroniza Gradle.
La clave de Maps se guarda únicamente en `local.properties` como `MAPS_API_KEY=...`.
No está incluida ni se debe subir a GitHub. La app permite probar la ubicación sin ella;
el área del mapa indica que falta configurar Google Maps.
