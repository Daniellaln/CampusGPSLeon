# CampusGPS

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Estudiantes que consultan lugares del campus desde su teléfono. Daniella Leon realiza y expone el laboratorio de forma individual.

## Product Purpose

Obtener la posición, identificar lugares cercanos y seleccionar uno en el mapa o en una lista ordenada por distancia.

## Operating Context

Laboratorio 13 de Android. Fase 1 funcional en main; fase 2 en mejora-ia. Los cinco lugares de Lima son datos ficticios de la guía. El mapa es real y requiere Internet. La ubicación requiere permiso de Android y Google Play Services.

## Capabilities and Constraints

Kotlin, Compose, Material 3, MVVM y Repository. ViewModel sin Context y consultas de ubicación fuera de la UI. Se conserva com.leon.campusgps. MapLibre/OpenFreeMap sin claves ni facturación; la diferencia frente al proveedor indicado por el docente está pendiente de consulta.

La fase 2 incorpora categorías, radios de 100/200/500 metros, punto azul de ubicación, seguimiento foreground con inicio y parada, último lugar seleccionado en DataStore y tema oscuro. La rama main se conserva.

## Brand Commitments

Nombre CampusGPS. Interfaz en español. Daniella confirmó verde agua, vidrio claro y panel inferior desplegable. Diseño profesional, moderno y sobrio, con información breve, iconos coherentes, controles cómodos y movimiento útil. Imágenes generadas cuando aporten al uso.

## Evidence on Hand

Código y diez capturas reales de fase 1. Quince pruebas unitarias y nueve instrumentadas correctas. Documento GLAB-S13-JLEONS-2026 y audio del profesor revisados en este proyecto. No se dispone de fotografías ni planos de un campus real para esta app.

## Product Principles

El mapa permite orientarse antes de abrir opciones. Las funciones avanzadas se muestran progresivamente. Los estados y errores tienen una acción comprensible. Los recursos y prompts deben estar en el repositorio y en cada entrega acumulativa.

## Accessibility & Inclusion

Objetivos táctiles de al menos 48 dp, etiquetas para iconos, texto escalable y alternativas al color. Se respeta la configuración del sistema para desactivar animaciones.
