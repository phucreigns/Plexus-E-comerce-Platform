# Luồng Chạy Code API Gateway

## 📋 Tổng Quan

API Gateway sử dụng Spring Cloud Gateway (WebFlux) để route requests đến các microservices. Luồng xử lý theo reactive programming pattern.

## 🔄 Luồng Xử Lý Request

### 1. **Khởi Động Application**

```
ApiGatewayApplication.main()
  ↓
SpringApplication.run()
  ↓
Load application.yml
  ↓
Initialize Spring Cloud Gateway
  ↓
Load Routes Configuration (10 routes)
  ↓
Initialize Security Config
  ↓
Initialize CORS Config
  ↓
Start Netty Server on port 8080
```

**File:** `ApiGatewayApplication.java`

---

### 2. **Request Đến Gateway**

```
Client Request: GET http://localhost:8080/api/auth/login
  ↓
Netty Server nhận request
  ↓
Spring WebFlux Handler
```

---

### 3. **Security Filter Chain** (Nếu có AUTH0_DOMAIN)

```
SecurityWebFilterChain (SecurityConfig.java)
  ↓
├─ CSRF Filter (disabled)
  ↓
├─ CORS Filter (CorsConfig.java)
  │   └─ Kiểm tra Origin, Methods, Headers
  │   └─ Set CORS headers nếu hợp lệ
  ↓
├─ Authorization Filter
  │   └─ Kiểm tra path:
  │       ├─ /api/auth/** → permitAll()
  │       ├─ /actuator/** → permitAll()
  │       └─ Other paths → authenticated() (cần JWT)
  ↓
└─ JWT Authentication (nếu có AUTH0_DOMAIN)
    └─ Validate JWT token từ Authorization header
```

**File:** `SecurityConfig.java`

**Logic:**
```java
// Line 24: Kiểm tra AUTH0_DOMAIN
boolean hasAuth0 = issuerUri != null && !issuerUri.isEmpty() && !issuerUri.equals("https://");

if (hasAuth0) {
    // Enable OAuth2 với JWT decoder
} else {
    // Disable security - permitAll()
}
```

---

### 4. **Global Filters**

```
JwtAuthenticationFilter (Order: -100)
  ↓
├─ Đọc Authorization header từ request
├─ Log debug nếu có token
└─ Forward header xuống downstream services
```

**File:** `JwtAuthenticationFilter.java`

**Code:**
```java
// Line 17-26: Filter logic
String authHeader = request.getHeaders().getFirst("Authorization");
if (authHeader != null) {
    log.debug("Forwarding Authorization header to downstream service");
}
return chain.filter(exchange); // Continue filter chain
```

---

### 5. **Route Matching**

```
Gateway Route Locator
  ↓
Kiểm tra predicates cho từng route:
  ├─ auth-service: Path=/api/auth/**
  ├─ product-service: Path=/api/product/**
  ├─ shop-service: Path=/api/shop/**
  └─ ... (10 routes)
  ↓
Match route: auth-service
  ↓
URI: http://localhost:8090
```

**File:** `application.yml` (lines 9-88)

**Ví dụ cho auth-service:**
```yaml
- id: auth-service
  uri: http://localhost:8090
  predicates:
    - Path=/api/auth/**
  filters:
    - StripPrefix=1  # Bỏ "/api" prefix
```

---

### 6. **Gateway Filters**

```
Filter Chain (theo thứ tự Order):
  ↓
├─ RemoveCachedBodyFilter (Order: -2147483648)
├─ AdaptCachedBodyGlobalFilter (Order: -2147482648)
├─ JwtAuthenticationFilter (Order: -100) ✅ Custom filter
├─ NettyWriteResponseFilter (Order: -1)
├─ ForwardPathFilter (Order: 0)
├─ GatewayMetricsFilter (Order: 0)
├─ StripPrefix Filter (Order: 1) ✅ Bỏ "/api" prefix
│   └─ /api/auth/login → /auth/login
├─ RouteToRequestUrlFilter (Order: 10000)
├─ NoLoadBalancerClientFilter (Order: 10150)
├─ WebsocketRoutingFilter (Order: 2147483646)
├─ NettyRoutingFilter (Order: 2147483647) ✅ Forward request
└─ ForwardRoutingFilter (Order: 2147483647)
```

**StripPrefix Filter:**
- Input: `/api/auth/login`
- Output: `/auth/login`
- Forward đến: `http://localhost:8090/auth/login`

---

### 7. **Forward Request đến Downstream Service**

```
NettyRoutingFilter
  ↓
Tạo HTTP request mới:
  ├─ Method: GET
  ├─ URL: http://localhost:8090/auth/login
  ├─ Headers: Copy từ original request (bao gồm Authorization)
  └─ Body: Copy từ original request
  ↓
Netty HTTP Client
  ↓
Gửi request đến Auth Service (port 8090)
```

---

### 8. **Nhận Response từ Downstream Service**

```
Auth Service xử lý request
  ↓
Trả response về Gateway
  ↓
NettyRoutingFilter nhận response
  ↓
NettyWriteResponseFilter
  ↓
CORS Filter thêm CORS headers
  ↓
Trả response về Client
```

---

## 📊 Sơ Đồ Luồng Hoàn Chỉnh

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │ GET /api/auth/login
       ▼
┌─────────────────────────────────────┐
│      API Gateway (Port 8080)        │
│                                      │
│  1. Netty Server nhận request       │
│                                      │
│  2. Security Filter Chain           │
│     ├─ CORS Filter                  │
│     ├─ Authorization Check          │
│     └─ JWT Validation (nếu có)      │
│                                      │
│  3. Global Filters                   │
│     └─ JwtAuthenticationFilter       │
│                                      │
│  4. Route Matching                  │
│     └─ Match: auth-service          │
│                                      │
│  5. Gateway Filters                 │
│     └─ StripPrefix: /api → /        │
│                                      │
│  6. Forward Request                 │
│     └─ http://localhost:8090       │
│        /auth/login                  │
└──────────────┬───────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    Auth Service (Port 8090)         │
│                                      │
│  1. Nhận request: /auth/login       │
│  2. Xử lý business logic            │
│  3. Trả response                    │
└──────────────┬───────────────────────┘
               │
               │ Response
               ▼
┌─────────────────────────────────────┐
│      API Gateway                    │
│                                      │
│  1. Nhận response từ Auth Service   │
│  2. Thêm CORS headers               │
│  3. Forward response về Client      │
└──────────────┬───────────────────────┘
               │
               ▼
┌─────────────┐
│   Client    │
│  (Browser)  │
└─────────────┘
```

---

## 🔍 Chi Tiết Các Component

### **1. ApiGatewayApplication.java**
- Entry point của application
- Khởi tạo Spring Boot với Spring Cloud Gateway

### **2. SecurityConfig.java**
- Cấu hình Spring Security cho WebFlux
- Kiểm tra AUTH0_DOMAIN để enable/disable security
- Public endpoints: `/api/auth/**`, `/actuator/**`
- Protected endpoints: yêu cầu JWT token

### **3. CorsConfig.java**
- Cấu hình CORS cho tất cả requests
- Sử dụng `allowedOriginPatterns` thay vì `allowedOrigins` (vì có `allowCredentials: true`)

### **4. JwtAuthenticationFilter.java**
- Global filter với Order = -100
- Forward Authorization header xuống downstream services
- Chạy trước các filters khác

### **5. application.yml**
- Cấu hình routes cho 10 services
- Mỗi route có:
  - `id`: Tên route
  - `uri`: Địa chỉ service
  - `predicates`: Điều kiện match (Path pattern)
  - `filters`: StripPrefix để bỏ `/api` prefix

---

## 🎯 Ví Dụ Cụ Thể

### **Request:** `GET http://localhost:8080/api/product/products`

**Luồng xử lý:**

1. **Gateway nhận request**
   - Path: `/api/product/products`
   - Method: GET

2. **Security Check**
   - Path không match `/api/auth/**` → Cần authentication (nếu có AUTH0_DOMAIN)
   - Kiểm tra JWT token trong Authorization header

3. **Route Matching**
   - Match route: `product-service`
   - URI: `http://localhost:8091`

4. **StripPrefix Filter**
   - Input: `/api/product/products`
   - Output: `/product/products`

5. **Forward Request**
   - URL: `http://localhost:8091/product/products`
   - Headers: Copy từ original request

6. **Product Service xử lý**
   - Nhận request tại `/product/products`
   - Xử lý business logic
   - Trả response

7. **Gateway nhận response**
   - Thêm CORS headers
   - Trả về client

---

## ⚙️ Cấu Hình Quan Trọng

### **StripPrefix Filter**
```yaml
filters:
  - StripPrefix=1  # Bỏ 1 segment đầu tiên
```

**Ví dụ:**
- `/api/auth/login` → `/auth/login`
- `/api/product/123` → `/product/123`

### **Retry Configuration**
```yaml
default-filters:
  - name: Retry
    args:
      retries: 3
      statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
```

Gateway sẽ tự động retry 3 lần nếu service trả về BAD_GATEWAY hoặc GATEWAY_TIMEOUT.

---

## 🚀 Performance

- **Reactive Programming**: Sử dụng WebFlux (non-blocking)
- **Connection Pooling**: Netty tự động quản lý connection pool
- **Retry Logic**: Tự động retry khi service không available
- **Caching**: Gateway có thể cache routes configuration

---

## 📝 Notes

1. **Order của Filters**: Quan trọng! Filters chạy theo thứ tự Order (từ nhỏ đến lớn)

2. **StripPrefix**: Luôn bỏ `/api` prefix trước khi forward

3. **Security**: Nếu không có AUTH0_DOMAIN, tất cả requests đều `permitAll()`

4. **CORS**: Được xử lý ở cả Gateway và có thể ở downstream services

5. **Error Handling**: Gateway có default error handling, nhưng có thể customize

