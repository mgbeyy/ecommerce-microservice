# Auth Service (Kimlik Doğrulama Servisi) - MVP

## Servisin Amacı
**Auth Service**, e-ticaret mikroservis ekosisteminin merkezi kimlik sağlayıcısıdır (Identity Provider). 
Temel sorumluluğu; sisteme yeni kullanıcıları kaydetmek, şifreleri güvenli bir şekilde (BCrypt) hashleyerek veritabanında saklamak ve başarılı giriş işlemlerinde (Login) istemciye standartlara uygun bir **JWT (JSON Web Token)** üretmektir.

**Ne Yapmaz?:** 
Bu servis **token doğrulama (validation)** işlemi yapmaz. Mikroservis mimarimizin tasarım kararı gereği, üretilen JWT'lerin doğrulanması, yetki kontrolü (Authorization) ve rotalama öncesi güvenlik duvarı işlemleri **API Gateway** üzerinde yapılmaktadır. Bu nedenle Auth Service'in kendi uç noktaları dışarıya açıktır (`permitAll`).

---

## Kullanılan Teknolojiler
* **Java 21 & Spring Boot 4.0.6**
* **Spring Security 6** (Sadece Password Encoder ve yapılandırma için)
* **JJWT (0.11.5)** (Token üretimi)
* **Spring Data JPA & Hibernate**
* **PostgreSQL**
* **Spring Cloud Netflix Eureka Client** (Servis keşfi)
* **Spring Cloud Config Client** (Merkezi yapılandırma)

---

## Önemli Sınıflar ve Sorumlulukları

* **`SecurityConfig`**: Spring Security'nin varsayılan kısıtlamalarını ezer. İstemci ile Gateway arasındaki yetki kontrolünü Gateway yaptığı için, bu sınıfta tüm CSRF korumaları kapatılmış (`csrf.disable()`), oturum yönetimi `STATELESS` yapılmış ve tüm rotalara (`permitAll`) erişim izni verilmiştir. Ayrıca `BCryptPasswordEncoder` bean'ini barındırır.
* **`JwtService`**: Sadece token üretmekten sorumludur. Başarılı login/register sonrasında Gateway'in diğer servislerde kullanabilmesi için token içerisine `userId` ve `role` bilgilerini **Claim** olarak ekler. `secret` ve `expiration` değerlerini Config Server'dan okur.
* **`ISoftDeletable` & `User`**: Veritabanı standartlarını korumak için kayıtların fiziksel olarak silinmesini engeller. Hibernate `@SQLDelete` ve `@SQLRestriction` kullanılarak "Soft Delete" mekanizması Entity seviyesinde garanti altına alınmıştır. Ayrıca JPA Auditing ile `created_at` ve `updated_at` alanları otomatik yönetilir.
* **`AuthService`**: Ana iş mantığının bulunduğu katmandır. Mükerrer e-posta kontrolü, şifre hashleme, şifre doğrulama ve JWT servisini tetikleme işlemlerini koordine eder.

---

## API Referansı (Alıp Döndürdükleri)

Auth Service, istemcilerle sadece DTO'lar (Data Transfer Objects) aracılığıyla konuşur. Tüm istekler Gateway üzerinden (`lb://auth-service/api/v1/auth/**`) buraya yönlendirilir.

### 1. Kullanıcı Kaydı (Register)
Sisteme yeni bir kullanıcı ekler ve anında login olmuş gibi JWT döner.

* **URL:** `/api/v1/auth/register`
* **HTTP Metodu:** `POST`
* **Başarılı Yanıt Kodu:** `201 Created`

**Request Body (`RegisterRequest`):**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response Body (`AuthResponse`):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJVU0VSIiwic3ViIjoidXNlckBleGFtcGxl... (JWT Token)"
}
```

### 2. Kullanıcı Girişi (Login)
Kayıtlı kullanıcının kimliğini doğrular ve JWT döner.

* **URL:** `/api/v1/auth/login`
* **HTTP Metodu:** `POST`
* **Başarılı Yanıt Kodu:** `200 OK`

**Request Body (`LoginRequest`):**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response Body (`AuthResponse`):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJVU0VSIiwic3ViIjoidXNlckBleGFtcGxl... (JWT Token)"
}
```

---

## Gelecek Geliştirmeler (To-Do List)

Projenin MVP aşamasından çıkıp production ortamına hazırlanırken bu servise eklenmesi planlanan özellikler:

- [ ] **Refresh Token Mekanizması:** Access Token süresi dolduğunda kullanıcının tekrar şifre girmemesi için uzun ömürlü bir Refresh Token yapısının kurulması ve veritabanında/Redis'te yönetilmesi.
- [ ] **Global Exception Handler Eklenmesi:** `@RestControllerAdvice` oluşturularak `@Valid` validasyon hatalarının (örn: e-posta formatı yanlış, şifre çok kısa) ve özel iş mantığı hatalarının (`UserAlreadyExistsException`, `InvalidCredentialsException`) standart bir ErrorResponse DTO'su ile (Gateway'deki yapıya uygun olarak) dışarı dönülmesi.
- [ ] **Validasyon Kurallarının Yazılması:** `RegisterRequest` ve `LoginRequest` DTO'larına `jakarta.validation` (`@Email`, `@NotBlank`, `@Size`) kurallarının eklenmesi.
- [ ] **Şifre Sıfırlama (Password Reset) Akışı:** Kullanıcının şifresini unuttuğu senaryolar için tek kullanımlık token üretimi ve e-posta servisi (Notification Service) ile entegrasyon.
- [ ] **Rate Limiting:** Brute-force saldırılarını engellemek için `/login` uç noktasına Gateway veya Auth seviyesinde istek sınırlandırması getirilmesi.
```