.PHONY: help build up down logs clean test health

help:
	@echo "Mikroservis Sistemi - Komutlar"
	@echo "================================"
	@echo "make build       - Tüm servisleri build et"
	@echo "make up          - Tüm servisleri başlat"
	@echo "make down        - Tüm servisleri durdur"
	@echo "make logs        - Logları göster"
	@echo "make clean       - Tüm container ve volume'leri temizle"
	@echo "make test        - Yük testini çalıştır"
	@echo "make health      - Health check'leri kontrol et"
	@echo "make restart     - Tüm servisleri yeniden başlat"

build:
	@echo "Building all services..."
	docker-compose build

up:
	@echo "Starting all services..."
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	@sleep 10
	@make health

down:
	@echo "Stopping all services..."
	docker-compose down

logs:
	docker-compose logs -f

clean:
	@echo "Cleaning up all containers and volumes..."
	docker-compose down -v
	docker system prune -f

test:
	@echo "Running load test..."
	python3 load-test.py --duration 60 --workers 50

health:
	@echo "\nChecking service health..."
	@echo "========================================="
	@echo -n "API Gateway: "
	@curl -s http://localhost:8080/fallback/health || echo "NOT READY"
	@echo -n "User Service: "
	@curl -s http://localhost:8080/api/users/health || echo "NOT READY"
	@echo -n "Product Service: "
	@curl -s http://localhost:8080/api/products/health || echo "NOT READY"
	@echo -n "Order Service: "
	@curl -s http://localhost:8080/api/orders/health || echo "NOT READY"
	@echo "\n========================================="

restart:
	@make down
	@make up

# Service specific commands
restart-user:
	docker-compose restart user-service

restart-product:
	docker-compose restart product-service

restart-order:
	docker-compose restart order-service

restart-gateway:
	docker-compose restart api-gateway

# Database commands
db-shell:
	docker-compose exec postgres psql -U admin -d maindb

# OpenSearch commands
opensearch-health:
	@echo "OpenSearch Health:"
	@curl -s http://localhost:9200/_cluster/health?pretty

opensearch-indices:
	@echo "OpenSearch Indices:"
	@curl -s http://localhost:9200/_cat/indices?v

# Sample data
sample-data:
	@echo "Creating sample users..."
	@curl -X POST http://localhost:8080/api/users \
		-H "Content-Type: application/json" \
		-d '{"name":"Ahmet Yılmaz","email":"ahmet@example.com","phoneNumber":"+905551234567"}'
	@echo "\nCreating sample products..."
	@curl -X POST http://localhost:8080/api/products \
		-H "Content-Type: application/json" \
		-d '{"name":"Laptop","description":"High performance laptop","price":25000.00,"stockQuantity":50,"category":"Electronics"}'
	@curl -X POST http://localhost:8080/api/products \
		-H "Content-Type: application/json" \
		-d '{"name":"Mouse","description":"Wireless mouse","price":150.00,"stockQuantity":100,"category":"Electronics"}'
	@echo "\nSample data created!"
