# Arquitectura de VetControl

## 1. Descripción general

VetControl utiliza una arquitectura de microservicios desarrollada con Spring Boot. Cada microservicio representa un dominio específico del sistema, contiene sus propias reglas de negocio, expone una API REST y utiliza un esquema independiente en MySQL.

La arquitectura busca mantener los componentes desacoplados, facilitar el mantenimiento y permitir que cada servicio pueda evolucionar o desplegarse de manera independiente.

Los clientes externos, como Postman o una futura aplicación frontend, acceden al sistema mediante API Gateway.

## 2. Componentes principales

### Eureka Server

`eureka-server` funciona como servidor de descubrimiento de servicios.

Sus responsabilidades son:

* Registrar los microservicios disponibles.
* Mostrar el estado de las instancias.
* Permitir que API Gateway encuentre los servicios mediante su nombre.
* Evitar el uso de direcciones IP fijas entre microservicios.

Puerto:

```text
8761
```

Panel:

```text
http://localhost:8761
```

### API Gateway

`api-gateway` es el punto único de entrada al sistema.

Sus principales responsabilidades son:

* Recibir solicitudes externas.
* Validar el token JWT.
* Aplicar restricciones según el rol.
* Redirigir cada solicitud al microservicio correspondiente.
* Centralizar la configuración de rutas y CORS.

Puerto:

```text
8080
```

Ejemplo:

```text
POST http://localhost:8080/api/v1/auth/login
```

El usuario no necesita conocer el puerto interno de cada microservicio cuando utiliza el Gateway.

### Auth Service

`auth-service` administra:

* Inicio de sesión.
* Usuarios.
* Roles.
* Contraseñas cifradas con BCrypt.
* Generación de tokens JWT.

Roles disponibles:

* `ADMIN`
* `RECEPCIONISTA`
* `VETERINARIO`

## 3. Microservicios del sistema

| Microservicio          | Puerto | Responsabilidad                      |
| ---------------------- | -----: | ------------------------------------ |
| `auth-service`         |   8081 | Usuarios, roles y autenticación      |
| `cliente-service`      |   8082 | Gestión de clientes                  |
| `mascota-service`      |   8083 | Gestión de mascotas                  |
| `veterinario-service`  |   8084 | Gestión de veterinarios              |
| `agenda-service`       |   8085 | Gestión de citas                     |
| `atencion-service`     |   8086 | Registro de atenciones veterinarias  |
| `historial-service`    |   8087 | Historial clínico                    |
| `producto-service`     |   8088 | Gestión de productos                 |
| `inventario-service`   |   8089 | Control de existencias               |
| `venta-service`        |   8090 | Registro de ventas                   |
| `notificacion-service` |   8091 | Notificaciones generadas por eventos |

## 4. Comunicación sincrónica

La comunicación sincrónica se realiza mediante peticiones HTTP y clientes OpenFeign.

Este tipo de comunicación se utiliza cuando un microservicio necesita una respuesta inmediata antes de completar una operación.

Ejemplos:

* `mascota-service` consulta `cliente-service` para validar que el dueño exista.
* `agenda-service` consulta `mascota-service` y `veterinario-service`.
* `venta-service` consulta `cliente-service`, `producto-service` e `inventario-service`.
* `atencion-service` utiliza identificadores de cita, mascota y veterinario.

Flujo de ejemplo:

```text
Cliente externo
      |
      v
API Gateway
      |
      v
mascota-service
      |
      v
cliente-service
```

Si el cliente indicado no existe, la creación de la mascota debe ser rechazada.

## 5. Comunicación asincrónica

La comunicación asincrónica utiliza Apache Kafka.

Los eventos permiten que un servicio publique información sin esperar una respuesta inmediata de otro microservicio.

Eventos principales:

* `cita-creada`
* `atencion-registrada`
* `venta-creada`

Ejemplo:

```text
agenda-service
      |
      | publica cita-creada
      v
Apache Kafka
      |
      v
notificacion-service
```

Este enfoque reduce el acoplamiento entre los servicios y permite procesar tareas secundarias, como la creación de notificaciones, sin detener la operación principal.

Configuración de Kafka:

```text
Desde Windows: localhost:9092
Dentro de Docker: kafka:29092
```

## 6. Seguridad

El sistema utiliza autenticación basada en JWT.

### Flujo de autenticación

1. El usuario envía sus credenciales a `auth-service`.
2. Las credenciales son validadas.
3. `auth-service` genera un token JWT.
4. El cliente guarda el token.
5. El token se envía en cada solicitud protegida.
6. API Gateway valida la firma, vigencia y contenido del token.
7. Los servicios aplican las reglas correspondientes al rol.

Encabezado:

```http
Authorization: Bearer TOKEN_GENERADO
```

### Respuestas de seguridad esperadas

| Situación                         |      Respuesta |
| --------------------------------- | -------------: |
| Token válido y permisos correctos | 200, 201 o 204 |
| Token inexistente                 |            401 |
| Token inválido o vencido          |            401 |
| Usuario autenticado sin permisos  |            403 |

## 7. Base de datos por servicio

VetControl utiliza el patrón de base de datos por servicio.

Cada microservicio utiliza un esquema propio:

* `vetcontrol_auth`
* `vetcontrol_clientes`
* `vetcontrol_mascotas`
* `vetcontrol_veterinarios`
* `vetcontrol_agenda`
* `vetcontrol_atenciones`
* `vetcontrol_historial`
* `vetcontrol_productos`
* `vetcontrol_inventario`
* `vetcontrol_ventas`
* `vetcontrol_notificaciones`

Los microservicios no deben consultar directamente las tablas de otros servicios.

Cuando se necesita información externa se utiliza:

* OpenFeign para consultas inmediatas.
* Kafka para eventos asincrónicos.

### Conexión MySQL

Desde Windows:

```text
Host: localhost
Puerto: 3307
Usuario: vetcontrol
```

Desde los contenedores:

```text
Host: mysql
Puerto: 3306
```

Aunque se utiliza un solo contenedor MySQL para el entorno académico, los esquemas permanecen separados.

## 8. Infraestructura Docker

Docker Compose administra los siguientes componentes:

* MySQL 8.
* Zookeeper.
* Apache Kafka.
* Eureka Server.
* API Gateway.
* Auth Service.
* Microservicios de negocio.

Todos los contenedores se conectan mediante una red Docker compartida.

Esto permite utilizar nombres de servicio como:

```text
mysql
kafka
eureka-server
auth-service
```

en lugar de direcciones IP manuales.

## 9. Healthchecks

Los servicios exponen Spring Boot Actuator:

```text
/actuator/health
```

Ejemplo:

```text
http://localhost:8082/actuator/health
```

Respuesta esperada:

```json
{
  "status": "UP"
}
```

Docker utiliza este endpoint para clasificar los contenedores como:

* `starting`
* `healthy`
* `unhealthy`

## 10. Flujo general de una solicitud

```text
Postman o Frontend
        |
        v
API Gateway
        |
        +---- Validación JWT
        |
        v
Microservicio de negocio
        |
        +---- OpenFeign hacia otro servicio
        |
        +---- Evento hacia Kafka
        |
        v
Esquema propio en MySQL
```

## 11. Ventajas de la solución

* Responsabilidades separadas por dominio.
* Menor acoplamiento entre módulos.
* Seguridad centralizada.
* Descubrimiento automático con Eureka.
* Punto único de acceso mediante API Gateway.
* Procesamiento asincrónico con Kafka.
* Persistencia independiente por servicio.
* Ejecución reproducible mediante Docker.
* Posibilidad de escalar servicios individualmente.
* Facilidad para agregar nuevos módulos en el futuro.
