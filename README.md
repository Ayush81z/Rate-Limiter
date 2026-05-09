# Distributed Rate Limiter

A distributed rate limiter built using Spring Boot and Redis.

The project limits how many requests a client can send to an API within a fixed time period. If the limit is exceeded, the server returns an HTTP 429 (Too Many Requests) response.

The application uses the Token Bucket algorithm and Redis for centralized token storage, allowing the rate limiter to work consistently even when multiple application instances are running.

---

# Features

* Distributed rate limiting using Redis
* Token Bucket algorithm implementation
* IP-based request limiting
* Dynamic rate limit updates using admin APIs
* Request metadata through HTTP headers
* Configurable request limits
* Servlet filter-based interception
* Spring Boot REST APIs

---

# Technologies Used

* Java
* Spring Boot
* Redis
* Docker
* Maven
* Servlet Filter API
* Java Collections Framework
* Postman

---

# Project Structure

```text
src/main/java/com/rateLimiter
│
├── controller
│   ├── AdminController
│   └── TestController
│
├── filter
│   └── RateLimitingFilter
│
├── model
│   └── RateLimitResponse
│
├── service
│   └── RateLimiterService
│
└── RateLimiterApplication
```

---

# How the Application Works

1. A client sends a request to an API endpoint.
2. The request first passes through the `RateLimitingFilter`.
3. The filter extracts:

   * Client IP address
   * Requested endpoint
4. A unique key is generated using:

```text
IP_ADDRESS:endpoint
```

5. The `RateLimiterService` checks Redis for:

   * Remaining tokens
   * Last refill timestamp
6. If tokens are available:

   * One token is consumed
   * Request is allowed
7. If no tokens remain:

   * Request is blocked
   * HTTP 429 response is returned

---

# Token Bucket Algorithm

The application uses the Token Bucket algorithm.
checkout : https://medium.com/@devenchan/implementing-rate-limiting-in-java-from-scratch-leaky-bucket-and-tokenn-bucket-implementation-63a944ba93aa

* Each request consumes one token.
* The bucket has a maximum capacity.
* Tokens are refilled after a fixed interval.
* If the bucket becomes empty, requests are rejected until refill occurs.

Example:

```text
Capacity = 5
Refill Rate = 5 per minute
```

Behavior:

* First 5 requests are allowed
* 6th request is blocked
* After one minute, tokens refill again

---

# Prerequisites

Before running the project, make sure the following are installed:

* Java 17+
* Maven
* Docker
* Redis
* IDE (IntelliJ IDEA recommended)

---

# Running Redis

The project uses Redis for distributed token storage.

Run Redis using Docker:

```bash
docker run --name redis-rate-limiter -p 6379:6379 -d redis:7-alpine
```

To verify Redis is running:

```bash
docker ps
```

---

# Clone the Project

```bash
git clone https://github.com/Ayush81z/Rate-Limiter
```

Open the project in IntelliJ IDEA or another Java IDE.

---

# Configure Application

File:

```text
src/main/resources/application.properties
```

Configuration:

```properties
spring.application.name=rateLimiter

spring.data.redis.host=localhost
spring.data.redis.port=6379

server.port=8080

app.rate-limit.capacity=5
app.rate-limit.refill-rate=5
```

---

# Running the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or run the `RateLimiterApplication` class directly from the IDE.

Once started, the application runs on:

```text
http://localhost:8080
```

---


Example:

```text
http://localhost:8080/api/test
```

---

Example using Postman:

* Method: POST
* URL:

```text
http://localhost:8080/admin/update-limits?capacity=10&refillRate=10
```

---

## View Current Limits

```http
GET /admin/current-limits
```

Example:

```text
http://localhost:8080/admin/current-limits
```

Response:

```json
{
  "defaultCapacity": 10,
  "defaultRefillRate": 10
}
```

---

# Response Headers

Protected APIs return rate limit metadata through HTTP headers.

Example:

```text
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
X-Client-IP: 127.0.0.1 (usually it is going to be your ipv4 or ipv6 address) not the real one as it is tested on localhost 
```
---

# Example Flow

1. Start Redis using docker 
2. Start Spring Boot application
3. Call:

```text
/api/hello
```

4. After the configured limit is exceeded:

```http
429 Too Many Requests
```

5. Update limits dynamically:

```text
POST /admin/update-limits?capacity=20&refillRate=20
```

6. Requests are now allowed according to the new configuration.

---

---

## Redis

Used for:

* Distributed token storage
* Shared request state across application instances

---

# Limitations

Current limitations of the project:

* IP-based limiting only
* Redis operations are not fully atomic
* No authentication-based rate limiting
* No frontend dashboard

---

The project is useful for understanding real-world backend system design and scalable API protection mechanisms.
