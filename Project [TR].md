# Proje Mimari ve Tasarım Dokümanı (MVP)

## 1. Genel Mimari Yaklaşım
* **Mimari Tip:** Mikroservis (Microservices)
* **İletişim:** Senkron (REST - Gateway üzerinden) ve Asenkron (RabbitMQ - Servisler arası)
* **Tasarım Deseni:** Katmanlı Mimari (Controller -> Service -> Repository)
* **Dağıtık Transaction:** Saga Orchestration (Custom State Machine tabanlı)

## 2. Teknoloji Yığını
* **Dil/Framework:** Java & Spring Boot
* **Veritabanı:** PostgreSQL (Her mikroservis için bağımsız veritabanı/şema)
* **Cache/Transient Data:** Redis (Sepet yönetimi için)
* **Message Broker:** RabbitMQ
* **Deployment/Containerization:** Docker & Jib
* **Altyapı:**
    * **API Gateway:** Spring Cloud Gateway
    * **Service Discovery:** Netflix Eureka
    * **Config Management:** Spring Cloud Config Server

## 3. Mikroservis Tanımları
* **Auth Service:** Kullanıcı kayıt/giriş, JWT üretimi. PostgreSQL kullanır.
* **Catalog Service:** Ürün listeleme ve yönetimi, Sayfalama (Pagination). PostgreSQL kullanır.
* **Basket Service:** Sepete ekle/çıkar/güncelle. Redis (AOF/RDB açık) kullanır.
* **Order Service (Orchestrator):** Sipariş oluşturma ve akış yönetimi. Custom State Machine burada çalışır. PostgreSQL kullanır.
* **Payment Service:** Iyzico entegrasyonu. Ödeme kayıtları PostgreSQL'de tutulur.

## 4. Kritik Teknik Kararlar

### 4.1. Güvenlik Akışı
* **Doğrulama:** JWT doğrulaması sadece API Gateway seviyesinde yapılır.
* **Propagasyon:** Gateway, doğrulanmış kullanıcının ID'sini `X-User-Id` header'ı ile alt servislere iletir. Alt servisler JWT ile uğraşmaz, sadece bu header'a güvenir.

### 4.2. Sipariş Akışı ve State Machine
* **Akış Yönetimi:** Order servisi içindeki bir `status` kolonu (Enum) üzerinden manuel yönetilir.
* **Adımlar:** `PENDING` -> `PAYMENT_WAITING` -> `COMPLETED` / `FAILED`.
* **Hata Yönetimi:** Ödeme başarısız olursa RabbitMQ üzerinden gelen mesaja göre sipariş `CANCELLED` durumuna çekilir. State logic, servis içinde izole bir `StateManager` bileşeni ile yönetilmelidir.

### 4.3. Veritabanı ve Performans
* **Audit Kolonları:** Sistem başlatılırken (initialization) oluşabilecek döngüsel bağımlılıkları (bootstrap problem) çözmek amacıyla `CreatedBy` ve `UpdatedBy` kolonları **nullable** olarak tasarlanmıştır.
* **Redis Kalıcılığı:** Veri kaybını önlemek için Redis'te AOF (Append Only File) özelliği aktif edilecektir.

### 4.4. İzlenebilirlik (Loglama)
* **Merkezi Loglama Yok:** Loglar servis bazlı lokal dosyalarda tutulacaktır.
* **Correlation ID:** İsteklerin servisler arası takibi için Gateway'de üretilen bir `Trace-Id` tüm HTTP ve RabbitMQ mesajlarına eklenecektir.

### 4.5. Containerization ve Lokal Geliştirme (Deployment)
* **Java Servisleri:** Dockerfile yazılmayacak. İmaj oluşturma işlemi Jib plugini üzerinden (`mvn jib:dockerBuild`) yapılarak doğrudan yerel Docker Daemon'a aktarılacaktır.
* **Altyapı ve Orkestrasyon:** Geliştirme aşamasında altyapı bileşenleri (PostgreSQL, Redis, RabbitMQ) ve yerel test ortamı için Java servislerini bir araya getiren kapsamlı bir `docker-compose.yml` dosyası kullanılacaktır.

## 5. Uygulama Detayları
* **Dokümantasyon:** Her servis için Swagger/OpenAPI.
* **Test:** JUnit ve Mockito ile Unit Test; Testcontainers ile Integration Test.
* **Hata Toleransı:** RabbitMQ mesajları için "Idempotent" tüketici tasarımı zorunludur ve mesajlara `Retry Limit` eklenecektir.