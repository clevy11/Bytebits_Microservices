# Testing the ByteBites Platform

This guide provides step-by-step instructions to test the functionality of the ByteBites microservices platform. You will need a command-line tool like `cURL` and a web browser.

---

## ▶️ 1. Running the Platform

Ensure all microservices are running. You should start them in this order:
1.  `discovery-server`
2.  `config-server`
3.  `auth-service`
4.  `restaurant-service`
5.  `order-service`
6.  `api-gateway`

---

## 🔐 2. Authentication and User Management (`auth-service`)

All requests go through the `api-gateway` on port `8080`.

### 2.1. Register Users

We will register three users with different roles.

**A. Register a Customer**
```bash
curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{
  "name": "Alice Customer",
  "email": "alice@example.com",
  "password": "password123",
  "role": "ROLE_CUSTOMER"
}'
```

**B. Register a Restaurant Owner**
```bash
curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{
  "name": "Bob Owner",
  "email": "bob@example.com",
  "password": "password123",
  "role": "ROLE_RESTAURANT_OWNER"
}'
```

**C. Register an Admin**
```bash
curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{
  "name": "Eve Admin",
  "email": "eve@example.com",
  "password": "password123",
  "role": "ROLE_ADMIN"
}'
```

### 2.2. Login and Get JWT

Now, log in as the customer to get a JWT token. Save the token for the next steps.

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{
  "email": "alice@example.com",
  "password": "password123"
}'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "alice@example.com",
  "name": "Alice Customer",
  "role": "ROLE_CUSTOMER"
}
```

### 2.3. Test OAuth2 Login (Google)

1.  Open your browser and navigate to: `http://localhost:8080/auth/oauth2/google`
2.  You will be redirected to the Google login page.
3.  After authenticating with Google, you will be redirected back, and the final response in your browser will be a JSON object containing a JWT.

---

## 🥘 3. Restaurant Management (`restaurant-service`)

First, log in as the restaurant owner (`bob@example.com`) to get a JWT. Export it as a variable.

```bash
export OWNER_TOKEN="<your-restaurant-owner-jwt>"
```

### 3.1. Create a Restaurant (as Owner)

This should succeed because the user has the `ROLE_RESTAURANT_OWNER` role.

```bash
curl -X POST http://localhost:8080/api/restaurants -H "Content-Type: application/json" -H "Authorization: Bearer $OWNER_TOKEN" -d '{
  "name": "Bob''s Burgers",
  "address": "123 Ocean Ave",
  "cuisineType": "Burgers"
}'
```

### 3.2. View Restaurants (as Customer)

Now, log in as the customer (`alice@example.com`) and get a new JWT.

```bash
export CUSTOMER_TOKEN="<your-customer-jwt>"
```

Any authenticated user can view restaurants.
```bash
curl http://localhost:8080/api/restaurants -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

### 3.3. Fail to Create a Restaurant (as Customer)

This request should be **denied** with a `403 Forbidden` error because the customer does not have the required role.

```bash
curl -X POST http://localhost:8080/api/restaurants -H "Content-Type: application/json" -H "Authorization: Bearer $CUSTOMER_TOKEN" -d '{
  "name": "Alice''s Cafe",
  "address": "456 Maple St",
  "cuisineType": "Coffee"
}'
```

---

## 🛒 4. Order Management (`order-service`)

Use the customer token (`$CUSTOMER_TOKEN`) for these steps.

### 4.1. Place an Order (as Customer)

This should succeed. Assume the restaurant created by Bob has an ID of `1`.

```bash
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -H "Authorization: Bearer $CUSTOMER_TOKEN" -d '{
  "restaurantId": 1,
  "items": [
    {
      "name": "Cheeseburger",
      "quantity": 1,
      "price": 9.99
    }
  ]
}'
```

### 4.2. View Your Orders (as Customer)

```bash
curl http://localhost:8080/api/orders -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

### 4.3. Fail to View Restaurant Orders (as Customer)

This request should be **denied** with a `403 Forbidden` error.

```bash
curl http://localhost:8080/api/orders/restaurant/1 -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

### 4.4. View and Update Order Status (as Owner)

Use the owner's token (`$OWNER_TOKEN`). Assume the order placed by Alice has an ID of `1`.

**A. View orders for the restaurant:**
```bash
curl http://localhost:8080/api/orders/restaurant/1 -H "Authorization: Bearer $OWNER_TOKEN"
```

**B. Update order status:**
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?status=CONFIRMED&restaurantId=1" -H "Authorization: Bearer $OWNER_TOKEN"
```

---

## 📢 5. Verify Event-Driven Communication

After you successfully place an order in **Step 4.1**, the `order-service` publishes an `OrderPlacedEvent`. You can verify that the other services received this event by checking their console logs.

### 5.1. Check `restaurant-service` Log

Look for a log entry similar to this in the `restaurant-service` console output:

```text
INFO --- [nt-service-queue]] c.b.r.l.OrderEventListener      : Received order for restaurant 1: Order ID 1. Starting preparation...
```

### 5.2. Check `notification-service` Log

Simultaneously, look for a log entry like this in the `notification-service` console output:

```text
INFO --- [ion-service-queue]] c.b.n.l.OrderEventListener      : Received order placed event for customer 1: Order ID 1. Sending confirmation email...
```

Seeing these log messages confirms that the event was successfully published by the `order-service` and consumed independently by both the `restaurant-service` and `notification-service`.

---

## ⚡ 6. Test Circuit Breaker Resilience

The `order-service` is configured with a circuit breaker that will "open" if it detects repeated failures, preventing cascading failures. When the circuit is open, it will immediately return a fallback response without attempting to process the order.

### 6.1. How to Test

To test this, you would need to simulate a failure condition. For example, you could temporarily shut down the database that the `order-service` depends on.

1.  **Stop the PostgreSQL database container.**
2.  **Attempt to place an order** using the `cURL` command from **Step 4.1** multiple times (e.g., 5-10 times).
3.  **Observe the responses:**
    *   The first few requests might take a moment to fail as they attempt to connect to the database.
    *   After the failure threshold is met (50% of the last 10 requests), the circuit breaker will open.
    *   Subsequent requests will fail instantly and return the fallback response defined in `OrderService.java`.

**Expected Fallback Response:**
```json
{
    "id": null,
    "customerId": 1, // Or the customer ID you used
    "restaurantId": 1, // Or the restaurant ID you used
    "orderItems": null,
    "totalAmount": 0,
    "status": "PENDING",
    "deliveryAddress": "Fallback address",
    "specialInstructions": null,
    "createdAt": null
}
```

4.  **Restart the database.** After waiting for the `wait-duration-in-open-state` (10 seconds), the circuit will move to a "half-open" state, and subsequent requests will succeed again.
