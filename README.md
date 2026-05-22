# 🛒 Multi-Vendor E-Commerce Order Management System

A robust, enterprise-grade operations backend designed to manage the complex coordination between sellers, inventory, order fulfillment, returns, and commission payouts in a multi-vendor marketplace. 

Unlike standard storefront applications, this project models the **operations pipeline**—the critical infrastructure that marketplace companies (like Amazon, Flipkart, or Meesho) rely on to track money and physical goods.

## 🚀 Key Features

* **Seller & Catalog Management:** Register vendors, manage their status, and list products.
* **Inventory Reservation Pattern:** Prevents overselling by reserving stock at the exact moment an order is placed, and only deducting it upon final delivery.
* **Order Finite State Machine (FSM):** Enforces strict status transitions (e.g., `PLACED` → `CONFIRMED` → `SHIPPED` → `DELIVERED`).
* **Automated Commission Payouts:** Event-driven payouts trigger automatically when an order reaches `DELIVERED` status, calculating platform commission automatically.
* **Return Handling:** Approving returns automatically restores physical inventory and places seller payouts on hold.

## 🛠️ Tech Stack

**Backend**
* **Java 21**
* **Spring Boot 3.2.x** (Spring Web, Spring Data JPA)
* **Hibernate / ORM**
* **Maven** (Build & Dependency Management)

**Database**
* **MySQL 8.x** (Relational Database)
* **Custom JPQL** for atomic inventory operations

**Frontend (Admin Dashboard)**
* **HTML5 / CSS3 / Vanilla JavaScript**
* **Fetch API** for REST communication

---

## 🏗️ Architecture & Design Patterns

* **DTO Pattern:** Entities are never exposed directly to the API; all incoming and outgoing data passes through Data Transfer Objects mapping for security and clean payloads.
* **Global Exception Handling:** Utilizes `@RestControllerAdvice` to intercept exceptions (e.g., `InsufficientStockException`, `ResourceNotFoundException`) and return consistent JSON error responses.
* **CORS Configured:** Securely configured `WebMvcConfigurer` to allow communication between the separated frontend and backend environments.

---

## ⚙️ Local Setup Instructions

### Prerequisites
* Java Development Kit (JDK) 17 or 21
* Apache Maven 3.9+
* MySQL Server 8.x

### 1. Database Setup
Log into your MySQL instance and run the following command to create the database:
```sql
CREATE DATABASE multivendor_ecom;
