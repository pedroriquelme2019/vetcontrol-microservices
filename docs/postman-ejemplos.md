# Pruebas funcionales de VetControl con Postman

## 1. Objetivo

Este documento contiene el orden recomendado para probar VetControl mediante Postman.

Todas las solicitudes deben realizarse a través de API Gateway:

```text
http://localhost:8080
```

## 2. Variables del entorno

Crear o importar un entorno llamado:

```text
VetControl Local
```

Variables:

| Variable        | Valor inicial           |
| --------------- | ----------------------- |
| `baseUrl`       | `http://localhost:8080` |
| `token`         | Vacío                   |
| `clienteId`     | `1`                     |
| `mascotaId`     | `1`                     |
| `veterinarioId` | `1`                     |
| `citaId`        | `1`                     |
| `productoId`    | `1`                     |
| `ventaId`       | `1`                     |

Las rutas utilizarán:

```text
{{baseUrl}}
```

Las solicitudes protegidas deberán utilizar:

```text
Authorization → Bearer Token → {{token}}
```

## 3. Inicio de sesión

### Petición

```http
POST {{baseUrl}}/api/v1/auth/login
Content-Type: application/json
```

### Body

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Respuesta esperada

```json
{
  "token": "TOKEN_GENERADO",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

### Script para guardar automáticamente el token

En la pestaña **Tests**:

```javascript
const respuesta = pm.response.json();

pm.test("Inicio de sesión correcto", function () {
    pm.response.to.have.status(200);
    pm.expect(respuesta.token).to.exist;
});

if (respuesta.token) {
    pm.environment.set("token", respuesta.token);
}
```

## 4. Crear cliente

### Petición

```http
POST {{baseUrl}}/api/v1/clientes
Authorization: Bearer {{token}}
Content-Type: application/json
```

### Body

```json
{
  "rut": "12345678-9",
  "nombre": "Pedro Riquelme",
  "telefono": "+56912345678",
  "correo": "pedro@example.com",
  "direccion": "Santiago"
}
```

### Tests

```javascript
const respuesta = pm.response.json();

pm.test("Cliente creado correctamente", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
    pm.expect(respuesta.id).to.exist;
});

if (respuesta.id) {
    pm.environment.set("clienteId", respuesta.id);
}
```

## 5. Consultar clientes

```http
GET {{baseUrl}}/api/v1/clientes
Authorization: Bearer {{token}}
```

Respuesta esperada:

```json
[
  {
    "id": 1,
    "rut": "12345678-9",
    "nombre": "Pedro Riquelme"
  }
]
```

La estructura exacta puede contener más campos.
## Crear mascota

```http
POST {{baseUrl}}/api/v1/mascotas
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "clienteId": "{{clienteId}}",
  "nombre": "Rocky",
  "especie": "Perro",
  "raza": "Mestizo",
  "edad": 3,
  "sexo": "Macho",
  "peso": 12.5,
  "microchip": "CHIP-123"
}
```

## Agendar cita

```http
POST {{baseUrl}}/api/v1/citas
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "mascotaId": "{{mascotaId}}",
  "veterinarioId": "{{veterinarioId}}",
  "fecha": "2027-12-15",
  "hora": "10:30:00",
  "motivo": "Consulta general"
}
```

## Registrar atención

```http
POST {{baseUrl}}/api/v1/atenciones
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "citaId": "{{citaId}}",
  "mascotaId": "{{mascotaId}}",
  "veterinarioId": "{{veterinarioId}}",
  "fechaAtencion": "2027-12-15T10:45:00",
  "diagnostico": "Control general sin hallazgos graves",
  "tratamiento": "Vitaminas y control en 30 días",
  "observaciones": "Paciente estable"
}
```

## Crear inventario

```http
POST {{baseUrl}}/api/v1/inventario
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "productoId": "{{productoId}}",
  "stockActual": 20,
  "stockMinimo": 5
}
```

## Validar stock

```http
GET {{baseUrl}}/api/v1/inventario/productos/{{productoId}}/validar/2
Authorization: Bearer {{token}}
```

## Registrar venta

```http
POST {{baseUrl}}/api/v1/ventas
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "clienteId": "{{clienteId}}",
  "medioPago": "DEBITO",
  "detalles": [
    {
      "productoId": "{{productoId}}",
      "cantidad": 2
    }
  ]
}
```


### Tests

```javascript
const respuesta = pm.response.json();

pm.test("Venta registrada correctamente", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});

if (respuesta.id) {
    pm.environment.set("ventaId", respuesta.id);
}
```

Después de la venta, se debe comprobar que el inventario haya disminuido.

## 14. Consultar notificaciones

```http
GET {{baseUrl}}/api/v1/notificaciones
Authorization: Bearer {{token}}
```

Esta prueba permite verificar si los eventos publicados mediante Kafka fueron procesados por `notificacion-service`.

## 15. Pruebas de seguridad

### Solicitud sin token

Ejecutar:

```http
GET {{baseUrl}}/api/v1/clientes
```

sin configurar autorización.

Resultado esperado:

```text
401 Unauthorized
```

### Token vencido

Utilizar un token de una prueba anterior.

Resultado esperado:

```text
401 Unauthorized
```

### Token alterado

Modificar manualmente uno o más caracteres del token.

Resultado esperado:

```text
401 Unauthorized
```

### Usuario sin permisos

Ejecutar una operación administrativa con un rol que no esté autorizado.

Resultado esperado:

```text
403 Forbidden
```

## 16. Orden recomendado para la demostración

1. Verificar Eureka.
2. Ejecutar login.
3. Guardar el token.
4. Crear cliente.
5. Crear mascota.
6. Crear veterinario.
7. Agendar cita.
8. Registrar atención.
9. Crear producto.
10. Crear inventario.
11. Validar stock.
12. Registrar venta.
13. Comprobar reducción de stock.
14. Consultar notificaciones.
15. Mostrar una prueba de token inválido o expirado.

## 17. Recomendaciones

* Utilizar datos diferentes si las validaciones no permiten duplicados.
* Generar un token nuevo antes de iniciar la presentación.
* Confirmar que el entorno `VetControl Local` esté seleccionado.
* Revisar que las variables de identificadores se actualicen.
* Usar fechas futuras para las citas.
* Comprobar que todos los servicios estén registrados en Eureka.
