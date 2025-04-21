# ✈️ Flight Booking System - Microservices Architecture (JAVA 21)

A full-fledged **Flight Booking System** built using **Spring Boot Microservices**, integrated with **Stripe for payments**, **email notifications** for check-in, and service communication via **Feign Clients**. This project is modular, scalable, and deployable with an API Gateway and Eureka Server.

---

## 🚀 Features

- 🔐 **User Authentication & Roles** (Passenger, Admin)
- 🛫 **Flight Management** (source, destination, time)
- 📖 **Flight Booking**
- 💳 **Stripe Payment Integration**
- 📩 **Email Confirmation on Check-in**
- 📦 **Microservices Architecture**
- 🔍 **Service Discovery with Eureka**(http://localhost:8761/)
- 🌐 **API Gateway Routing**
- 📑 **Swagger API Documentation** (http://localhost:8081/swagger-ui/index.html#/)

---

## 🧱 Microservices

| Service Name      | Port | Responsibilities |
|------------------|------|------------------|
| **User Service**   | `8081` | Manages users (registration, login, fetch by ID) |
| **Flight Service** | `8082` | Manages flight details |
| **Booking Service**| `8083` | Books flights for users |
| **Payment Service**| `8084` | Stripe payment gateway integration |
| **Check-In Service**| `8085` | Generates boarding pass, sends email |
| **Eureka Server**  | `8761` | Service registry |
| **API Gateway**    | `8080` | Unified routing layer |

---

## 🧩 Tech Stack

- Java 21, Spring Boot 3+
- Spring Cloud (Feign, Eureka, Config)
- Stripe API
- JavaMailSender
- Swagger for API docs
- Maven, Lombok

---

## 🗂️ Database Schema (Simplified)

### 🧑‍💼 User

| Field       | Type       | Constraints       |
|-------------|------------|-------------------|
| user_id     | Integer    | PK, Auto Increment |
| name        | Text       |                   |
| email       | Text       | Unique            |
| password    | Text       |                   |
| phone_no    | Numeric    | Unique            |
| age         | Integer    |                   |
| gender      | Enum       |                   |
| role        | Enum       | Default: Passenger|

### ✈️ Flight

| Field          | Type     |
|----------------|----------|
| flight_id      | Integer  |
| flight_number  | Integer  |
| source         | Text     |
| destination    | Text     |
| departure_time | Time     |
| arrival_time   | Time     |

### 📦 Booking

| Field         | Type    |
|---------------|---------|
| booking_id    | Integer |
| user_id (FK)  | Integer |
| flight_id (FK)| Integer |
| booking_status| Enum    |

### 💳 Payment

| Field          | Type     |
|----------------|----------|
| payment_id     | Integer  |
| booking_id (FK)| Integer  |
| amount         | Integer  |
| payment_status | Enum     |
| payment_date   | Date     |
| transaction_id | UUID     |

### 🛄 Check-In

| Field           | Type    |
|-----------------|---------|
| checkin_id      | Integer |
| booking_id (FK) | Integer |
| flight_id (FK)  | Integer |
| checkin_status  | Enum    |
| checkin_time    | Time    |

---

## 💳 Stripe Integration Flow

1. User initiates payment with only `bookingId`.
2. Backend fetches user email via `Booking → User`.
3. Stripe session is created with the email.
4. Session URL & ID is returned.
5. On success, booking status is updated.

---

## 📩 Email Notification

- Triggered after successful check-in.
- Email contains:
  - Passenger Name
  - Flight Details
  - Boarding Pass Confirmation
- Sent using `JavaMailSender` with configured SMTP.

---

## 🔐 Sample API Endpoints

| Method | Endpoint                            | Description                    |
|--------|-------------------------------------|--------------------------------|
| POST   | `/users`                   | Register new user             |
| POST   | `/bookings/`                        | Book a flight                 |
| POST   | `/payments/stripepayment`           | Initiate Stripe payment       |
| POST   | `/checkin/`                         | Perform check-in & send email |
| GET    | `/flights/from-to?src=X&dest=Y`     | Get flights by route          |

---

