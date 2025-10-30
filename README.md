# Mikroservis Sistemi - Spring Boot + Docker + OpenTelemetry

Bu proje, Docker üzerinde çalışan, OpenTelemetry entegrasyonlu, service-to-service iletişim kurabilen ve yük testine hazır bir mikroservis sistemidir.

## 🏗️ Mimari

```
┌─────────────────┐
│   API Gateway   │ :8080
│  (Port: 8080)   │
└────────┬────────┘
         │
    ┌────┴────┬────────────┬──────────────┐
    │         │            │              │
┌───▼────┐ ┌──▼────┐ ┌────▼─────┐ ┌──────▼──────┐
│  User  │ │Product│ │  Order   │ │ Data Prepper│
│Service │ │Service│ │ Service  │ │   (OTEL)    │
│  :8081 │ │ :8083 │ │  :8082   │ │  :21890     │
└───┬────┘ └───┬───┘ └────┬─────┘ └──────┬──────┘
    │          │           │              │
    └──────────┴───────────┴──────────────┤
                                          │
    ┌─────────────────────────────────────┴────┐
    │                                           │
┌───▼──────────┐  ┌──────────────┐  ┌─────────▼─────┐
│ PostgreSQL   │  │  OpenSearch  │  │  OpenSearch   │
│   :5432      │  │    :9200     │  │  Dashboards   │
│ (3 database) │  │              │  │    :5601      │
└──────────────┘  └──────────────┘  └───────────────┘
```

## 📋 Servisler

### 1. API Gateway (Port 8080)
- Spring Cloud Gateway kullanır
- Tüm mikroservislere routing yapar
- Circuit breaker desteği
- CORS yapılandırması

### 2. User Service (Port 8081)
- Kullanıcı yönetimi (CRUD)
- PostgreSQL (userdb)
- OpenTelemetry entegrasyonu

### 3. Product Service (Port 8083)
- Ürün yönetimi (CRUD)
- Stok kontrolü
- PostgreSQL (productdb)
- OpenTelemetry entegrasyonu

### 4. Order Service (Port 8082)
- Sipariş yönetimi
- **Service-to-service iletişim:**
  - User Service ile kullanıcı doğrulama
  - Product Service ile ürün bilgisi alma ve stok güncelleme
- PostgreSQL (orderdb)
- OpenTelemetry entegrasyonu

### 5. Data Prepper (Port 21890, 4900)
- OpenTelemetry traces collector
- OpenTelemetry metrics collector
- OpenSearch'e veri gönderimi

### 6. OpenSearch (Port 9200)
- Metrics ve traces saklama
- Index templates ile yapılandırılmış

### 7. OpenSearch Dashboards (Port 5601)
- Görselleştirme arayüzü
- Trace analytics
- Service map

## 🚀 Kurulum ve Çalıştırma

### Ön Gereksinimler
- Docker ve Docker Compose
- 8GB+ RAM önerilir
- Java 17+ (local development için)
- Maven 3.9+ (local development için)

### Sistemi Başlatma

```bash
# 1. Projeyi klonlayın
cd microservices-system

# 2. Docker Compose ile tüm servisleri başlatın
docker-compose up -d

# 3. Logları takip edin
docker-compose logs -f

# 4. Servislerin durumunu kontrol edin
docker-compose ps
```

### Servisler Hazır mı?

Tüm servislerin health check'lerini bekleyin (yaklaşık 2-3 dakika):

```bash
# API Gateway
curl http://localhost:8080/fallback/health

# User Service
curl http://localhost:8080/api/users/health

# Product Service
curl http://localhost:8080/api/products/health

# Order Service
curl http://localhost:8080/api/orders/health
```

## 📊 OpenTelemetry ve Observability

### OpenSearch Dashboards'a Erişim

```
URL: http://localhost:5601
```

### Traces Görüntüleme

1. OpenSearch Dashboards'a gidin
2. Sol menüden "Observability" → "Trace Analytics" seçin
3. Service map ve trace'leri görüntüleyin

### Metrics Görüntüleme

Metrics şu indexlerde saklanır:
- `ss4o_metrics-otel-*`

Traces şu indexlerde saklanır:
- `otel-v1-apm-span-*`
- `otel-v1-apm-service-map`

## 🔧 API Kullanımı

### Postman Collection

`Microservices-API-Collection.postman_collection.json` dosyasını Postman'e import edin.

### Örnek API Çağrıları

#### User Oluşturma
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ahmet Yılmaz",
    "email": "ahmet@example.com",
    "phoneNumber": "+905551234567"
  }'
```

#### Product Oluşturma
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High performance laptop",
    "price": 25000.00,
    "stockQuantity": 50,
    "category": "Electronics"
  }'
```

#### Order Oluşturma (Service-to-Service İletişim)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

Bu çağrı sırasında:
1. Order Service, User Service'i çağırarak kullanıcıyı doğrular
2. Product Service'i çağırarak ürün bilgilerini alır
3. Product Service'i çağırarak stok günceller
4. Sipariş oluşturur

Tüm bu adımlar OpenTelemetry ile trace'lenir ve OpenSearch'te görüntülenebilir.

## ⚡ Yük Testleri

### JMeter ile Yük Testi

```bash
# JMeter kurulumunuz varsa
jmeter -n -t load-test.jmx -l results.jtl

# GUI modunda
jmeter -t load-test.jmx
```

JMeter testi:
- 50 concurrent user ile user oluşturma (10 loop)
- 50 concurrent user ile product oluşturma (10 loop)
- 30 concurrent user ile order oluşturma (5 loop)

### Python ile Yük Testi

```bash
# Gereksinimleri yükleyin
pip install requests

# Testi çalıştırın (60 saniye, 50 worker)
python load-test.py --duration 60 --workers 50

# Özel parametrelerle
python load-test.py --duration 120 --workers 100 --url http://localhost:8080
```

Python scripti:
- Mixed workload (CRUD operasyonları)
- Service-to-service testleri
- Detaylı istatistikler

## 📈 Performans Metrikleri

Test sonuçlarını OpenSearch Dashboards'tan görüntüleyin:
- Request latency
- Throughput (requests/second)
- Error rate
- Service dependencies
- Database query performance

## 🛠️ Geliştirme

### Local Development

```bash
# Her servis için ayrı ayrı
cd user-service
mvn spring-boot:run

cd product-service
mvn spring-boot:run

cd order-service
mvn spring-boot:run

cd api-gateway
mvn spring-boot:run
```

### Environment Variables

Docker Compose içinde tanımlı:
- `SPRING_DATASOURCE_URL`: PostgreSQL connection
- `USER_SERVICE_URL`: User service URL
- `PRODUCT_SERVICE_URL`: Product service URL
- `ORDER_SERVICE_URL`: Order service URL
- `OPENSEARCH_HOST`: OpenSearch host

## 🐛 Troubleshooting

### Servisler başlamıyor
```bash
# Logları kontrol edin
docker-compose logs service-name

# Örnek:
docker-compose logs user-service
```

### Database bağlantı hatası
```bash
# PostgreSQL'in hazır olduğundan emin olun
docker-compose logs postgres

# Manuel olarak restart edin
docker-compose restart user-service
```

### OpenSearch bağlantı hatası
```bash
# OpenSearch memory ayarları
# docker-compose.yml içinde OPENSEARCH_JAVA_OPTS değerini artırın
```

### Port çakışması
```bash
# Kullanılan portları kontrol edin
docker-compose ps

# Gerekirse docker-compose.yml'de portları değiştirin
```

## 📦 Teknolojiler

- **Backend**: Java 17, Spring Boot 3.2.0
- **API Gateway**: Spring Cloud Gateway
- **Database**: PostgreSQL 15
- **Observability**: OpenTelemetry, OpenSearch, Data Prepper
- **Containerization**: Docker, Docker Compose
- **Load Testing**: JMeter, Python
- **Monitoring**: OpenSearch Dashboards

## 🔑 Önemli Özellikler

✅ Mikroservis mimarisi
✅ Service-to-service iletişim
✅ OpenTelemetry distributed tracing
✅ OpenTelemetry metrics
✅ API Gateway pattern
✅ Database per service pattern
✅ Health checks
✅ Circuit breaker
✅ Docker containerization
✅ Yük testi hazır
✅ Postman collection

## 📝 Notlar

- Tüm servisler OpenTelemetry ile enstrümante edilmiştir
- Order Service, User ve Product servislerini çağırarak distributed trace oluşturur
- Data Prepper, OTLP formatında trace ve metrics kabul eder
- OpenSearch Dashboards'ta trace analytics ve service map görüntülenebilir
- Health check'ler Docker Compose tarafından yönetilir

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 📞 İletişim

Sorularınız için issue açabilirsiniz.

---

**Hazırlayan:** Mikroservis Sistemi Ekibi
**Versiyon:** 1.0.0
**Tarih:** 2025
#   m i c r o s e v i c e s - s y s t e m - o t e l  
 