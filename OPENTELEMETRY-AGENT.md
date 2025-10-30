# 🔍 OpenTelemetry Java Agent Kullanım Rehberi

## ✅ Güncelleme Yapıldı!

Tüm Dockerfile'lara **OpenTelemetry Java Agent** eklendi. Artık otomatik instrumentation aktif!

## 🎯 Ne Değişti?

### Önceki Yöntem (Spring Boot Starter)
- Maven dependency olarak ekleme
- Manuel konfigürasyon gerekiyor
- Kod değişikliği gerekiyor

### Yeni Yöntem (Java Agent) ✨
- Runtime'da otomatik instrumentation
- **Sıfır kod değişikliği**
- Daha az konfigürasyon
- Otomatik HTTP, JDBC, ve daha fazla instrumentation

## 🚀 Nasıl Çalışır?

Dockerfile'lar güncellendi ve şu özelliklere sahip:

```dockerfile
# OpenTelemetry Java Agent indirilir
ADD https://github.com/open-telemetry/.../opentelemetry-javaagent.jar /app/

# Uygulama Java Agent ile başlatılır
ENTRYPOINT ["java", \
    "-javaagent:/app/opentelemetry-javaagent.jar", \
    "-Dotel.service.name=user-service", \
    "-Dotel.traces.exporter=otlp", \
    "-Dotel.metrics.exporter=otlp", \
    "-Dotel.exporter.otlp.endpoint=http://data-prepper:21890", \
    "-jar", "app.jar"]
```

## 📦 Yeniden Build Etme

```bash
# Tüm servisleri yeniden build et
docker-compose build

# Veya sadece değişen servisleri
docker-compose build user-service
docker-compose build product-service
docker-compose build order-service
docker-compose build api-gateway

# Başlat
docker-compose up -d

# Logları izle
docker-compose logs -f
```

## 🔬 Java Agent Ne Yapar?

Java Agent otomatik olarak şunları enstrümante eder:

### ✅ HTTP Server/Client
- Spring MVC controllers
- RestTemplate
- WebClient
- HTTP istekleri otomatik trace'lenir

### ✅ Database
- JDBC queries
- JPA/Hibernate
- PostgreSQL bağlantıları
- Query execution time

### ✅ Service-to-Service
- RestTemplate calls (Order → User)
- RestTemplate calls (Order → Product)
- Distributed tracing context propagation

### ✅ Async Operations
- @Async methods
- CompletableFuture
- Thread pool executions

## 📊 OpenSearch'te Görüntüleme

1. **OpenSearch Dashboards'a git**: http://localhost:5601

2. **Trace Analytics'e tıkla**:
   - Observability → Trace Analytics

3. **Service Map'i gör**:
   - API Gateway → User Service
   - API Gateway → Product Service
   - API Gateway → Order Service
   - Order Service → User Service
   - Order Service → Product Service

4. **Traces'leri filtrele**:
   - Service name'e göre
   - Latency'ye göre
   - Error'lara göre

## 🎯 Test Senaryosu

### 1. Basit Trace
```bash
# User oluştur (tek servis)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@example.com","phoneNumber":"+905551234567"}'
```

**Trace göreceksin:**
- API Gateway → User Service
- User Service → PostgreSQL

### 2. Distributed Trace (Service-to-Service)
```bash
# Order oluştur (3 servis arası iletişim)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [{"productId": 1, "quantity": 2}]
  }'
```

**Trace göreceksin:**
- API Gateway → Order Service
- Order Service → User Service (kullanıcı doğrulama)
- Order Service → Product Service (ürün bilgisi)
- Order Service → Product Service (stok güncelleme)
- Order Service → PostgreSQL (sipariş kaydetme)

## ⚡ Performance

Java Agent çok hafiftir:
- **Latency artışı**: ~1-2ms
- **Memory overhead**: ~50MB
- **CPU overhead**: Minimal

## 🔧 İleri Seviye Konfigürasyon

### Environment Variables ile Özelleştirme

docker-compose.yml'de environment ekleyebilirsin:

```yaml
user-service:
  environment:
    - OTEL_TRACES_SAMPLER=always_on
    - OTEL_TRACES_SAMPLER_ARG=1.0
    - OTEL_INSTRUMENTATION_JDBC_ENABLED=true
    - OTEL_INSTRUMENTATION_SPRING_WEBMVC_ENABLED=true
```

### Sampling (Örnekleme)

```yaml
environment:
  # %10 trace'leri kaydet
  - OTEL_TRACES_SAMPLER=parentbased_traceidratio
  - OTEL_TRACES_SAMPLER_ARG=0.1
```

### Debug Mode

```yaml
environment:
  - OTEL_LOGS_EXPORTER=logging
  - OTEL_LOG_LEVEL=DEBUG
```

## 🆚 Spring Boot Starter vs Java Agent

| Özellik | Spring Boot Starter | Java Agent |
|---------|---------------------|------------|
| **Kurulum** | Maven dependency | Dockerfile'da ADD |
| **Kod değişikliği** | Gerekli | **Gerekmez** ✅ |
| **Auto-instrumentation** | Manuel | **Otomatik** ✅ |
| **HTTP tracking** | Manuel config | **Otomatik** ✅ |
| **JDBC tracking** | Manuel config | **Otomatik** ✅ |
| **Build time** | +dependencies | Aynı |
| **Runtime overhead** | Düşük | Düşük |
| **Özelleştirme** | Java kodu ile | Environment vars ile |

## 🐛 Troubleshooting

### "Agent cannot be loaded"
```bash
# Java sürümünü kontrol et
java -version  # 17+ olmalı

# Agent dosyasını kontrol et
docker-compose exec user-service ls -lh /app/opentelemetry-javaagent.jar
```

### "No traces in OpenSearch"
```bash
# Data Prepper'ın çalıştığını kontrol et
curl http://localhost:21890/health

# Servis loglarını kontrol et
docker-compose logs user-service | grep -i otel
```

### "High memory usage"
```yaml
# JVM memory limitlerini ayarla
ENTRYPOINT ["java", \
    "-Xmx512m", \
    "-Xms256m", \
    "-javaagent:/app/opentelemetry-javaagent.jar", \
    ...]
```

## 📚 Daha Fazla Bilgi

- **Java Agent Docs**: https://opentelemetry.io/docs/instrumentation/java/automatic/
- **Configuration**: https://opentelemetry.io/docs/instrumentation/java/automatic/agent-config/
- **Supported Libraries**: https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md

## ✅ Özet

1. ✅ Tüm Dockerfile'lar güncellendi
2. ✅ Java Agent otomatik indirilir
3. ✅ Sıfır kod değişikliği gerekiyor
4. ✅ Build et ve çalıştır!

```bash
docker-compose build
docker-compose up -d
```

**İşte bu kadar! 🎉**

OpenTelemetry Java Agent şimdi otomatik olarak:
- HTTP isteklerini trace'liyor
- Database query'leri izliyor
- Service-to-service çağrıları takip ediyor
- Distributed tracing yapıyor

OpenSearch Dashboards'da (http://localhost:5601) her şeyi görüntüleyebilirsin!
