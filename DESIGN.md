---
name: CampusGPS
description: Orientación en Android con verde agua y vidrio claro.
colors:
  light-primary: "#087768"
  light-primary-container: "#D1F2E9"
  light-on-primary-container: "#063B33"
  light-secondary: "#2F736A"
  light-secondary-container: "#D6EDE6"
  light-on-secondary-container: "#173E34"
  light-background: "#F5FAF8"
  light-surface: "#FAFDFB"
  light-surface-variant: "#E4F0EB"
  light-ink: "#142D29"
  light-ink-muted: "#44625C"
  light-outline: "#69847C"
  light-outline-variant: "#CCDFD7"
  light-error: "#9D3434"
  light-error-container: "#FCE5E5"
  light-on-error-container: "#7C2828"
  dark-primary: "#83DAC5"
  dark-on-primary: "#00382E"
  dark-primary-container: "#155044"
  dark-on-primary-container: "#C0F5E7"
  dark-secondary: "#A7D1C4"
  dark-on-secondary: "#183B32"
  dark-secondary-container: "#244A40"
  dark-on-secondary-container: "#D6F1E6"
  dark-background: "#0D1B19"
  dark-surface: "#12231F"
  dark-surface-variant: "#203B33"
  dark-ink: "#E1F1E9"
  dark-ink-muted: "#B0CDC0"
  dark-outline: "#8DAA9D"
  dark-outline-variant: "#36554A"
  dark-error: "#FFB4AD"
  dark-error-container: "#632A27"
  dark-on-error-container: "#FFDAD5"
  white: "#FFFFFF"
  glass-fallback-light: "#F5FFFA"
  glass-fallback-dark: "#173B32"
  map-location: "#1674D1"
  map-nearest: "#138A5B"
  map-place-light: "#277CA5"
  map-place-dark: "#94CDDD"
  map-radius: "#168B7A"
typography:
  headline-small:
    fontFamily: "sans-serif"
    fontSize: "24sp"
    fontWeight: 700
    lineHeight: "32sp"
    letterSpacing: "0sp"
  title-large:
    fontFamily: "sans-serif"
    fontSize: "22sp"
    fontWeight: 700
    lineHeight: "28sp"
    letterSpacing: "0sp"
  title-medium:
    fontFamily: "sans-serif"
    fontSize: "16sp"
    fontWeight: 500
    lineHeight: "24sp"
    letterSpacing: "0.2sp"
  title-small:
    fontFamily: "sans-serif"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
    letterSpacing: "0.1sp"
  body-large:
    fontFamily: "sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "24sp"
    letterSpacing: "0.5sp"
  body-medium:
    fontFamily: "sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
    letterSpacing: "0.2sp"
  body-small:
    fontFamily: "sans-serif"
    fontSize: "12sp"
    fontWeight: 400
    lineHeight: "16sp"
    letterSpacing: "0.4sp"
  label-large:
    fontFamily: "sans-serif"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "20sp"
    letterSpacing: "0.1sp"
  label-medium:
    fontFamily: "sans-serif"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0.5sp"
  label-small:
    fontFamily: "sans-serif"
    fontSize: "11sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0.5sp"
rounded:
  row: "12dp"
  primary-action: "27dp"
  glass: "28dp"
  sheet: "30dp"
  glass-action: "32dp"
spacing:
  tight: "4dp"
  compact: "8dp"
  inline: "10dp"
  related: "12dp"
  section: "16dp"
  action-inset: "18dp"
  screen: "20dp"
  modal: "24dp"
components:
  button-primary:
    backgroundColor: "{colors.light-primary}"
    textColor: "{colors.white}"
    typography: "{typography.label-large}"
    rounded: "{rounded.primary-action}"
    height: "54dp"
  button-primary-dark:
    backgroundColor: "{colors.dark-primary}"
    textColor: "{colors.dark-on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.primary-action}"
    height: "54dp"
  button-glass:
    textColor: "{colors.light-ink}"
    typography: "{typography.label-large}"
    rounded: "{rounded.glass-action}"
    padding: "0dp 18dp"
    height: "52dp"
  icon-glass-location:
    textColor: "{colors.light-primary}"
    rounded: "{rounded.glass-action}"
    size: "60dp"
  chip-selected:
    backgroundColor: "{colors.light-secondary-container}"
    textColor: "{colors.light-on-secondary-container}"
    typography: "{typography.label-large}"
    height: "48dp"
  radius-segment:
    textColor: "{colors.light-primary}"
    rounded: "{rounded.glass}"
    height: "54dp"
  place-row:
    textColor: "{colors.light-ink}"
    typography: "{typography.body-large}"
    rounded: "{rounded.row}"
    padding: "10dp"
  places-sheet:
    textColor: "{colors.light-ink}"
    rounded: "{rounded.sheet}"
    padding: "0dp 20dp"
---

# Design System: CampusGPS

## Overview

**Creative North Star: "Verde agua y vidrio claro"**

La dirección confirmada por Daniella combina blanco frío, verde agua profundo y cristal legible. La interfaz es sobria y táctil: el mapa real conserva su detalle y las superficies flotantes reúnen acciones breves. Los rótulos y los iconos permanecen nítidos sobre el material.

Este documento describe el código Android de fase 2, no una implementación iOS ni un material nativo de Apple. Kotlin, Compose y Material 3 son la autoridad. Los lugares son ficticios; la ilustración inicial no es una fotografía ni un plano del campus. No hay login ni perfil.

**Key Characteristics:**

- Verde agua para acciones y estado, con equivalentes explícitos de noche.
- Cristal ligero en controles y centro mate en superficies de lectura.
- Tipografía Material 3, iconos vectoriales de trazo redondeado y áreas táctiles cómodas.
- Una misma información accesible desde el mapa y la lista.

Fuentes: `ui/theme/Theme.kt`, `ui/CristalCampus.kt`, `ui/PantallaMapa.kt`, `ui/ListaLugares.kt` y `ui/MapaUbicacion.kt`, bajo `app/src/main/java/com/leon/campusgps/`. Evidencia visual: `.impeccable/review/phone{,_dark,_large_text,_landscape}.png`, `docs/capturas_fase2/01_inicio.png` y `04_filtros.png`. Son capturas del emulador Android usado en la entrega; no acreditan todos los dispositivos, niveles API ni escalas de fuente. El código prevalece sobre capturas de estados anteriores.

## Colors

Los tokens `light-*` y `dark-*` reflejan los dos `ColorScheme` explícitos. No se usa color dinámico del dispositivo.

### Primary

- **Verde agua profundo / agua nocturna:** `light-primary` y `dark-primary` identifican acciones, ubicación lista, lugar más cercano y selección activa. Los botones rellenos usan blanco en claro y `dark-on-primary` en oscuro.
- **Agua suave:** los pares `*-primary-container` / `*-on-primary-container` sostienen selección. La fila elegida usa el contenedor con alfa 0.8.
- **Señales cartográficas:** `map-location` refuerza el punto nativo azul; `map-nearest` señala el lugar más cercano; los otros lugares usan `map-place-light` o `map-place-dark`. `map-radius` delimita el radio con relleno de alfa 0.085 y contorno de alfa 0.7.

### Secondary

- **Salvia:** los pares `*-secondary`, `*-secondary-container` y sus colores de contenido pertenecen al tema Material 3; los chips seleccionados usan el contenedor secundario y una marca de verificación.

### Neutral

- **Blanco frío / verde noche:** `*-background`, `*-surface` y `*-surface-variant` ordenan fondo, hojas y estados. `*-ink` sirve tanto para `onSurface` como para `onBackground`; `*-ink-muted` conserva texto secundario legible.
- **Bordes vegetales:** `*-outline` y `*-outline-variant` separan controles y asa sin competir con el mapa.
- **Error:** los tres tokens de error de cada tema se reservan para fallos y sus mensajes. Los demás valores no redefinidos por `Theme.kt` siguen los valores predeterminados de Material 3.

**The Estado legible Rule.** Acompañar el color de selección, cercanía o radio con texto, marca, semántica de estado o tamaño; el color solo no basta.

## Typography

**Display Font / Body Font:** `FontFamily.SansSerif` de Android; no hay fuente empaquetada. `Typography()` de Material 3 aporta la escala y el código aplica los pesos indicados en el frontmatter. Las unidades `sp` conservan la escala de fuente del usuario; `dp` pertenece a geometría, no al texto.

- **Headline small:** nombre del producto y título de filtros; énfasis Bold.
- **Title large:** encabezado de lugares; énfasis Bold.
- **Title medium:** etiquetas de secciones; el título inicial usa SemiBold como variante local.
- **Body large:** nombres de lugares; SemiBold, máximo dos líneas con elipsis.
- **Body medium / small:** explicación, categoría y pertenencia al radio.
- **Title small:** distancias; SemiBold.
- **Label large / medium / small:** acciones, estado de ubicación y coordenadas. No se utiliza una escala display adicional ni texto en mayúsculas como firma.

La atribución cartográfica compacta tiene un tratamiento local y no establece el tamaño de texto para nuevos controles.

## Layout

Geometría nativa en `dp`, con insets de estado y navegación de Android. Ritmo recurrente de 4/8/12/16 y márgenes de pantalla de 20; el modal usa 24. El panel usa 20 de margen interior horizontal y las filas 10, con separación de 4.

En vertical compacto, una hoja persistente conserva el mapa visible. Su altura inicial es el menor valor entre 55% del alto y 370 sin posición / 338 con posición; el máximo útil de contenido descuenta 155. El botón de filtros mide 214 × 52 y resume categoría y radio. Controles laterales separados por 12.

Con ancho de al menos 600 o alto menor que 520 se usa panel lateral: ancho menor entre 360 y 43% de la ventana, margen exterior derecho 16 y márgenes verticales 12. Ese panel es fijo y no presenta asa ni acción de desplegar. No inferir un diseño de tablet validado a partir de una captura horizontal.

**The Tacto cómodo Rule.** Mantener objetivos de al menos 48 × 48; botones rellenos y radio miden 54 de alto, vidrio 52, ubicación 60, tema 52 y filas al menos 72. Conservar etiquetas accesibles y crecimiento del texto.

## Elevation & Depth

`SuperficieCristal` separa profundidad y contenido. En API 33+, `RuntimeShader` AGSL refracta una captura real del mapa; no aplica distorsión al texto ni a los iconos. La captura se renueva al quedar la cámara en reposo o al actualizar datos, con intervalo mínimo de 400 ms y ancho máximo de 720 px. No es una captura continua por fotograma.

La lente usa borde de 14, fuerza de 8 y difusión de 8, expresados desde `dp`. Nueve muestras difunden el mapa y el resultado se compone con alfa 0.97 para evitar una segunda superposición nítida de sus rótulos. El control tiene `mate = 0.12`; los paneles de lectura `mate = 0.94`. Para mate mayor que 0.75, el interior se vuelve casi opaco (alfa 0.965), manteniendo la refracción en los bordes. La sombra nativa mide 7 en controles y 10 en paneles; tinte `#173F35`, alfa ambiental 0.12 y puntual 0.16. El borde de luz mide 1.

En API 24–32, si falla el shader o aún no hay captura, se usa un degradado claro/nocturno: alfas 0.85→0.55 en controles y 0.97→0.94 en paneles. Ese fallback mantiene forma, contenido y acciones; no promete refracción. La hoja de Compose no añade otra sombra o elevación tonal.

**The Lectura sobre vidrio Rule.** Reservar el centro mate para información sostenida; la transparencia y el brillo nunca sustituyen el contraste del contenido. De noche, el shader mezcla un mínimo de 0.86 del tinte oscuro para mantener legibles las etiquetas incluso si la captura contiene zonas claras.

## Shapes

Filas suaves con `MaterialTheme.shapes.medium`; acciones rellenas en cápsula; vidrio redondeado, con el radio limitado a la mitad de la dimensión menor. La hoja inferior usa su radio propio y el asa mide 36 × 4. Los radios del frontmatter son los usados por esas familias, no una invitación a redondear cada elemento del mapa.

## Components

### Buttons

La acción principal es rellena, de ancho disponible y etiqueta breve. El vidrio tiene interior transparente, padding horizontal de acción y contenido oscuro/claro según el tema; el estado activo usa `primary`. Deshabilitado conserva semántica Material y reduce el vidrio a alfa 0.48.

La presión del vidrio escala a 0.945 con resorte (`dampingRatio = 0.78`, `stiffness = 650`). El selector de radio desplaza el fondo con resorte (0.78, 500). La cámara se desplaza en 500 ms y cambia inmediatamente cuando Android desactiva animaciones; Compose usa su escala de duración del sistema. No añadir movimiento continuo decorativo. La interacción táctil y el foco son nativos; los estados HTML del sidecar son solo muestras para el panel de documentación.

### Chips

`FilterChip` Material 3, mínimo táctil de 48, verificación en la opción seleccionada. Las categorías fluyen y se envuelven, con 8 de espacio horizontal y 2 vertical. La selección no depende de un cambio de color aislado.

### Cards / Containers

`SuperficieCristal` es la firma reutilizable. La hoja inferior combina borde claro, sombra moderada y centro mate; el modal de filtros usa `surface` de Material 3. No hay tarjetas decorativas repetidas alrededor de cada dato.

### Navigation

Un encabezado compacto identifica la app; el menú de aspecto ofrece sistema/claro/oscuro. La hoja se expande por asa, encabezado o icono, y Atrás la contrae. El panel lateral omite esas señales de expansión. No hay barra de navegación inferior, perfil ni campos de entrada que deban sintetizarse en este sistema.

### Mapa y filas de lugares

MapLibre OpenGL (13.5.4) conserva los rótulos cartográficos nativos: estilos Positron y Dark de OpenFreeMap, con ajuste nocturno en el código. El punto azul representa al usuario; los círculos tienen borde blanco de 3 y radio de 9, o 12 al seleccionar. Los lugares fuera del radio permanecen visibles. La lista ofrece nombre, categoría, estado del radio y distancia, y conserva la selección al recorrerla. Cada fila reserva una columna final de 64 dp alineada al extremo para la distancia y deja 12 dp interiores al final del nombre, además del separador de 8 dp.

Los iconos locales son VectorDrawable con trazo redondeado (base 24, trazo 1.8), tinte semántico y descripciones en acciones sin texto. `app/src/main/res/drawable-nodpi/campus_vista.png` es la única ilustración raster de UI: PNG transparente de 1536 × 1024 generado mediante OpenAI `image_gen` para el estado inicial, no como fondo del mapa. Su prompt y procedencia están en `docs/recursos/campus_vista.prompt.json` y en los metadatos PNG `Prompt` / `Source`. Conservar ambos al reutilizarlo. La atribución visible acredita OpenStreetMap, OpenMapTiles y OpenFreeMap; no eliminarla.

## Do's and Don'ts

### Do:

- **Do** reutilizar los pares semánticos claro/oscuro de `CampusGPSTheme` y mantener contenido legible sobre el material.
- **Do** conservar objetivos táctiles de 48 dp o mayores, texto escalable y descripciones de iconos.
- **Do** mantener estados con texto o marcas, además del color.
- **Do** preservar fallback sin shader, atribución del mapa y procedencia de la ilustración.

### Don't:

- **Don't** reemplazar el mapa real o sus rótulos por una imagen generada, texto superpuesto o un plano ficticio.
- **Don't** convertir transparencia, brillo o animación permanente en requisito de lectura.
- **Don't** presentar la ilustración o los lugares de ejemplo como información de un campus real.
- **Don't** tratar capturas de emulador como garantía de todos los dispositivos, ni convertir solapamientos de texto o atribución compacta en patrones nuevos.
