# Arquitectura VetControl

## Patrón general

VetControl usa microservicios desacoplados. Cada servicio tiene responsabilidad propia, API REST, base de datos independiente y reglas de negocio locales.

## Comunicación

- OpenFeign: validaciones cruzadas inmediatas. Ejemplos:
  - mascota-service valida cliente-service.
  - agenda-service valida mascota-service y veterinario-service.
  - venta-service valida cliente-service, producto-service e inventario-service.
- Kafka: eventos de negocio asincrónicos.
  - cita-creada
  - atencion-registrada
  - venta-creada

## Seguridad

- auth-service genera JWT.
- Los servicios validan el token con un filtro JWT.
- Roles: ADMIN, RECEPCIONISTA, VETERINARIO.

## Base de datos por servicio

Cada microservicio usa un esquema propio en MySQL. No se comparten tablas directamente.
