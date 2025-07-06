#!/bin/bash

# ByteBites Platform - Service Startup Script
# This script starts all services in the correct order

echo "🍽️  Starting ByteBites Platform..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java 17+ is installed
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

print_success "Prerequisites check passed!"

# Step 1: Start infrastructure services
print_status "Step 1: Starting infrastructure services (Docker Compose)..."
docker-compose up -d

if [ $? -eq 0 ]; then
    print_success "Infrastructure services started successfully!"
else
    print_error "Failed to start infrastructure services"
    exit 1
fi

# Wait for infrastructure to be ready
print_status "Waiting for infrastructure services to be ready..."
sleep 10

# Step 2: Start core services
print_status "Step 2: Starting core services..."

# Start Discovery Server
print_status "Starting Discovery Server..."
cd discovery-server
mvn spring-boot:run > ../logs/discovery-server.log 2>&1 &
DISCOVERY_PID=$!
cd ..

# Wait for discovery server to start
sleep 15

# Start Config Server
print_status "Starting Config Server..."
cd config-server
mvn spring-boot:run > ../logs/config-server.log 2>&1 &
CONFIG_PID=$!
cd ..

# Wait for config server to start
sleep 10

# Start API Gateway
print_status "Starting API Gateway..."
cd api-gateway
mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..

# Wait for gateway to start
sleep 10

print_success "Core services started!"

# Step 3: Start business services
print_status "Step 3: Starting business services..."

# Start Auth Service
print_status "Starting Auth Service..."
cd auth-service
mvn spring-boot:run > ../logs/auth-service.log 2>&1 &
AUTH_PID=$!
cd ..

# Wait for auth service to start
sleep 10

# Start Restaurant Service
print_status "Starting Restaurant Service..."
cd restaurant-service
mvn spring-boot:run > ../logs/restaurant-service.log 2>&1 &
RESTAURANT_PID=$!
cd ..

# Wait for restaurant service to start
sleep 10

# Start Order Service
print_status "Starting Order Service..."
cd order-service
mvn spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!
cd ..

# Wait for order service to start
sleep 10

# Start Notification Service
print_status "Starting Notification Service..."
cd notification-service
mvn spring-boot:run > ../logs/notification-service.log 2>&1 &
NOTIFICATION_PID=$!
cd ..

# Wait for notification service to start
sleep 10

print_success "All services started successfully!"

# Save PIDs to file for later cleanup
echo "DISCOVERY_PID=$DISCOVERY_PID" > .service-pids
echo "CONFIG_PID=$CONFIG_PID" >> .service-pids
echo "GATEWAY_PID=$GATEWAY_PID" >> .service-pids
echo "AUTH_PID=$AUTH_PID" >> .service-pids
echo "RESTAURANT_PID=$RESTAURANT_PID" >> .service-pids
echo "ORDER_PID=$ORDER_PID" >> .service-pids
echo "NOTIFICATION_PID=$NOTIFICATION_PID" >> .service-pids

# Display service URLs
echo ""
print_success "🎉 ByteBites Platform is now running!"
echo ""
echo "📋 Service URLs:"
echo "  • Eureka Dashboard:     http://localhost:8761"
echo "  • API Gateway:          http://localhost:8080"
echo "  • Auth Service:         http://localhost:8081"
echo "  • Restaurant Service:   http://localhost:8082"
echo "  • Order Service:        http://localhost:8083"
echo "  • Notification Service: http://localhost:8084"
echo ""
echo "📚 API Documentation:"
echo "  • Auth Service:         http://localhost:8081/swagger-ui.html"
echo "  • Restaurant Service:   http://localhost:8082/swagger-ui.html"
echo "  • Order Service:        http://localhost:8083/swagger-ui.html"
echo ""
echo "🔧 Management:"
echo "  • RabbitMQ Management:  http://localhost:15672 (admin/admin123)"
echo "  • Health Checks:        http://localhost:8080/actuator/health"
echo ""
echo "📖 Testing Guide:         See TESTING_GUIDE.md for detailed testing instructions"
echo "🏗️  Architecture:         See ARCHITECTURE.md for system architecture"
echo ""
print_warning "To stop all services, run: ./stop-services.sh"
echo ""
print_status "Services are starting up. Please wait a few minutes for all services to be fully ready." 
 