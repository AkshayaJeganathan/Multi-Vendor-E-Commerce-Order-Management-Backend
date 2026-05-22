# ════════════════════════════════════════════════════════════
#  Multi-Vendor E-Commerce Order Management System
#  COMPLETE SETUP GUIDE — Placement Level Project
# ════════════════════════════════════════════════════════════

---

## PROJECT OVERVIEW

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Backend      | Spring Boot 3.2.4, Java 17          |
| ORM          | Spring Data JPA / Hibernate         |
| Database     | MySQL 8.x                           |
| Build Tool   | Apache Maven 3.9+                   |
| Frontend     | HTML5, CSS3, Vanilla JS, Custom CSS |
| Testing      | JUnit 5, Mockito                    |
| API Style    | REST (JSON)                         |

---

## STEP 1 — SOFTWARE TO INSTALL

### 1.1 Java Development Kit (JDK 17)

**Windows:**
1. Go to https://adoptium.net
2. Download **Temurin 17 (LTS)** → Windows x64 .msi installer
3. Run installer → check "Set JAVA_HOME" and "Add to PATH"
4. Verify: open Command Prompt → type `java -version`
   Expected: `openjdk version "17.x.x"`

**macOS:**
```bash
brew install openjdk@17
echo 'export JAVA_HOME=$(brew --prefix openjdk@17)' >> ~/.zshrc
source ~/.zshrc
java -version
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
java -version
```

---

### 1.2 Apache Maven 3.9+

**Windows:**
1. Go to https://maven.apache.org/download.cgi
2. Download **Binary zip archive** (apache-maven-3.9.x-bin.zip)
3. Extract to `C:\Program Files\Maven`
4. Add to System Environment Variables:
   - Variable: `MAVEN_HOME` → Value: `C:\Program Files\Maven\apache-maven-3.9.x`
   - Append to `PATH`: `%MAVEN_HOME%\bin`
5. Verify: `mvn -version`

**macOS:**
```bash
brew install maven
mvn -version
```

**Linux:**
```bash
sudo apt install maven -y
mvn -version
```

---

### 1.3 MySQL 8.x

**Windows:**
1. Go to https://dev.mysql.com/downloads/installer/
2. Download **MySQL Installer (Community)** — full version
3. Run installer → choose "Developer Default" setup type
4. Set root password: `root` (or your own — update application.properties)
5. Complete installation
6. MySQL service will auto-start

**macOS:**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation   # set root password
```

**Linux:**
```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql
sudo mysql_secure_installation  # set root password
```

**Test MySQL connection:**
```bash
mysql -u root -p
# Enter your password
```

---

### 1.4 IDE — IntelliJ IDEA (Recommended)

1. Go to https://www.jetbrains.com/idea/download/
2. Download **Community Edition** (free) — sufficient for this project
3. Install and open IntelliJ IDEA
4. Install plugin (optional): **Lombok** plugin
   - File → Settings → Plugins → Marketplace → search "Lombok" → Install → Restart

**Alternatively — Eclipse:**
1. Go to https://www.eclipse.org/downloads/
2. Download **Eclipse IDE for Enterprise Java and Web Developers**
3. Install Spring Tools 4 plugin via Help → Eclipse Marketplace

---

## STEP 2 — DATABASE SETUP

### 2.1 Create the Database

Open MySQL Workbench or Command Line:

```sql
-- Option A: Command Line
mysql -u root -p

-- Inside MySQL prompt:
CREATE DATABASE multivendor_ecom;
SHOW DATABASES;   -- verify it appears
USE multivendor_ecom;
EXIT;
```

---

### 2.2 Run schema.sql

**Via MySQL Command Line:**
```bash
mysql -u root -p multivendor_ecom < path/to/ecommerce-backend/src/main/resources/schema.sql
```

Example (Windows):
```bash
mysql -u root -p multivendor_ecom < C:\projects\ecommerce-backend\src\main\resources\schema.sql
```

Example (Mac/Linux):
```bash
mysql -u root -p multivendor_ecom < ~/projects/ecommerce-backend/src/main/resources/schema.sql
```

**Via MySQL Workbench:**
1. Open MySQL Workbench → connect to localhost
2. File → Open SQL Script → navigate to `schema.sql`
3. Click the ⚡ (Execute All) button
4. Verify tables: `SHOW TABLES;`

Expected tables created:
```
sellers, products, inventory, customers, orders, order_items, returns, payouts
```

Sample data is also inserted automatically.

---

### 2.3 Verify Sample Data

```sql
USE multivendor_ecom;
SELECT * FROM sellers;       -- should show 3 sellers
SELECT * FROM products;      -- should show 5 products
SELECT * FROM inventory;     -- should show 5 inventory records
SELECT * FROM customers;     -- should show 3 customers
```

---

## STEP 3 — CONFIGURE application.properties

Open `src/main/resources/application.properties` and update your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/multivendor_ecom?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root     ← CHANGE THIS to your MySQL root password
```

If your MySQL runs on a different port, change `3306` accordingly.

---

## STEP 4 — IMPORT PROJECT INTO IDE

### IntelliJ IDEA:
1. Open IntelliJ IDEA
2. Click **Open** → Navigate to the `ecommerce-backend` folder → Click OK
3. IntelliJ detects it as a Maven project → Click **Trust Project**
4. Wait for Maven to download all dependencies (first time may take 2-5 minutes)
5. Enable Annotation Processing:
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check ✅ "Enable annotation processing" → OK

### Eclipse:
1. File → Import → Maven → Existing Maven Projects
2. Browse to `ecommerce-backend` folder → Finish
3. Right-click project → Maven → Update Project → OK

---

## STEP 5 — BUILD THE PROJECT

Open Terminal in the project root (`ecommerce-backend/`):

```bash
# Download dependencies and compile
mvn clean install -DskipTests

# Expected output:
# [INFO] BUILD SUCCESS
```

If build fails:
- Ensure Java 17 is active: `java -version`
- Ensure Maven finds Java: `mvn -version` should show Java 17
- Check internet connection (first build downloads ~100MB of dependencies)

---

## STEP 6 — RUN THE SPRING BOOT BACKEND

### Option A: From Terminal
```bash
cd ecommerce-backend
mvn spring-boot:run
```

### Option B: From IntelliJ IDEA
1. Open `EcommerceApplication.java`
2. Click the green ▶ Run button next to `public static void main`
3. Or: Right-click → Run 'EcommerceApplication'

### Option C: Run the JAR
```bash
mvn clean package -DskipTests
java -jar target/ecommerce-1.0.0.jar
```

**Expected startup output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
Tomcat started on port(s): 8080 (http)
Started EcommerceApplication in X.XXX seconds
```

Backend is running at: **http://localhost:8080**

---

## STEP 7 — RUN JUNIT TESTS

```bash
# Run all tests
mvn test

# Expected output:
# Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

Tests use H2 in-memory database — MySQL does NOT need to be running for tests.

---

## STEP 8 — OPEN THE FRONTEND

The frontend is plain HTML/CSS/JS — no build step needed.

### Option A: Open directly in browser
Navigate to the `frontend/` folder and double-click `index.html`

### Option B: Use VS Code Live Server (Recommended)
1. Install VS Code: https://code.visualstudio.com
2. Install extension: **Live Server** by Ritwick Dey
3. Open the `frontend/` folder in VS Code
4. Right-click `index.html` → **Open with Live Server**
5. Browser opens at `http://127.0.0.1:5500/index.html`

### Option C: Python HTTP Server
```bash
cd ecommerce-backend/frontend
python -m http.server 5500
# Open: http://localhost:5500
```

### Option D: Node.js serve
```bash
npx serve frontend/
```

> ⚠️ The backend must be running on port 8080 before using the frontend.

---

## STEP 9 — API TESTING

### Using Browser
Visit: http://localhost:8080/api/dashboard

### Using cURL (Terminal)

```bash
# Get all sellers
curl http://localhost:8080/api/sellers

# Register a seller
curl -X POST http://localhost:8080/api/sellers \
  -H "Content-Type: application/json" \
  -d '{"name":"My Shop","email":"myshop@test.com","phone":"9999999999","gstNumber":"GST999","address":"123 Main St"}'

# Get all products
curl http://localhost:8080/api/products

# Add a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"sellerId":1,"name":"Test Product","price":499.00,"mrp":799.00,"sku":"SKU-TEST-001","initialStock":50,"category":"Electronics"}'

# Get all orders
curl http://localhost:8080/api/orders

# Place an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"shippingAddress":"123 Test St","items":[{"productId":1,"quantity":2}]}'

# Update order status (PLACED → CONFIRMED)
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"CONFIRMED"}'

# Approve a return
curl -X PATCH http://localhost:8080/api/returns/1/approve

# Get payouts
curl http://localhost:8080/api/payouts
```

### Using Postman
1. Download Postman: https://www.postman.com/downloads/
2. Import this base URL: `http://localhost:8080/api`
3. Test all endpoints below

---

## STEP 10 — COMPLETE REST API REFERENCE

| Module      | Method | Endpoint                              | Description                    |
|-------------|--------|---------------------------------------|--------------------------------|
| Dashboard   | GET    | /api/dashboard                        | All stats                      |
| Sellers     | POST   | /api/sellers                          | Register seller                |
| Sellers     | GET    | /api/sellers                          | All sellers                    |
| Sellers     | GET    | /api/sellers/{id}                     | Get seller                     |
| Sellers     | PUT    | /api/sellers/{id}                     | Update seller                  |
| Sellers     | PATCH  | /api/sellers/{id}/status?status=X     | Change status                  |
| Products    | POST   | /api/products                         | Add product + auto-create inv  |
| Products    | GET    | /api/products                         | All products with stock        |
| Products    | GET    | /api/products/{id}                    | Get product                    |
| Products    | GET    | /api/products/seller/{sellerId}       | Products by seller             |
| Products    | PATCH  | /api/products/{id}/status?status=X    | Change status                  |
| Inventory   | GET    | /api/inventory                        | All inventory                  |
| Inventory   | GET    | /api/inventory/product/{productId}    | Stock for product              |
| Inventory   | POST   | /api/inventory/product/{id}/add?quantity=N | Add stock              |
| Customers   | POST   | /api/customers                        | Register customer              |
| Customers   | GET    | /api/customers                        | All customers                  |
| Orders      | POST   | /api/orders                           | Place order (reserves stock)   |
| Orders      | GET    | /api/orders                           | All orders                     |
| Orders      | GET    | /api/orders/{id}                      | Order detail with items        |
| Orders      | GET    | /api/orders/customer/{customerId}     | Orders by customer             |
| Orders      | GET    | /api/orders/status/{status}           | Orders by status               |
| Orders      | PATCH  | /api/orders/{id}/status               | Update status (pipeline)       |
| Returns     | POST   | /api/returns                          | Request return                 |
| Returns     | GET    | /api/returns                          | All returns                    |
| Returns     | GET    | /api/returns/{id}                     | Get return                     |
| Returns     | PATCH  | /api/returns/{id}/approve             | Approve (restores inventory)   |
| Returns     | PATCH  | /api/returns/{id}/reject              | Reject return                  |
| Payouts     | GET    | /api/payouts                          | All payouts                    |
| Payouts     | GET    | /api/payouts/seller/{sellerId}        | Payouts by seller              |
| Payouts     | GET    | /api/payouts/seller/{sellerId}/summary| Seller payout summary          |
| Payouts     | GET    | /api/payouts/status/{status}          | Payouts by status              |
| Payouts     | PATCH  | /api/payouts/{id}/process             | Disburse payout                |

---

## STEP 11 — BUSINESS FLOW WALKTHROUGH

### Complete Order Lifecycle (step by step):

```
1. Register Seller          POST /api/sellers
2. Add Product              POST /api/products       (inventory created auto)
3. Register Customer        POST /api/customers
4. Place Order              POST /api/orders         ← inventory RESERVED
5. Confirm Order            PATCH /api/orders/1/status {"status":"CONFIRMED"}
6. Ship Order               PATCH /api/orders/1/status {"status":"SHIPPED"}
7. Deliver Order            PATCH /api/orders/1/status {"status":"DELIVERED"}
                                                     ← inventory DEDUCTED
                                                     ← PAYOUT GENERATED (auto)
8. [Optional] Return        POST /api/returns
9. [Optional] Approve Return PATCH /api/returns/1/approve
                                                     ← inventory RESTORED
                                                     ← payout put on HOLD
10. Disburse Payout         PATCH /api/payouts/1/process
```

---

## PROJECT FOLDER STRUCTURE (Complete)

```
ecommerce-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/multivendor/ecommerce/
│   │   │   ├── EcommerceApplication.java
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java                  ← CORS config
│   │   │   ├── controller/
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── SellerController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── InventoryController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── ReturnController.java
│   │   │   │   └── PayoutController.java
│   │   │   ├── service/
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── SellerService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── InventoryService.java            ← Core business logic
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── OrderService.java                ← Pipeline + payout trigger
│   │   │   │   ├── ReturnService.java               ← Inventory restore
│   │   │   │   └── PayoutService.java               ← Commission calc
│   │   │   ├── repository/
│   │   │   │   ├── SellerRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── InventoryRepository.java         ← Custom @Query methods
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── OrderItemRepository.java
│   │   │   │   ├── ReturnRepository.java
│   │   │   │   └── PayoutRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Seller.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Inventory.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Return.java
│   │   │   │   └── Payout.java
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── DashboardDTO.java
│   │   │   │   ├── SellerDTO.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── InventoryDTO.java
│   │   │   │   ├── CustomerDTO.java
│   │   │   │   ├── OrderDTO.java
│   │   │   │   ├── ReturnDTO.java
│   │   │   │   └── PayoutDTO.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── BusinessException.java
│   │   │       └── InsufficientStockException.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/
│       ├── java/com/multivendor/ecommerce/
│       │   ├── SellerServiceTest.java
│       │   ├── OrderServiceTest.java
│       │   ├── InventoryServiceTest.java
│       │   ├── PayoutServiceTest.java
│       │   └── ReturnServiceTest.java
│       └── resources/
│           └── application-test.properties         ← H2 in-memory DB
└── frontend/
    ├── index.html                                  ← Dashboard
    ├── sellers.html
    ├── products.html
    ├── orders.html
    ├── returns.html
    ├── payouts.html
    ├── css/
    │   └── style.css                               ← Custom design system
    └── js/
        └── api.js                                  ← Shared API utilities
```

---

## COMMON ERRORS & FIXES

| Error | Cause | Fix |
|-------|-------|-----|
| `Communications link failure` | MySQL not running | Start MySQL service |
| `Access denied for user 'root'` | Wrong password | Update application.properties |
| `Unknown database 'multivendor_ecom'` | DB not created | Run `CREATE DATABASE multivendor_ecom;` |
| `Port 8080 already in use` | Another app on 8080 | Change `server.port=8081` in properties |
| `Lombok not working` | Annotation processing off | Enable in IDE settings |
| `CORS error in browser` | Backend not running | Start Spring Boot first |
| `BUILD FAILURE - Cannot find symbol` | Java version mismatch | Set Java 17 as project SDK |

---

## INTERVIEW TALKING POINTS

1. **Inventory Reservation Pattern** — Stock is reserved (not deducted) at order placement, deducted only at delivery. This prevents overselling.

2. **Order FSM (Finite State Machine)** — Strict status transitions enforced in OrderService.validateStatusTransition().

3. **Payout Trigger** — Payouts are event-driven: generated automatically when order status changes to DELIVERED.

4. **Return → Inventory Restore** — Approved returns call InventoryService.restoreStockOnReturn(), demonstrating bidirectional inventory management.

5. **Custom JPQL Queries** — InventoryRepository uses @Modifying @Query for atomic stock operations.

6. **Global Exception Handling** — @RestControllerAdvice intercepts all exceptions and returns consistent ApiResponse<T> structure.

7. **DTOs over Entities** — No entity is exposed directly to the API; all responses go through DTO transformation.

8. **JUnit + Mockito** — Service layer fully unit-tested with mocked repositories, 22 test cases covering happy paths and edge cases.
