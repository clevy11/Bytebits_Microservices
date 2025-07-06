#!/bin/bash

# ByteBites Platform - Service Shutdown Script
# This script stops all services gracefully

echo "🛑 Stopping ByteBites Platform..."

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

# Check if .service-pids file exists
if [ ! -f .service-pids ]; then
    print_warning "No service PIDs file found. Services may not be running."
    exit 0
fi

# Load PIDs from file
source .service-pids

# Function to kill process if it exists
kill_process() {
    local pid=$1
    local service_name=$2
    
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
        print_status "Stopping $service_name (PID: $pid)..."
        kill "$pid"
        sleep 2
        
        # Force kill if still running
        if kill -0 "$pid" 2>/dev/null; then
            print_warning "Force killing $service_name..."
            kill -9 "$pid"
        fi
        
        print_success "$service_name stopped"
    else
        print_warning "$service_name is not running"
    fi
}

# Stop services in reverse order
print_status "Stopping business services..."

if [ -n "$NOTIFICATION_PID" ]; then
    kill_process "$NOTIFICATION_PID" "Notification Service"
fi

if [ -n "$ORDER_PID" ]; then
    kill_process "$ORDER_PID" "Order Service"
fi

if [ -n "$RESTAURANT_PID" ]; then
    kill_process "$RESTAURANT_PID" "Restaurant Service"
fi

if [ -n "$AUTH_PID" ]; then
    kill_process "$AUTH_PID" "Auth Service"
fi

print_status "Stopping core services..."

if [ -n "$GATEWAY_PID" ]; then
    kill_process "$GATEWAY_PID" "API Gateway"
fi

if [ -n "$CONFIG_PID" ]; then
    kill_process "$CONFIG_PID" "Config Server"
fi

if [ -n "$DISCOVERY_PID" ]; then
    kill_process "$DISCOVERY_PID" "Discovery Server"
fi

# Stop infrastructure services
print_status "Stopping infrastructure services..."
docker-compose down

if [ $? -eq 0 ]; then
    print_success "Infrastructure services stopped successfully!"
else
    print_warning "Some infrastructure services may still be running"
fi

# Clean up
rm -f .service-pids

print_success "🎉 All ByteBites Platform services have been stopped!"

echo ""
echo "📋 Cleanup completed:"
echo "  • All Java services stopped"
echo "  • Docker containers stopped"
echo "  • PID file removed"
echo ""
print_status "To start services again, run: ./start-services.sh" 