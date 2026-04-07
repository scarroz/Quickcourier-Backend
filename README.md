# 🚀 QuickCourier - Sistema de Entregas Urbanas

Sistema de entregas urbanas con cálculo dinámico de costos de envío y aplicación de extras mediante patrones de diseño.

---

## 📋 Características Principales

- ✅ **Autenticación JWT** con Access y Refresh Tokens
- ✅ **Rate Limiting** con Caffeine Cache (100 req/min configurable)
- ✅ **Caché inteligente** para optimización de performance (<200ms en horas pico)
- ✅ **Patrones de Diseño**:
  - 🎯 **Strategy Pattern**: Cálculo dinámico de envío
  - 🎨 **Decorator Pattern**: Extras apilables (express, frágil, seguro, etc.)
  - 🏭 **Factory Pattern**: Creación de pedidos
- ✅ **API REST** completamente documentada con Swagger/OpenAPI
- ✅ **Roles**: CUSTOMER y ADMIN
- ✅ **Gestión completa**: Usuarios, Productos, Pedidos, Pagos

---

## 🛠️ Stack Tecnológico

### Backend
- **Spring Boot 3.2.0** (Java 17)
- **Spring Security** + JWT (JJWT 0.12.3)
- **Spring Data JPA** + PostgreSQL
- **Caffeine Cache** para performance
- **SpringDoc OpenAPI 3** (Swagger)
- **Maven** como gestor de dependencias

### Base de Datos
- **PostgreSQL** con índices optimizados
- Migraciones con SQL scripts

---

## 📂 Estructura del Proyecto

```
quickcourier/
├── src/main/java/co/edu/unbosque/quickcourier/
│   ├── config/                 # Configuraciones
│   │   ├── SecurityConfig.java
│   │   ├── CacheConfig.java
│   │   └── OpenApiConfig.java
│   ├── controller/             # Controladores REST
│   │   ├── AuthController.java
│   │   ├── OrderController.java
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   ├── UserController.java
│   │   ├── AddressController.java
│   │   ├── PaymentController.java
│   │   └── ShippingController.java
│   ├── service/                # Interfaces de servicios
│   │   └── impl/               # Implementaciones
│   ├── repository/             # Repositorios JPA
│   ├── model/                  # Entidades JPA
│   ├── dto/                    # Data Transfer Objects
│   │   ├── request/
│   │   └── response/
│   ├── security/               # Seguridad JWT
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── RateLimitingFilter.java
│   ├── decorator/              # Decorator Pattern (Extras)
│   │   ├── OrderComponent.java
│   │   ├── BaseOrder.java
│   │   ├── OrderDecorator.java
│   │   ├── ExpressDecorator.java
│   │   ├── FragileDecorator.java
│   │   ├── InsuranceDecorator.java
│   │   ├── GiftWrapDecorator.java
│   │   └── CarbonNeutralDecorator.java
│   ├── strategy/               # Strategy Pattern (Shipping)
│   │   └── ShippingStrategyFactory.java
│   ├── factory/                # Factory Pattern (Orders)
│   │   └── OrderFactory.java
│   ├── exception/              # Manejo de excepciones
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   ├── UnauthorizedException.java
│   │   ├── ConflictException.java
│   │   └── GlobalExceptionHandler.java
│   └── mapper/                 # Mappers Entity <-> DTO
│       └── DataMapper.java
└── src/main/resources/
    ├── application.properties
    └── db/
        └── schema.sql          # Script de base de datos
```

---

## 🚀 Instalación y Configuración

### 1. Requisitos Previos
```bash
- Java 17 o superior
- Maven 3.8+
- PostgreSQL 14+
- Git
```

### 2. Clonar el Repositorio
```bash
git clone https://github.com/tuusuario/quickcourier.git
cd quickcourier
```

### 3. Configurar Base de Datos
```bash
# Crear base de datos
psql -U postgres
CREATE DATABASE quickcourier_db;

# Ejecutar script de schema
psql -U postgres -d quickcourier_db -f src/main/resources/db/schema.sql
```

### 4. Configurar application.properties
```properties
# Editar src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/quickcourier_db
spring.datasource.username=postgres
spring.datasource.password=tu_password

# Generar una clave secreta para JWT (Base64)
jwt.secret=TU_CLAVE_SECRETA_BASE64
```

### 5. Compilar y Ejecutar
```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

---

## 📖 Documentación de la API

### Swagger UI
Accede a la documentación interactiva en:
```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principales

#### Autenticación
```http
POST /api/auth/register        # Registrar usuario
POST /api/auth/login           # Iniciar sesión
POST /api/auth/refresh-token   # Refrescar token
POST /api/auth/logout          # Cerrar sesión
```

#### Productos (Público - Lectura)
```http
GET  /api/products                    # Listar productos
GET  /api/products/{id}               # Obtener por ID
GET  /api/products/sku/{sku}          # Obtener por SKU
GET  /api/products/category/{id}      # Por categoría
GET  /api/products/search?q=...       # Buscar
```

#### Pedidos (Autenticado)
```http
POST   /api/orders                    # Crear pedido
GET    /api/orders/my-orders          # Mis pedidos
GET    /api/orders/{id}               # Obtener por ID
PATCH  /api/orders/{id}/confirm       # Confirmar
PATCH  /api/orders/{id}/cancel        # Cancelar
```

#### Pagos (Autenticado)
```http
POST /api/payments              # Crear pago
POST /api/payments/{id}/process # Procesar pago
```

---

## 🔐 Autenticación

### 1. Registrarse
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "password123",
    "firstName": "Juan",
    "lastName": "Pérez",
    "phone": "3001234567"
  }'
```

### 2. Iniciar Sesión
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "password123"
  }'
```

**Respuesta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CUSTOMER"
  }
}
```

### 3. Usar el Token
```bash
curl -X GET http://localhost:8080/api/orders/my-orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

## 🎨 Patrones de Diseño Implementados

### 1. Strategy Pattern - Cálculo de Envío
**Ubicación**: `co.edu.unbosque.quickcourier.strategy`

Permite cambiar dinámicamente las reglas de cálculo de envío:
- Promo fin de semana (20% descuento)
- Primera compra (envío gratis)
- Tarifa plana por zona
- Basado en peso

**Ejemplo de uso:**
```java
ShippingStrategyFactory factory = ...;
ShippingCalculationResult result = factory.calculateShipping(order);
```

### 2. Decorator Pattern - Extras de Envío
**Ubicación**: `co.edu.unbosque.quickcourier.decorator`

Permite apilar extras de forma flexible:
- 🚀 **EXPRESS**: Entrega en < 2 horas (+$15,000)
- 📦 **FRAGILE**: Manejo frágil (+$5,000)
- 🛡️ **INSURANCE**: Seguro 5% del subtotal
- 🎁 **GIFT_WRAP**: Empaque de regalo (+$8,000)
- 🌱 **CARBON_NEUTRAL**: Huella carbono neutra (+$3,000)

**Ejemplo de uso:**
```java
OrderComponent order = new BaseOrder(baseOrder);
order = new ExpressDecorator(order, expressExtra);
order = new InsuranceDecorator(order, insuranceExtra);
BigDecimal totalCost = order.getCost();
```

### 3. Factory Pattern - Creación de Pedidos
**Ubicación**: `co.edu.unbosque.quickcourier.factory`

Encapsula la lógica compleja de creación de pedidos con items.

---

## ⚡ Performance y Caché

### Caffeine Cache
Configurado para responder en **< 200ms** durante horas pico:

| Cache | TTL | Max Size | Uso |
|-------|-----|----------|-----|
| orders | 5 min | 1000 | Pedidos recientes |
| products | 10 min | 5000 | Catálogo |
| categories | 30 min | 100 | Categorías |
| shippingRules | 5 min | 50 | Reglas de envío |

### Rate Limiting
- **100 requests/minuto** por IP o usuario
- Configurable en `application.properties`:
```properties
rate-limit.requests-per-minute=100
```

---

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=AuthServiceTest
```

---

## 📊 Base de Datos

### Tablas Principales
- `users` - Usuarios del sistema
- `addresses` - Direcciones de entrega
- `categories` - Categorías de productos
- `products` - Catálogo de productos
- `orders` - Pedidos
- `order_items` - Items de pedidos
- `order_extras` - Extras aplicados
- `shipping_rules` - Reglas de envío
- `shipping_extras` - Extras disponibles
- `payments` - Transacciones de pago
- `refresh_tokens` - Tokens de refresco
- `token_blacklist` - Tokens revocados

### Usuarios de Prueba
```sql
-- Admin (password: admin123)
Email: admin@quickcourier.com
Role: ADMIN

-- Cliente (password: customer123)
Email: juan.perez@email.com
Role: CUSTOMER
```

---

## 🐛 Troubleshooting

### Error: "JWT secret must be at least 256 bits"
```properties
# Generar una clave válida (Base64, 256+ bits)
jwt.secret=$(openssl rand -base64 64)
```

### Error: "Rate limit exceeded"
Espera 1 minuto o aumenta el límite en `application.properties`

### Error de conexión a PostgreSQL
```bash
# Verificar que PostgreSQL esté corriendo
sudo systemctl status postgresql

# Verificar credenciales en application.properties
```

---

## 👥 Autores

**Ingeniería de Software 2**  
Universidad El Bosque
Sebastian Ernesto Carroz Añez
Sophy Valentina Guiza Valencia
Andres Camilo Guerrero Mateus
Cristhian Camilo Diaz Romero

---

**¡Gracias por usar QuickCourier! 🚀📦**
