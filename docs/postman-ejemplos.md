# Ejemplos Postman

## Login

POST `http://localhost:8080/api/v1/auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

## Crear cliente

POST `http://localhost:8080/api/v1/clientes`

```json
{
  "rut": "12345678-9",
  "nombre": "Pedro Riquelme",
  "telefono": "+56912345678",
  "correo": "pedro@example.com",
  "direccion": "Santiago"
}
```

## Crear mascota

POST `http://localhost:8080/api/v1/mascotas`

```json
{
  "clienteId": 1,
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

POST `http://localhost:8080/api/v1/citas`

```json
{
  "mascotaId": 1,
  "veterinarioId": 1,
  "fecha": "2026-06-01",
  "hora": "10:30:00",
  "motivo": "Consulta general"
}
```

## Registrar venta

POST `http://localhost:8080/api/v1/ventas`

```json
{
  "clienteId": 1,
  "medioPago": "DEBITO",
  "detalles": [
    { "productoId": 1, "cantidad": 2 }
  ]
}
```
