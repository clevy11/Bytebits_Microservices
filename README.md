# 🍽️ ByteBites Platform

A microservices-based food delivery platform connecting customers with local restaurants.

## 🏗️ Architecture Overview

ByteBites is built using Spring Boot microservices with the following components:

### Core Infrastructure
- **Discovery Server** - Eureka service registry
- **Config Server** - Centralized configuration management
- **API Gateway** - JWT validation and request routing

### Business Services
- **Auth Service** - User authentication and JWT management
- **Restaurant Service** - Restaurant and menu management
- **Order Service** - Order processing and management
- **Notification Service** - Email/push notifications

### Supporting Infrastructure
- **RabbitMQ** - Event-driven messaging
- **Resilience4j** - Circuit breaker and resilience patterns

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL (via Docker)

### Environment Setup

**⚠️ IMPORTANT: Set up environment variables before starting services**

1. **Copy the example environment file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` with your actual values:**
   ```bash
   # Database
   POSTGRES_PORT=5433
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=your-secure-password
   
   # JWT
   JWT_SECRET=your-very-secret-jwt-key-at-least-32-characters-long
   JWT_EXPIRATION=86400000
   
   # RabbitMQ
   RABBITMQ_HOST=localhost
   RABBITMQ_PORT=5672
   RABBITMQ_USER=admin
   RABBITMQ_PASSWORD=your-secure-rabbitmq-password
   
   # OAuth2 Google (Optional - for OAuth2 login)
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   
   # OAuth2 GitHub (Optional - for OAuth2 login)
   GITHUB_CLIENT_ID=your-github-client-id
   GITHUB_CLIENT_SECRET=your-github-client-secret
   ```

3. **Load environment variables:**
   ```bash
   # For local development
   export $(grep -v '^#' .env | xargs)
   ```

### Service Startup Order

1. **Start Infrastructure**
   ```bash
   docker-compose up -d
   ```

2. **Start Core Services**
   ```bash
   # Terminal 1: Discovery Server
   cd discovery-server && mvn spring-boot:run
   
   # Terminal 2: Config Server
   cd config-server && mvn spring-boot:run
   
   # Terminal 3: API Gateway
   cd api-gateway && mvn spring-boot:run
   ```

3. **Start Business Services**
   ```bash
   # Terminal 4: Auth Service
   cd auth-service && mvn spring-boot:run
   
   # Terminal 5: Restaurant Service
   cd restaurant-service && mvn spring-boot:run
   
   # Terminal 6: Order Service
   cd order-service && mvn spring-boot:run
   
   # Terminal 7: Notification Service
   cd notification-service && mvn spring-boot:run
   ```

### Alternative: Using IntelliJ IDEA

If running from IntelliJ IDEA:

1. **Set environment variables in Run Configuration:**
   - Go to Run/Debug Configuration for each service
   - Add environment variables from your `.env` file
   - Example for Auth Service:
     ```
     JWT_SECRET=your-secret-key
     POSTGRES_PASSWORD=your-password
     GOOGLE_CLIENT_ID=your-client-id
     GOOGLE_CLIENT_SECRET=your-client-secret
     ```

## 🔐 Authentication Flow

1. **Register/Login** via Auth Service
2. **Receive JWT** with user roles and claims
3. **Include JWT** in Authorization header for all API calls
4. **API Gateway** validates JWT and forwards user info to services

### JWT Testing with Postman

1. **Register a customer:**
   ```
   POST http://localhost:8080/auth/register
   Content-Type: application/json
   
   {
     "email": "customer@example.com",
     "password": "password123",
     "name": "John Doe",
     "role": "ROLE_CUSTOMER"
   }
   ```

2. **Login to get JWT:**
   ```
   POST http://localhost:8080/auth/login
   Content-Type: application/json
   
   {
     "email": "customer@example.com",
     "password": "password123"
   }
   ```

3. **Use JWT in subsequent requests:**
   ```
   GET http://localhost:8080/api/restaurants
   Authorization: Bearer <your-jwt-token>
   ```

## 🎯 Role-Based Access Control

| Role | Permissions |
|------|-------------|
| `ROLE_CUSTOMER` | Place orders, view own orders, browse restaurants |
| `ROLE_RESTAURANT_OWNER` | Manage own restaurant, view restaurant orders |
| `ROLE_ADMIN` | Full system access, user management |

## 📊 Service Endpoints

### Auth Service (Port 8081)
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/refresh` - Refresh JWT token
- `GET /auth/oauth2/authorize/google` - Google OAuth2 login
- `GET /auth/oauth2/authorize/github` - GitHub OAuth2 login

### Restaurant Service (Port 8082)
- `GET /api/restaurants` - List all restaurants
- `POST /api/restaurants` - Create restaurant (ROLE_RESTAURANT_OWNER)
- `PUT /api/restaurants/{id}` - Update restaurant (owner only)
- `GET /api/restaurants/{id}/orders` - Get restaurant orders (owner only)

### Order Service (Port 8083)
- `POST /api/orders` - Place order (ROLE_CUSTOMER)
- `GET /api/orders` - Get user orders
- `GET /api/orders/{id}` - Get specific order (owner only)
- `PUT /api/orders/{id}/status` - Update order status

### Admin Endpoints (Port 8080 via Gateway)
- `GET /admin/users` - List all users (ROLE_ADMIN)
- `GET /admin/orders` - List all orders (ROLE_ADMIN)

## 🔗 Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| Eureka Dashboard | http://localhost:8761 | Service registry |
| API Gateway | http://localhost:8080 | Main entry point |
| Auth Service | http://localhost:8081 | Authentication |
| Restaurant Service | http://localhost:8082 | Restaurant management |
| Order Service | http://localhost:8083 | Order processing |
| Notification Service | http://localhost:8084 | Notifications |

## 🧪 Testing Scenarios

### 1. Customer Order Flow
1. Register as customer
2. Login to get JWT
3. Browse restaurants
4. Place an order
5. Check order status

### 2. Restaurant Owner Flow
1. Register as restaurant owner
2. Create restaurant profile
3. Add menu items
4. View incoming orders

### 3. Circuit Breaker Testing
1. Start all services
2. Place an order
3. Stop restaurant service
4. Place another order (should trigger fallback)
5. Check `/actuator/circuitbreakerevents` on order service

### 4. Event-Driven Flow
1. Place an order
2. Check notification service logs
3. Verify restaurant service received order event

## 🛠️ Development

### Project Structure
```
bytesbitsPlatform/
├── discovery-server/
├── config-server/
├── api-gateway/
├── auth-service/
├── restaurant-service/
├── order-service/
├── notification-service/
├── docker-compose.yml
├── .env.example
└── README.md
```

### Configuration
- All services use Spring Cloud Config for centralized configuration
- External config repository: [ByteBites Config Repo](https://github.com/your-org/bytebites-config)
- Profiles: `dev`, `prod`

### Monitoring
- Actuator endpoints enabled on all services
- Circuit breaker metrics available at `/actuator/circuitbreakerevents`
- Health checks at `/actuator/health`

## 🐳 Docker Deployment

```bash
# Build all services
mvn clean package -DskipTests

# Start with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f
```

## 📈 Resilience Features

- **Circuit Breaker**: Protects services from cascading failures
- **Retry**: Automatic retry for transient failures
- **Timeout**: Configurable timeouts for service calls
- **Fallback**: Graceful degradation when services are unavailable

## 🔒 Security Features

- JWT-based authentication
- Role-based access control (RBAC)
- Resource ownership validation
- Secure service-to-service communication
- Password encryption with BCrypt
- OAuth2 integration (Google & GitHub)

## 📝 API Documentation

Each service includes Swagger UI for API documentation:
- Auth Service: http://localhost:8081/swagger-ui.html
- Restaurant Service: http://localhost:8082/swagger-ui.html
- Order Service: http://localhost:8083/swagger-ui.html

## 🔐 Environment Variables & Security

### Required Environment Variables

All sensitive configuration must be set via environment variables. **Never put secrets in application.yml!**

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for JWT signing | `your-very-secret-key-32-chars` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `your-secure-password` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `your-secure-password` |

### Optional Environment Variables (OAuth2)

| Variable | Description | Example |
|----------|-------------|---------|
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | `123456789-abc.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | `GOCSPX-xxxxxxxxxxxxxxxx` |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client ID | `your-github-client-id` |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 client secret | `your-github-client-secret` |

### Setting Up OAuth2 (Optional)

1. **Google OAuth2:**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create OAuth2 credentials
   - Set redirect URI: `http://localhost:8081/auth/oauth2/callback/google`

2. **GitHub OAuth2:**
   - Go to [GitHub Developer Settings](https://github.com/settings/developers)
   - Create OAuth App
   - Set callback URL: `http://localhost:8081/auth/oauth2/callback/github`

### Security Best Practices

- ✅ Use strong, unique passwords
- ✅ Generate secure JWT secrets (at least 32 characters)
- ✅ Never commit `.env` files to version control
- ✅ Use different secrets for development and production
- ✅ Rotate secrets regularly in production

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## External Config Repo: BytesBits_ConfigRepo

This project uses a separate GitHub repository for centralized configuration, as required for Spring Cloud Config.

- **Config Repo URL:** https://github.com/clevy11/BytesBits_ConfigRepo.git
- **Structure:**
  - Place service-specific config files in folders or with naming conventions (e.g., `auth-service.yml`, `restaurant-service.yml`, etc.)
  - The config-server is configured to pull from this repo (see `config-server/src/main/resources/application.yml`).

### Example Structure

```
BytesBits_ConfigRepo/
  auth-service.yml
  restaurant-service.yml
  order-service.yml
  notification-service.yml
  api-gateway.yml
  discovery-server.yml
```

- Each file contains the `application.yml` content for the corresponding service.
- You can also use folders for profiles (e.g., `auth-service-dev.yml`, `auth-service-prod.yml`).

### How to Use
1. Clone the config repo and add your config files.
2. Push changes to GitHub.
3. The config-server will automatically serve these configs to all services.

See the `config-server/README.md` for more details on configuration and usage. 