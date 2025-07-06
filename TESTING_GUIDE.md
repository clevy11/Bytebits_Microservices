# 🧪 ByteBites Platform Testing Guide

## 🚀 Prerequisites

Before starting the tests, ensure you have:

1. **Java 17+** installed
2. **Maven 3.6+** installed
3. **Docker & Docker Compose** installed
4. **Postman** or similar API testing tool
5. **All services built and ready to run**

## 📋 Test Setup

### 1. Start Infrastructure Services

```bash
# Start Docker containers
docker-compose up -d

# Verify services are running
docker-compose ps
```

Expected output:
```
Name                    Command               State           Ports
bytebites-postgres     docker-entrypoint.sh postgres    Up      0.0.0.0:5432->5432/tcp
bytebites-rabbitmq     docker-entrypoint.sh rabbi ...   Up      0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
bytebites-redis        docker-entrypoint.sh redis ...   Up      0.0.0.0:6379->6379/tcp
```

### 2. Start Core Services (in separate terminals)

```bash
# Terminal 1: Discovery Server
cd discovery-server && mvn spring-boot:run

# Terminal 2: Config Server
cd config-server && mvn spring-boot:run

# Terminal 3: API Gateway
cd api-gateway && mvn spring-boot:run
```

### 3. Start Business Services (in separate terminals)

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

### 4. Verify Service Registration

Visit: http://localhost:8761 (Eureka Dashboard)

You should see all services registered:
- `discovery-server`
- `config-server`
- `api-gateway`
- `auth-service`
- `restaurant-service`
- `order-service`
- `notification-service`

## 🔐 Authentication Testing

### Test 1: User Registration

**Request:**
```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "password123",
  "name": "John Customer",
  "role": "ROLE_CUSTOMER"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "customer@example.com",
  "name": "John Customer",
  "role": "ROLE_CUSTOMER"
}
```

### Test 2: User Login

**Request:**
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "customer@example.com",
  "name": "John Customer",
  "role": "ROLE_CUSTOMER"
}
```

### Test 3: Restaurant Owner Registration

**Request:**
```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "email": "owner@restaurant.com",
  "password": "password123",
  "name": "Restaurant Owner",
  "role": "ROLE_RESTAURANT_OWNER"
}
```

## 🏪 Restaurant Service Testing

### Test 4: Create Restaurant (Restaurant Owner)

**Request:**
```http
POST http://localhost:8080/api/restaurants
Authorization: Bearer <RESTAURANT_OWNER_JWT>
Content-Type: application/json

{
  "name": "Pizza Palace",
  "description": "Best pizza in town",
  "address": "123 Main St, City",
  "phone": "555-0123"
}
```

**Expected Response:**
```json
{
  "id": 1,
  "name": "Pizza Palace",
  "description": "Best pizza in town",
  "address": "123 Main St, City",
  "phone": "555-0123",
  "ownerId": 2,
  "active": true,
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

### Test 5: Get All Restaurants (Customer)

**Request:**
```http
GET http://localhost:8080/api/restaurants
Authorization: Bearer <CUSTOMER_JWT>
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "Pizza Palace",
    "description": "Best pizza in town",
    "address": "123 Main St, City",
    "phone": "555-0123",
    "active": true
  }
]
```

### Test 6: Get My Restaurants (Restaurant Owner)

**Request:**
```http
GET http://localhost:8080/api/restaurants/my-restaurants
Authorization: Bearer <RESTAURANT_OWNER_JWT>
```

## 🛒 Order Service Testing

### Test 7: Place Order (Customer)

**Request:**
```http
POST http://localhost:8080/api/orders
Authorization: Bearer <CUSTOMER_JWT>
Content-Type: application/json

{
  "restaurantId": 1,
  "items": [
    {
      "menuItemId": 1,
      "quantity": 2
    },
    {
      "menuItemId": 2,
      "quantity": 1
    }
  ],
  "deliveryAddress": "456 Customer St, City",
  "specialInstructions": "Extra cheese please"
}
```

**Expected Response:**
```json
{
  "id": 1,
  "customerId": 1,
  "restaurantId": 1,
  "status": "PENDING",
  "totalAmount": 30.00,
  "deliveryAddress": "456 Customer St, City",
  "specialInstructions": "Extra cheese please",
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

### Test 8: Get My Orders (Customer)

**Request:**
```http
GET http://localhost:8080/api/orders
Authorization: Bearer <CUSTOMER_JWT>
```

### Test 9: Get Restaurant Orders (Restaurant Owner)

**Request:**
```http
GET http://localhost:8080/api/orders/restaurant/1
Authorization: Bearer <RESTAURANT_OWNER_JWT>
```

### Test 10: Update Order Status (Restaurant Owner)

**Request:**
```http
PUT http://localhost:8080/api/orders/1/status?status=CONFIRMED&restaurantId=1
Authorization: Bearer <RESTAURANT_OWNER_JWT>
```

## 🔒 Security Testing

### Test 11: Unauthorized Access

**Request:**
```http
GET http://localhost:8080/api/orders
```

**Expected Response:**
```http
HTTP/1.1 401 Unauthorized
```

### Test 12: Wrong Role Access

**Request:**
```http
POST http://localhost:8080/api/restaurants
Authorization: Bearer <CUSTOMER_JWT>
Content-Type: application/json

{
  "name": "Unauthorized Restaurant",
  "description": "This should fail",
  "address": "123 Unauthorized St"
}
```

**Expected Response:**
```http
HTTP/1.1 403 Forbidden
```

### Test 13: Resource Ownership

**Request:**
```http
PUT http://localhost:8080/api/restaurants/1
Authorization: Bearer <CUSTOMER_JWT>
Content-Type: application/json

{
  "name": "Unauthorized Update",
  "description": "This should fail",
  "address": "123 Unauthorized St"
}
```

**Expected Response:**
```http
HTTP/1.1 404 Not Found
```

## 🛡️ Circuit Breaker Testing

### Test 14: Circuit Breaker Activation

1. **Start all services**
2. **Place an order successfully**
3. **Stop the restaurant service**
4. **Place another order**

**Expected Behavior:**
- First order: Success
- Second order: Circuit breaker triggers fallback
- Check logs for fallback message

### Test 15: Circuit Breaker Monitoring

**Request:**
```http
GET http://localhost:8083/actuator/circuitbreakerevents
```

**Expected Response:**
```json
{
  "circuitBreakerEvents": [
    {
      "circuitBreakerName": "orderService",
      "type": "FAILURE",
      "creationTime": "2024-01-01T12:00:00Z",
      "errorMessage": "Connection refused"
    }
  ]
}
```

## 🔄 Event-Driven Testing

### Test 16: Order Event Processing

1. **Place an order**
2. **Check notification service logs**
3. **Check restaurant service logs**

**Expected Logs:**

Notification Service:
```
=== NOTIFICATION SERVICE ===
📧 Sending email notification for order: Order placed: ID=1, Customer=1, Restaurant=1, Amount=30.00
📱 Sending push notification for order: Order placed: ID=1, Customer=1, Restaurant=1, Amount=30.00
✅ Notifications sent successfully!
=============================
```

Restaurant Service:
```
Restaurant service received order: Order placed: ID=1, Customer=1, Restaurant=1, Amount=30.00
```

## 📊 Admin Testing

### Test 17: Admin User Registration

**Request:**
```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "email": "admin@bytebites.com",
  "password": "admin123",
  "name": "System Admin",
  "role": "ROLE_ADMIN"
}
```

### Test 18: Admin Endpoints

**Request:**
```http
GET http://localhost:8080/admin/users
Authorization: Bearer <ADMIN_JWT>
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "email": "customer@example.com",
    "name": "John Customer",
    "role": "ROLE_CUSTOMER",
    "enabled": true
  },
  {
    "id": 2,
    "email": "owner@restaurant.com",
    "name": "Restaurant Owner",
    "role": "ROLE_RESTAURANT_OWNER",
    "enabled": true
  }
]
```

## 🔍 API Documentation Testing

### Test 19: Swagger UI Access

Visit the following URLs to access API documentation:

- **Auth Service**: http://localhost:8081/swagger-ui.html
- **Restaurant Service**: http://localhost:8082/swagger-ui.html
- **Order Service**: http://localhost:8083/swagger-ui.html

## 📈 Health Check Testing

### Test 20: Service Health

**Requests:**
```http
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health
GET http://localhost:8082/actuator/health
GET http://localhost:8083/actuator/health
GET http://localhost:8084/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

## 🧪 Integration Test Scenarios

### Scenario 1: Complete Customer Journey

1. **Register as customer**
2. **Login to get JWT**
3. **Browse restaurants**
4. **Place an order**
5. **Check order status**
6. **Verify notifications sent**

### Scenario 2: Complete Restaurant Owner Journey

1. **Register as restaurant owner**
2. **Login to get JWT**
3. **Create restaurant profile**
4. **View incoming orders**
5. **Update order status**

### Scenario 3: System Resilience

1. **Start all services**
2. **Place multiple orders**
3. **Stop restaurant service**
4. **Place more orders (should trigger fallback)**
5. **Restart restaurant service**
6. **Verify circuit breaker recovery**

## 🐛 Troubleshooting

### Common Issues

1. **Service not starting**
   - Check if required ports are available
   - Verify database connection
   - Check application logs

2. **JWT validation failing**
   - Ensure JWT secret is consistent across services
   - Check token expiration
   - Verify token format

3. **Circuit breaker not working**
   - Check Resilience4j configuration
   - Verify service dependencies
   - Check actuator endpoints

4. **Events not being processed**
   - Verify RabbitMQ is running
   - Check queue configuration
   - Review service logs

### Debug Commands

```bash
# Check service logs
docker-compose logs -f

# Check specific service
docker-compose logs -f rabbitmq

# Check service health
curl http://localhost:8080/actuator/health

# Check Eureka registry
curl http://localhost:8761/eureka/apps
```

## 📝 Test Results Template

Create a test results file to track your testing:

```markdown
# Test Results - ByteBites Platform

## Authentication Tests
- [ ] User Registration
- [ ] User Login
- [ ] JWT Validation
- [ ] Role-based Access

## Restaurant Service Tests
- [ ] Create Restaurant
- [ ] Get Restaurants
- [ ] Update Restaurant
- [ ] Owner-only Access

## Order Service Tests
- [ ] Place Order
- [ ] Get Orders
- [ ] Update Status
- [ ] Circuit Breaker

## Event-Driven Tests
- [ ] Order Event Publishing
- [ ] Notification Processing
- [ ] Restaurant Event Handling

## Security Tests
- [ ] Unauthorized Access
- [ ] Wrong Role Access
- [ ] Resource Ownership

## Admin Tests
- [ ] Admin Registration
- [ ] Admin Endpoints
- [ ] User Management

## Notes
- Test Date: _________
- Tester: _________
- Issues Found: _________
- Recommendations: _________
``` 