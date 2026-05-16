# 🌾 CropDeal – Full Stack Crop Trading Marketplace

<div align="center">

![Java](https://img.shields.io/badge/Java-JDK%2017-orange?style=for-the-badge\&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Microservices-brightgreen?style=for-the-badge\&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue?style=for-the-badge\&logo=mysql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-ff6600?style=for-the-badge\&logo=rabbitmq)
![REST API](https://img.shields.io/badge/API-RESTful-success?style=for-the-badge)
![Spring Security](https://img.shields.io/badge/Security-Spring%20Security-green?style=for-the-badge)

### A scalable microservices-based agricultural marketplace enabling direct farmer-to-dealer transactions.

</div>

---

# 📌 Overview

**CropDeal** is a full-stack crop trading marketplace built using a **Java/J2EE microservices architecture** that enables direct interaction between farmers and dealers while eliminating traditional intermediaries.

The platform focuses on scalability, reliability, and secure communication by leveraging:

* **Spring Boot Microservices**
* **RabbitMQ Event-Driven Messaging**
* **Feign Client Service Communication**
* **Event Sourcing for Payment Auditing**
* **Spring Security for RBAC**
* **MySQL Relational Data Modeling**

The project was designed to simulate a real-world distributed marketplace system capable of handling asynchronous operations, secure transactions, and service decoupling at scale.

---

# 🚀 Key Features

## 👨‍🌾 Farmer Module

* Farmer registration and authentication
* Crop listing and inventory management
* Real-time crop availability updates
* Direct communication with dealers
* Secure order and payment handling

## 🏢 Dealer Module

* Dealer registration and login
* Browse and purchase crop listings
* Order management system
* Payment tracking and transaction history
* Crop search and filtering

## 🔐 Security Features

* Role-Based Access Control (RBAC)
* JWT/Spring Security authentication
* Protected REST endpoints
* Secure inter-service communication

## ⚡ Microservices Features

* Independent deployable services
* Service-to-service communication using Feign Client
* Event-driven asynchronous processing with RabbitMQ
* Improved scalability and fault isolation

## 💳 Payment & Transaction Features

* Event Sourcing for payment audit trails
* Complete transaction history tracking
* Reliable event persistence
* Consistent payment state management

---

# 🏗️ System Architecture

The application follows a **Microservices Architecture** where multiple independent services communicate through REST APIs and asynchronous event messaging.

## Architecture Highlights

* **API Gateway** for routing requests
* **Authentication Service** for login and authorization
* **Crop Service** for crop inventory management
* **Order Service** for order lifecycle management
* **Payment Service** for transaction handling
* **RabbitMQ Broker** for asynchronous messaging
* **MySQL Database** for persistent storage

---

# 🧩 Tech Stack

| Category              | Technologies            |
| --------------------- | ----------------------- |
| Backend               | Java, J2EE, Spring Boot |
| Architecture          | Microservices           |
| Security              | Spring Security         |
| Messaging             | RabbitMQ                |
| Service Communication | OpenFeign Client        |
| Database              | MySQL                   |
| ORM / Persistence     | JPA / Hibernate         |
| API Standard          | RESTful APIs, XML       |
| Build Tool            | Maven                   |
| Version Control       | Git & GitHub            |

---

# ⚙️ Microservices Used

The project is divided into multiple independent services:

## 1. Authentication Service

Handles:

* User authentication
* Authorization
* Role management
* Security token validation

## 2. Farmer Service

Handles:

* Farmer profiles
* Crop listings
* Inventory updates

## 3. Dealer Service

Handles:

* Dealer profiles
* Dealer purchase workflows
* Order requests

## 4. Crop Service

Handles:

* Crop management
* Availability tracking
* Crop metadata

## 5. Order Service

Handles:

* Order creation
* Order status updates
* Transaction coordination

## 6. Payment Service

Handles:

* Payment processing
* Event sourcing
* Audit trail maintenance

---

# 🔄 Event-Driven Communication

CropDeal uses **RabbitMQ** to enable asynchronous communication between services.

## Benefits Achieved

* Reduced service coupling
* Improved throughput
* Better fault tolerance
* Reliable event processing
* Scalable asynchronous workflows

## Event Examples

* Order Created Event
* Payment Completed Event
* Inventory Updated Event
* Transaction Audit Event

---

# 🧾 Event Sourcing Implementation

The Payment Service implements **Event Sourcing** patterns to maintain:

* Complete payment history
* Immutable transaction logs
* Consistent audit trails
* Reliable transactional recovery

This approach ensures transparency and traceability for all financial operations.

---

# 🛡️ Security Implementation

Security is implemented using **Spring Security** with role-based access.

## Security Features

* Authentication & Authorization
* Role-based endpoint protection
* Secure API access
* Session validation
* Request filtering

## Supported Roles

* ADMIN
* FARMER
* DEALER

---

# 🗄️ Database Design

The application uses **MySQL relational database design** for persistent storage.

## Database Features

* Relational schema modeling
* SQL query optimization
* Transaction management
* Entity relationships
* Data normalization

## Major Tables

* Users
* Farmers
* Dealers
* Crops
* Orders
* Payments
* Transactions

---

# 📡 REST API Design

The platform exposes RESTful APIs following standard web principles.

## API Features

* REST-compliant endpoints
* JSON/XML support
* Proper HTTP status codes
* Request validation
* Layered architecture

### Example Endpoints

```http
POST /api/auth/login
GET /api/crops
POST /api/orders
GET /api/payments/history
```

---

# 📂 Project Structure

```bash
CropDeal/
│
├── api-gateway/
├── auth-service/
├── farmer-service/
├── dealer-service/
├── crop-service/
├── order-service/
├── payment-service/
├── common-library/
├── rabbitmq-config/
├── database/
└── README.md
```

---

# ▶️ Getting Started

## Prerequisites

Make sure the following are installed:

* Java JDK 17+
* Maven
* MySQL
* RabbitMQ Server
* Git
* IDE (IntelliJ IDEA / VS Code / Eclipse)

---

# 🔧 Installation & Setup

## 1. Clone the Repository

```bash
git clone https://github.com/Naincyshrivastava05/CropDeal.git
cd CropDeal
```

## 2. Configure MySQL Database

Update application properties:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cropdeal
spring.datasource.username=root
spring.datasource.password=your_password
```

---

## 3. Start RabbitMQ

Run RabbitMQ locally:

```bash
rabbitmq-server
```

RabbitMQ Dashboard:

```text
http://localhost:15672
```

---

## 4. Build the Project

```bash
mvn clean install
```

---

## 5. Run Microservices

Start each service individually:

```bash
mvn spring-boot:run
```

---

# 📈 Scalability & Reliability

The system is designed for distributed scalability.

## Improvements Achieved

* Decoupled service architecture
* Independent service deployment
* High availability messaging
* Better fault isolation
* Improved throughput using asynchronous processing

---

# 🧪 Future Enhancements

Potential future improvements:

* Docker & Kubernetes deployment
* API Gateway rate limiting
* Distributed tracing with Zipkin
* CI/CD integration
* Real-time notifications
* AI-based crop price prediction
* Elasticsearch integration
* Cloud deployment (AWS/Azure/GCP)

---

# 📚 Learning Outcomes

This project helped in gaining hands-on experience with:

* Enterprise-level backend development
* Microservices architecture patterns
* Distributed system communication
* Event-driven systems
* Secure API development
* Database modeling & optimization
* Event sourcing implementation
* Scalable system design

---

# 🤝 Contribution

Contributions, issues, and feature requests are welcome.

Feel free to fork the repository and submit pull requests.

---

# 📄 License

This project is developed for educational and portfolio purposes.

---

# 👩‍💻 Author

## Naincy Shrivastava

* Full Stack Java Developer
* Passionate about scalable backend systems and distributed architectures

### GitHub Repository

```text
https://github.com/Naincyshrivastava05/CropDeal
```

---

<div align="center">

### ⭐ If you found this project useful, consider giving it a star on GitHub.

</div>

