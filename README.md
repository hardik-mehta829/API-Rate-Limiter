# 🚀 Java Rate Limiter (Token Bucket using Upstash Redis)

A lightweight **Rate Limiter library in plain Java** using the **Token Bucket Algorithm**, powered by **Upstash Redis (REST API)**.

This library is framework-independent and can be used in **Spring Boot, Servlets, or any Java backend**.

---

## 📌 Features

* ⚡ Token Bucket algorithm implementation
* 🌐 Uses Upstash Redis via REST (no Redis client required)
* 🧠 Stateless backend logic
* 🔑 Flexible key-based limiting (userId, email, IP, etc.)
* 📦 Packaged as reusable Maven library
* 🚫 No Lua scripts (simple HTTP-based implementation)

---

## 🧠 How It Works

* Each user is assigned a **bucket of tokens**
* Tokens are consumed on each request
* Tokens are refilled after a fixed interval
* Data is stored in Redis:

  * `tokens` → remaining tokens
  * `issuedat` → last refill timestamp

---

## ⚙️ Configuration

The RateLimiter is initialized using:

```java
RateLimiter rl = new RateLimiter(capacity, refillRate, intervalInMinutes, ttl);
```

### Parameters:

| Parameter           | Description                     |
| ------------------- | ------------------------------- |
| `capacity`          | Maximum number of tokens        |
| `refillRate`        | Tokens added per interval       |
| `intervalInMinutes` | Time window for refill          |
| `ttl`               | Redis key expiry time (seconds) |

---

## 🧪 Usage Example

```java
RateLimiter rl = new RateLimiter(5, 5, 1, 300);

boolean allowed = rl.isAllowed("user@example.com");

if (allowed) {
    // Process request
} else {
    // Reject request (rate limited)
}
```

---

## 🔌 Redis Setup (Upstash)

This project uses **Upstash Redis REST API**.

You need:

* REST URL
* Authorization Token

Update these inside the class:

```java
private final String fixedUrl = "YOUR_UPSTASH_REDIS_URL";
private final String token = "YOUR_UPSTASH_TOKEN";
```

---

## 📡 API Calls Used

The library interacts with Redis using:

* `HGET` → fetch tokens & timestamp
* `HSET` → update tokens & timestamp
* `EXPIRE` → set TTL

---

## ⚠️ Limitations

* ❗ Not atomic (no Lua scripting)
* ⚠️ Possible race conditions under high concurrency
* 📉 Multiple HTTP calls per request (performance trade-off)

---

## 🚀 Future Improvements

* 🔒 Add Lua scripting for atomic operations
* ⚡ Reduce number of HTTP calls

---

## 🏗️ Maven Dependency (Local)

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>RateLimiter</artifactId>
    <version>1.0</version>
</dependency>
```

---

## 📁 Project Structure

```
RateLimiter/
 ├── src/main/java/org/example/RateLimiter.java
 ├── pom.xml
```

---

## 🧑‍💻 Author

Developed as part of backend engineering practice to understand:

* Rate limiting algorithms
* Redis-based distributed systems
* HTTP-based data storage

---

## 📜 License

This project is open-source and free to use.

---
