# VetControl - Plataforma de Microservicios para Clínica Veterinaria y Pet Shop

Proyecto base profesional para **Desarrollo Full Stack I**. Implementa una arquitectura distribuida para administrar clientes, mascotas, veterinarios, agenda, atenciones clínicas, historial, productos, inventario, ventas, autenticación y notificaciones.

> Uso recomendado: plantilla académica/profesional para estudiar, adaptar y completar con el equipo antes de entregar.

## Arquitectura incluida

- 10+ microservicios Spring Boot independientes.
- API Gateway con Spring Cloud Gateway.
- Service Discovery con Eureka.
- Comunicación sincrónica con OpenFeign.
- Comunicación asincrónica con Apache Kafka.
- Base de datos por servicio usando esquemas MySQL independientes.
- Spring Data JPA + Flyway para persistencia y migraciones SQL.
- DTOs + Bean Validation.
- ResponseEntity + manejo global de errores con `@RestControllerAdvice`.
- Seguridad básica con Spring Security, JWT, BCrypt y RBAC.
- Logs con SLF4J/Logback.
- Swagger/OpenAPI por microservicio.

## Módulos

| Módulo | Puerto | Responsabilidad |
|---|---:|---|
| eureka-server | 8761 | Registro y descubrimiento de servicios |
| api-gateway | 8080 | Punto único de entrada |
| auth-service | 8081 | Login, usuarios, JWT, roles |
| cliente-service | 8082 | Clientes/dueños |
| mascota-service | 8083 | Mascotas/pacientes veterinarios |
| veterinario-service | 8084 | Veterinarios y especialidades |
| agenda-service | 8085 | Citas, disponibilidad horaria |
| atencion-service | 8086 | Atenciones veterinarias |
| historial-service | 8087 | Historial clínico |
| producto-service | 8088 | Productos del pet shop |
| inventario-service | 8089 | Stock, movimientos, control de inventario |
| venta-service | 8090 | Ventas y detalle de productos |
| notificacion-service | 8091 | Notificaciones por eventos Kafka |

## Requisitos locales

- Java 17
- Maven 3.9+
- Docker Desktop o MySQL local
- Postman

## Levantar infraestructura

```bash
cd vetcontrol-microservices
docker compose up -d
```

Esto levanta:
- MySQL con bases separadas: `vetcontrol_auth`, `vetcontrol_clientes`, `vetcontrol_mascotas`, etc.
- Kafka + Zookeeper.

## Orden recomendado de ejecución en IntelliJ

1. `eureka-server`
2. `api-gateway`
3. `auth-service`
4. Resto de microservicios

También puedes compilar todo:

```bash
mvn clean install -DskipTests
```

## Credenciales de prueba

El `auth-service` crea usuarios iniciales:

| Usuario | Password | Rol |
|---|---|---|
| admin | admin123 | ADMIN |
| recepcion | recepcion123 | RECEPCIONISTA |
| vet | vet123 | VETERINARIO |

## Login

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Copia el token y úsalo en Postman:

```http
Authorization: Bearer TU_TOKEN
```

## Endpoints principales por Gateway

```http
GET    http://localhost:8080/api/v1/clientes
POST   http://localhost:8080/api/v1/clientes
GET    http://localhost:8080/api/v1/mascotas
POST   http://localhost:8080/api/v1/mascotas
GET    http://localhost:8080/api/v1/veterinarios
POST   http://localhost:8080/api/v1/veterinarios
GET    http://localhost:8080/api/v1/citas
POST   http://localhost:8080/api/v1/citas
GET    http://localhost:8080/api/v1/atenciones
POST   http://localhost:8080/api/v1/atenciones
GET    http://localhost:8080/api/v1/productos
POST   http://localhost:8080/api/v1/productos
GET    http://localhost:8080/api/v1/inventario
POST   http://localhost:8080/api/v1/inventario
GET    http://localhost:8080/api/v1/ventas
POST   http://localhost:8080/api/v1/ventas
```

## Flujo funcional recomendado para demo

1. Login con `admin`.
2. Crear cliente.
3. Crear mascota asociada a cliente.
4. Crear veterinario.
5. Agendar cita validando veterinario y mascota.
6. Registrar atención.
7. Crear producto.
8. Crear inventario para el producto.
9. Registrar venta validando cliente, producto y stock.
10. Ver notificaciones generadas por eventos Kafka.

## Swagger

Cada servicio expone Swagger, por ejemplo:

```http
http://localhost:8082/swagger-ui/index.html
http://localhost:8088/swagger-ui/index.html
```

## Nota técnica

Las bases están separadas por esquema MySQL para facilitar laboratorio local. En producción podrían levantarse servidores MySQL separados por servicio o contenedores independientes.
