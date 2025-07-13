## API Testing with Swagger UI

This service provides interactive API documentation and testing via Swagger UI.

### Accessing Swagger UI

1. Start the service:
   ```sh
   ./mvnw spring-boot:run
   ```
2. Open your browser and go to:
   - http://localhost:8083/swagger-ui.html
   - or http://localhost:8083/swagger-ui/index.html
   (Replace 8083 with your configured port if different.)

### Authorizing with JWT

- For secured endpoints, click the **Authorize** button in Swagger UI and enter your JWT token as:
  ```
  Bearer <your-jwt-token>
  ```
- Obtain a JWT by logging in via the `/login` or OAuth2 endpoints.

### Testing Order Creation

To test the creation of a new order, use the `POST /api/orders` endpoint.

**Endpoint:** `POST /api/orders`

**Description:** Creates a new order. This endpoint is secured and requires a valid JWT token with the appropriate customer role.

**Sample Request Body:**

Below is an example of how to structure the JSON payload. Note that you can now specify the item `name` and `price` directly, without needing a `menuItemId`.

```json
{
  "restaurantId": 1,
  "items": [
    {
      "name": "Margherita Pizza",
      "price": 14.50,
      "quantity": 1
    },
    {
      "name": "Garlic Bread",
      "price": 6.00,
      "quantity": 2
    }
  ],
  "deliveryAddress": "123 Foodie Lane, Flavor Town, USA",
  "specialInstructions": "Please make the pizza extra crispy."
}
```

### Notes
- Swagger UI is intended for development and should be restricted or disabled in production environments. 