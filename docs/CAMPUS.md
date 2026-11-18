# Tecsup Santa Anita

Centro del recinto: `-12.0439002, -76.9529147`. Corresponde al [recinto TECSUP en OpenStreetMap](https://www.openstreetmap.org/way/357287696), junto a Av. Los Cascanueces y Av. Huarochirí.

Daniella identificó el campus con `X23X+X5 Santa Anita` y la dirección de Google Maps Av. Cascanueces 2241. La [web institucional](https://www.tecsup.edu.pe/sede/sede-lima/) publica Cascanueces 2221 para esta misma sede.

La cámara inicial usa el centro del recinto; al pulsar Obtener mi ubicación, Fused Location obtiene la posición real del dispositivo. Centrar el campus no simula ni reemplaza el GPS.

Los cinco puntos fueron indicados por Daniella:

| Lugar | Latitud | Longitud |
|---|---|---|
| Biblioteca | -12.044947716677237 | -76.95291464372102 |
| Cafetería | -12.044048206861511 | -76.9521813705978 |
| Laboratorio de Cómputo | -12.043717829954366 | -76.95320316878995 |
| Auditorio | -12.0447880312089 | -76.95276423232795 |
| Estacionamiento | -12.044383822992161 | -76.95174238785984 |

Código: `model/CampusTecsup.kt` contiene el centro inicial y `data/LugaresRepositoryLocal.kt` los cinco destinos. La cámara arranca a zoom 16. Las distancias se calculan desde la ubicación recibida del GPS, no desde un punto fijo.

Las capturas de demostración envían al emulador el centro del recinto y después el punto del auditorio para mostrar el seguimiento. La fase 1 en main conserva los datos originales de la guía; esta corrección se añade al final de mejora-ia.
