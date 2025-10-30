# 🚀 Hızlı Başlangıç Rehberi

## 5 Dakikada Sistemi Çalıştırın!

### 1. Sistemi Başlatın
```bash
cd microservices-system
docker-compose up -d
```

### 2. Servislerin Hazır Olmasını Bekleyin (2-3 dakika)
```bash
# Logları izleyin
docker-compose logs -f

# Veya Makefile ile health check
make health
```

### 3. İlk Test - User Oluşturun
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phoneNumber": "+905551234567"
  }'
```

### 4. Product Oluşturun
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Test laptop",
    "price": 25000.00,
    "stockQuantity": 50,
    "category": "Electronics"
  }'
```

### 5. Order Oluşturun (Service-to-Service İletişim!)
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

### 6. OpenSearch Dashboards'a Gidin
```
http://localhost:5601
```
- Sol menüden "Observability" → "Trace Analytics"
- Service Map'i görün
- Order oluştururken yapılan tüm service çağrılarını trace'leyin!

### 7. Yük Testi Çalıştırın
```bash
# Python ile
python load-test.py --duration 60 --workers 50

# Veya JMeter ile (JMeter kuruluysa)
jmeter -n -t load-test.jmx -l results.jtl
```

### 8. Postman Collection'ı Kullanın
- Postman'i açın
- Import → File
- `Microservices-API-Collection.postman_collection.json` dosyasını seçin
- Tüm endpoint'leri test edin!

## 📊 Erişim Adresleri

| Servis | URL | Açıklama |
|--------|-----|----------|
| API Gateway | http://localhost:8080 | Ana giriş noktası |
| User Service | http://localhost:8081 | Direkt erişim |
| Product Service | http://localhost:8083 | Direkt erişim |
| Order Service | http://localhost:8082 | Direkt erişim |
| OpenSearch Dashboards | http://localhost:5601 | Monitoring |
| OpenSearch | http://localhost:9200 | Database |
| PostgreSQL | localhost:5432 | Database |

## 🎯 Ne Öğrendik?

✅ Mikroservis mimarisi
✅ Docker Compose ile orkestrasyon
✅ Service-to-service iletişim
✅ OpenTelemetry ile distributed tracing
✅ API Gateway pattern
✅ Database per service
✅ Health checks
✅ Yük testi

## 🛑 Sistemi Durdurun
```bash
docker-compose down

# Veya her şeyi temizleyin (dikkat: veriler silinir!)
docker-compose down -v
```

## 💡 İpuçları

1. **Logları İzleyin**: `docker-compose logs -f service-name`
2. **Health Check**: `make health` ile tüm servisleri kontrol edin
3. **Sample Data**: `make sample-data` ile örnek veriler oluşturun
4. **OpenSearch**: Trace analytics için http://localhost:5601

## 🐛 Sorun mu var?

1. Port çakışması mı? → docker-compose.yml'de portları değiştirin
2. Servis başlamıyor mu? → `docker-compose logs service-name`
3. Memory hatası mı? → Docker'a daha fazla RAM ayırın (8GB+ önerilir)

## 📚 Daha Fazlası

Detaylı bilgi için `README.md` dosyasına bakın!

---

**İyi Testler! 🚀**
