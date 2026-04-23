# Crafters Events Platform - Backend API

Una plataforma completa de gestión de eventos con autenticación JWT, pagos con Stripe, generación de códigos QR y sistema de entradas. Construida con Spring Boot 3.5 y PostgreSQL.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Tests](#tests)
- [API Endpoints](#api-endpoints)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Seguridad](#seguridad)
- [Base de Datos](#base-de-datos)

## ✨ Características

### Autenticación y Autorización
- ✅ Registro de usuarios con validación
- ✅ Login con JWT
- ✅ Refresh de tokens
- ✅ Roles basados en acceso
- ✅ CORS configurado

### Gestión de Eventos
- ✅ Crear, leer, actualizar y eliminar eventos
- ✅ Filtrado avanzado de eventos
- ✅ Búsqueda por múltiples criterios
- ✅ Eventos públicos y privados
- ✅ Capacidad máxima de asistentes

### Sistema de Tickets
- ✅ Registro de usuarios a eventos
- ✅ Verificación de tickets con código QR
- ✅ Código de verificación único
- ✅ Historial de tickets por usuario
- ✅ Control de evento pasado

### Pagos
- ✅ Integración Stripe
- ✅ Webhooks de Stripe
- ✅ Devoluciones de dinero
- ✅ Eventos gratuitos
- ✅ Reconciliación de pagos

### Imágenes y Multimedia
- ✅ Carga de imágenes de eventos
- ✅ Imágenes de perfil de usuario
- ✅ Generación de códigos QR
- ✅ Validación de tipo y tamaño

### Email y Notificaciones
- ✅ Notificaciones de cancelación de eventos
- ✅ Templates con Thymeleaf
- ✅ Envío masivo de emails
- ✅ Configuración SMTP

### Limpieza Automática
- ✅ Limpieza de tickets abandonados
- ✅ Limpieza de eventos pasados (se ocultan en la galería principal)
- ✅ Reconciliación de pagos pendientes
- ✅ Tareas programadas

## 🏗️ Arquitectura

```
com.code.crafters/
├── config/                 # Configuración (Stripe, JWT, Web)
├── controller/             # Controladores REST
├── service/                # Servicios de negocio
├── repository/             # Acceso a datos (JPA)
├── entity/                 # Modelos de base de datos
├── dto/                    # Data Transfer Objects
│   ├── request/            # DTOs de entrada
│   └── response/           # DTOs de salida
├── mapper/                 # MapStruct mappers
├── security/               # JWT y seguridad
├── scheduler/              # Tareas programadas
├── specification/          # Especificaciones JPA
├── exception/              # Excepciones personalizadas
└── CraftersApplication.java
```

### Patrones de Diseño Utilizados

- **MVC**: Controllers → Services → Repositories
- **DTO Pattern**: Separación entre modelos internos y API
- **Mapper Pattern**: MapStruct para transformación de datos
- **Repository Pattern**: Abstracción de acceso a datos
- **Service Layer**: Lógica de negocio centralizada
- **Specification Pattern**: Queries dinámicas con JPA

## 🔧 Requisitos Previos

- **Java**: 21 o superior
- **Maven**: 3.9+
- **PostgreSQL**: 14+
- **Git**: Último

### Dependencias Principales

```xml
- Spring Boot 3.5.13
- Spring Security
- Spring Data JPA
- Spring Mail
- Spring Scheduling
- JWT (jjwt 0.12.6)
- MapStruct 1.6.3
- Stripe Java 32.0.0
- ZXing 3.5.4 (QR Codes)
- Thymeleaf
- Lombok
```

## 📦 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/Code-Crafters-Events/CodeCrafters-Back
cd directorio
```

### 2. Instalar Dependencias

```bash
mvn clean install
```

### 3. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=crafters_db
DB_USER=postgres
DB_PASS=tu_password

# JWT
JWT_SECRET=tu_clave_secreta_jwt_muy_larga_minimo_32_caracteres

# Stripe
STRIPE_SECRET_KEY=sk_test_your_test_key_here
STRIPE_WEBHOOK_KEY=whsec_your_webhook_key_here

# Email
GMAIL_ACCOUNT=tu_email@gmail.com
GMAIL_SECRET=tu_gmail_app_password

# Backend URL
BACKEND_URL=http://localhost:8080
```

## 🚀 Ejecución

### Desarrollo

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

### Producción

```bash
mvn clean package -DskipTests
java -jar target/crafters-0.0.1-SNAPSHOT.jar
```

## 🧪 Tests

### Ejecutar Todos los Tests

```bash
mvn test
```

### Ejecutar Solo Tests Unitarios

```bash
mvn test -Dtest=*Service*Test
```

### Ejecutar Solo Tests de Integración

```bash
mvn test -Dtest=*Integration*
```

### Ejecutar Solo Tests E2E

```bash
mvn test -Dtest=*E2E*
```

### Con Cobertura de Código (JaCoCo)

```bash
mvn clean test jacoco:report
# Reporte disponible en: target/site/jacoco/index.html
```

### Suites de Tests Disponibles

1. **Unit Tests** (Mockito)
   - `UserServiceImplTest`
   - `EventServiceImplTest`
   - `TicketServiceImplTest`
   - Pruebas de servicios con mocks

2. **Integration Tests** (Spring Boot Test)
   - `AuthAndUserControllerIntegrationTest`
   - `EventAndTicketControllerIntegrationTest`
   - Pruebas de controllers con BD real (H2)

3. **E2E Tests** (REST Assured)
   - `EndToEndWorkflowTest`
   - Flujos completos de usuario
   - Escenarios reales

## 📡 API Endpoints

### Autenticación

#### Registro
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Juan",
  "firstName": "Pérez",
  "secondName": "García",
  "alias": "juanperez",
  "email": "juan@example.com",
  "password": "password123",
  "profileImage": null
}

Response: 201 Created
{
  "id": 1,
  "email": "juan@example.com",
  "token": "eyJhbGc...",
  "name": "Juan",
  "firstName": "Pérez",
  "alias": "juanperez"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "password123"
}

Response: 200 OK
{
  "id": 1,
  "email": "juan@example.com",
  "token": "eyJhbGc...",
  "name": "Juan"
}
```

### Usuarios

#### Obtener todos los usuarios
```http
GET /api/v1/users

Response: 200 OK
[
  {
    "id": 1,
    "email": "juan@example.com",
    "name": "Juan",
    "alias": "juanperez"
  }
]
```

#### Obtener usuario por ID
```http
GET /api/v1/users/{id}

Response: 200 OK
```

#### Actualizar usuario
```http
PUT /api/v1/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

Response: 200 OK
```

#### Actualizar perfil
```http
PATCH /api/v1/users/{id}/profile
Authorization: Bearer {token}
Content-Type: application/json

Response: 200 OK
```

#### Eliminar usuario
```http
DELETE /api/v1/users/{id}
Authorization: Bearer {token}

Response: 204 No Content
```

### Eventos

#### Crear evento
```http
POST /api/v1/events
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Masterclass Java",
  "description": "Una masterclass completa sobre Java",
  "type": "MASTERCLASS",
  "date": "2024-12-25",
  "time": "14:00",
  "maxAttendees": 50,
  "locationId": null,
  "category": "PRESENCIAL",
  "price": "29.99",
  "imageUrl": null
}

Response: 201 Created
```

#### Obtener todos los eventos
```http
GET /api/v1/events?page=0&size=15

Response: 200 OK
{
  "content": [...],
  "page": 0,
  "size": 15,
  "totalElements": 100,
  "totalPages": 7,
  "last": false
}
```

#### Obtener evento por ID
```http
GET /api/v1/events/{id}

Response: 200 OK
```

#### Buscar eventos con filtros
```http
GET /api/v1/events/search?title=Masterclass&category=PRESENCIAL&page=0&size=15

Response: 200 OK
```

#### Actualizar evento
```http
PUT /api/v1/events/{id}
Authorization: Bearer {token}
Content-Type: application/json

Response: 200 OK
```

#### Eliminar evento
```http
DELETE /api/v1/events/{id}
Authorization: Bearer {token}

Response: 204 No Content
```

### Tickets

#### Registrarse a evento
```http
POST /api/v1/tickets?eventId={eventId}
Authorization: Bearer {token}

Response: 201 Created
{
  "id": 1,
  "userId": 1,
  "eventId": 1,
  "paymentStatus": "PENDING",
  "verificationCode": "uuid-code",
  "qrUrl": "..."
}
```

#### Cancelar registro
```http
DELETE /api/v1/tickets?eventId={eventId}
Authorization: Bearer {token}

Response: 204 No Content
```

#### Obtener tickets del usuario
```http
GET /api/v1/tickets/user/{userId}?page=0&size=10

Response: 200 OK
```

#### Obtener tickets del evento
```http
GET /api/v1/tickets/event/{eventId}?page=0&size=10

Response: 200 OK
```

#### Verificar ticket con código
```http
GET /api/v1/tickets/verify/{verificationCode}

Response: 200 OK
{
  "valid": true,
  "message": "Ticket válido ✓",
  "ticketId": 1,
  "eventTitle": "Masterclass Java",
  "userName": "Juan",
  "purchasedAt": "2024-10-15T10:30:00",
  "usedAt": null,
  "paymentStatus": "COMPLETED"
}
```

#### Obtener cantidad total de tickets
```http
GET /api/v1/tickets/count

Response: 200 OK
42
```

### Pagos

#### Crear intención de pago
```http
POST /api/v1/payments/create-intent
Content-Type: application/json

{
  "userId": 1,
  "eventId": 1
}

Response: 200 OK
{
  "clientSecret": "pi_xxx#secret_xxx",
  "paymentIntentId": "pi_xxx",
  "amount": 29.99,
  "currency": "eur",
  "ticketId": 1,
  "qrUrl": "...",
  "verificationCode": "uuid"
}
```

#### Webhook de Stripe
```http
POST /api/v1/payments/webhook
Stripe-Signature: {signature}
Content-Type: application/json

Response: 200 OK
```

### Imágenes

#### Subir imagen de evento
```http
POST /api/v1/images/events/{eventId}?userId={userId}
Content-Type: multipart/form-data

form-data:
  file: <image-file>

Response: 200 OK
{
  "imageUrl": "http://localhost:8080/uploads/events/uuid.jpg"
}
```

#### Subir imagen de perfil
```http
POST /api/v1/images/users/{userId}
Content-Type: multipart/form-data

form-data:
  file: <image-file>

Response: 200 OK
{
  "imageUrl": "http://localhost:8080/uploads/avatars/uuid.jpg"
}
```

### Ubicaciones

#### Crear ubicación
```http
POST /api/v1/locations
Authorization: Bearer {token}
Content-Type: application/json

{
  "venue": "Centro de Convenciones",
  "address": "Calle Principal 123",
  "city": "Barcelona",
  "province": "Barcelona",
  "country": "España",
  "zipCode": "08002",
  "latitude": 41.3851,
  "longitude": 2.1734
}

Response: 201 Created
```

## 🗂️ Estructura del Proyecto

```
src/main/java/com/code/crafters/
├── CraftersApplication.java          # Main app
├── config/
│   ├── StripeConfig.java             # Configuración Stripe
│   ├── StripeProperties.java          # Propiedades Stripe
│   └── WebConfig.java                # Configuración web/MVC
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── EventController.java
│   ├── TicketController.java
│   ├── PaymentController.java
│   ├── ImageController.java
│   └── LocationController.java
├── service/
│   ├── AuthService/AuthServiceImpl
│   ├── UserService/UserServiceImpl
│   ├── EventService/EventServiceImpl
│   ├── TicketService/TicketServiceImpl
│   ├── PaymentService/PaymentServiceImpl
│   ├── ImageService/ImageServiceImpl
│   ├── LocationService/LocationServiceImpl
│   ├── EmailService/EmailServiceImpl
│   ├── QrService/QrServiceImpl
│   └── interfaces
├── repository/
│   ├── UserRepository.java
│   ├── EventRepository.java
│   ├── TicketRepository.java
│   └── LocationRepository.java
├── entity/
│   ├── User.java
│   ├── Event.java
│   ├── Ticket.java
│   ├── Location.java
│   └── enums/
├── dto/
│   ├── request/
│   │   ├── UserRequestDTO
│   │   ├── EventRequestDTO
│   │   ├── PaymentIntentRequestDTO
│   │   └── ...
│   └── response/
│       ├── UserResponseDTO
│       ├── EventResponseDTO
│       ├── AuthResponseDTO
│       └── ...
├── mapper/
│   ├── UserMapper.java
│   ├── EventMapper.java
│   ├── TicketMapper.java
│   ├── PaymentMapper.java
│   └── PageMapper.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   ├── JwtProperties.java
│   ├── SecurityConfig.java
│   ├── UserDetailsConfig.java
│   └── SecurityUtils.java
├── scheduler/
│   ├── TicketCleanupScheduler.java
│   └── PaymentReconciliationScheduler.java
├── specification/
│   └── EventSpecification.java
└── exception/
    ├── ResourceNotFoundException.java
    ├── ResourceAlreadyExistsException.java
    ├── ForbiddenOperationException.java
    └── GlobalExceptionHandler.java

src/test/java/com/code/crafters/
├── service/
│   ├── UserServiceImplTest.java
│   ├── EventServiceImplTest.java
│   └── TicketServiceImplTest.java
├── controller/
│   ├── AuthAndUserControllerIntegrationTest.java
│   └── EventAndTicketControllerIntegrationTest.java
└── e2e/
    └── EndToEndWorkflowTest.java

src/main/resources/
├── application.properties
├── application-test.properties
├── application-scheduler.properties
├── templates/
│   └── email-cancellation.html
```

## 🔐 Seguridad

### Autenticación JWT

- **Token**: JWT compuesto de header, payload y signature
- **Expiración**: Configurable (por defecto 7 días)
- **Renovación**: Token se incluye en cada login
- **Validación**: Interceptor de requests con JwtAuthFilter

### Autorización

- **Role-based**: Todos los usuarios son "ROLE_USER"
- **Endpoint protection**: Algunos endpoints requieren autenticación
- **Ownership validation**: Usuarios solo pueden modificar sus propios recursos

### Validaciones

- **Email**: Formato validado, único en BD
- **Alias**: 3-20 caracteres, único en BD
- **Contraseña**: Mínimo 6 caracteres, hasheada con BCrypt
- **Archivos**: Solo imágenes, máximo 5MB

### CORS

```javascript
Allowed Origins: http://localhost:5173
Allowed Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Allowed Headers: Authorization, Content-Type, Accept
```

## 💾 Base de Datos

### Diagrama ER

```
┌─────────────┐
│   users     │
├─────────────┤
│ id (PK)     │
│ email (UQ)  │
│ password    │
│ name        │
│ firstName   │
│ secondName  │
│ alias (UQ)  │
│ profileImg  │
└─────────────┘
      ↑
      │ (1:N)
      │
┌─────────────┐       ┌─────────────┐
│   events    │──────→│ locations   │
├─────────────┤ (N:1) ├─────────────┤
│ id (PK)     │       │ id (PK)     │
│ title       │       │ venue       │
│ description │       │ address     │
│ type (enum) │       │ city        │
│ date        │       │ country     │
│ time        │       │ latitude    │
│ maxAttendees│       │ longitude   │
│ price       │       └─────────────┘
│ imageUrl    │
│ category    │
│ createdBy   │
└─────────────┘
      ↑
      │ (1:N)
      │
┌─────────────┐
│   tickets   │
├─────────────┤
│ id (PK)     │
│ user_id (FK)│
│ event_id(FK)│
│ status      │
│ paymentId   │
│ qrUrl       │
│ verCode     │
│ createdAt   │
│ usedAt      │
└─────────────┘
```

### Migraciones

Las migraciones se ejecutan automáticamente gracias a `spring.jpa.hibernate.ddl-auto=update`.

Para resetear la BD:
```sql
DROP DATABASE crafters_db;
CREATE DATABASE crafters_db;
-- La app recreará las tablas automáticamente
```

## 📧 Email

### Configuración SMTP

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${GMAIL_ACCOUNT}
spring.mail.password=${GMAIL_SECRET}
```

### Plantillas

- **email-cancellation.html**: Notificación de cancelación de evento

### Servicio de Email

```java
emailService.sendCancellationEmail(to, eventTitle, userName, price);
emailService.sendBulkCancellationEmail(tickets, eventTitle, price);
```

## 🔄 Tareas Programadas

### TicketCleanupScheduler

```properties
ticket.cleanup.hours=24
```

- Limpia tickets PENDING no pagados después de 24 horas
- Elimina tickets de eventos pasados
- Ejecuta cada 60 segundos

### PaymentReconciliationScheduler

- Reconcilia tickets PENDING con Stripe
- Activa automáticamente tickets que ya fueron pagados
- Ejecuta cada 2 minutos

## 🐛 Manejo de Errores

### Excepciones Personalizadas

```java
- ResourceNotFoundException: 404
- ResourceAlreadyExistsException: 409
- ForbiddenOperationException: 403
- ValidationException: 400
- SecurityException: 401
```

### Respuesta de Error Estándar

```json
{
  "message": "Descripción del error",
  "status": 400,
  "timestamp": "2024-10-15T10:30:00"
}
```

## 📱 Integración Stripe

### Eventos Webhook

- `payment_intent.succeeded`: Ticket activado
- `payment_intent.payment_failed`: Log de error
- `payment_intent.canceled`: Log de cancelación

## 🤝 Contribuyendo

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la licencia MIT.

## 📞 Soporte

Para reportar bugs o sugerencias:
- Issues: Abre un issue en GitHub
- Email: codecraftersevents@gmail.com

## 🎯 Roadmap

- [ ] Integración con Google Calendar
- [ ] Sistema de notificaciones push
- [ ] Chat en tiempo real
- [ ] Calificaciones y reviews
- [ ] Certificados digitales
- [ ] Streaming de eventos
- [ ] API v2 con GraphQL

---

**Última actualización**: Abril 2026
**Versión**: 1.0.0