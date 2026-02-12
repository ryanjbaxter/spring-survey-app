#!/bin/bash

# Spring Cloud Poll Demo - Startup Script
# Starts all services in separate terminal tabs/windows

echo "🚀 Starting Spring Cloud Poll Demo..."
echo ""

# Check if RabbitMQ is running
if ! docker ps | grep -q rabbitmq; then
    echo "Starting RabbitMQ..."
    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
    echo "⏳ Waiting for RabbitMQ to start..."
    sleep 10
fi

echo "✅ RabbitMQ is running"
echo ""

# Function to start a service in a new terminal (macOS)
start_service_mac() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local extra_args=$4
    
    osascript -e "tell application \"Terminal\" to do script \"cd $(pwd)/$service_dir && echo '🟢 Starting $service_name on port $port' && $extra_args mvn spring-boot:run\""
}

# Function to start a service in a new terminal (Linux with gnome-terminal)
start_service_linux() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local extra_args=$4
    
    gnome-terminal --tab --title="$service_name" -- bash -c "cd $(pwd)/$service_dir && echo '🟢 Starting $service_name on port $port' && $extra_args mvn spring-boot:run; exec bash"
}

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    START_FN=start_service_mac
elif command -v gnome-terminal &> /dev/null; then
    START_FN=start_service_linux
else
    echo "⚠️  Auto-start not supported on this system."
    echo "Please start services manually in this order:"
    echo "1. eureka-server (port 8761)"
    echo "2. config-server (port 8888)"
    echo "3. gateway (port 8080)"
    echo "4. poll-service (port 8081)"
    echo "5. poll-service (port 8082)"
    echo "6. results-service (port 8083)"
    echo "7. poll-ui (port 8090)"
    exit 1
fi

echo "Starting services..."
echo ""

# Start services in order
$START_FN "Eureka Server" "eureka-server" "8761" ""
sleep 5

$START_FN "Config Server" "config-server" "8888" ""
sleep 5

$START_FN "Gateway" "gateway" "8080" ""
sleep 5

$START_FN "Poll Service 1" "poll-service" "8081" ""
sleep 3

$START_FN "Poll Service 2" "poll-service" "8082" "SERVER_PORT=8082 "
sleep 3

$START_FN "Results Service" "results-service" "8083" ""
sleep 3

$START_FN "Poll UI" "poll-ui" "8090" ""

echo ""
echo "✅ All services starting..."
echo ""
echo "📊 Service URLs:"
echo "   Eureka Dashboard: http://localhost:8761"
echo "   Poll UI:          http://localhost:8090"
echo "   RabbitMQ Mgmt:    http://localhost:15672 (guest/guest)"
echo ""
echo "⏳ Wait ~30 seconds for all services to fully start"
echo ""
