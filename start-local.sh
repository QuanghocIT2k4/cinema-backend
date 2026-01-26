#!/bin/bash

echo "============================================"
echo "STARTING CINEMA BACKEND (LOCAL)"
echo "============================================"
echo ""

echo "[1/3] Starting MySQL Docker..."
docker-compose up -d
sleep 5

echo ""
echo "[2/3] Waiting for MySQL to be ready..."
sleep 10

echo ""
echo "[3/3] Starting Spring Boot Application..."
echo ""
./mvnw spring-boot:run

