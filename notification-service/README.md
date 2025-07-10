## API Testing with Swagger UI

This service provides interactive API documentation and testing via Swagger UI.

### Accessing Swagger UI

1. Start the service:
   ```sh
   ./mvnw spring-boot:run
   ```
2. Open your browser and go to:
   - http://localhost:8084/swagger-ui.html
   - or http://localhost:8084/swagger-ui/index.html
   (Replace 8084 with your configured port if different.)

### Authorizing with JWT

- For secured endpoints, click the **Authorize** button in Swagger UI and enter your JWT token as:
  ```
  Bearer <your-jwt-token>
  ```
- Obtain a JWT by logging in via the `/login` or OAuth2 endpoints.

### Notes
- Swagger UI is intended for development and should be restricted or disabled in production environments. 