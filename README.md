# Patient Management System

A microservices-based healthcare management system built with Spring Boot, featuring JWT authentication, gRPC communication, event-driven architecture with Kafka, and cloud-native deployment on AWS using LocalStack.

## 🏗️ Architecture Overview

This system follows a **microservices architecture** with the following components:

```
                                    ┌─────────────────┐
                                    │   HTTP Client   │
                                    └────────┬────────┘
                                             │
                                             ▼
                              ┌──────────────────────────────┐
                              │   Application Load Balancer  │
                              │      (AWS ALB/LocalStack)    │
                              └──────────────┬───────────────┘
                                             │
                                             ▼
                              ┌──────────────────────────────┐
                              │       API Gateway            │
                              │  (Spring Cloud Gateway)      │
                              │  - JWT Validation Filter     │
                              │  - Routing & Load Balancing  │
                              └──────┬───────────────────────┘
                                     │
                    ┌────────────────┼────────────────┐
                    │                │                │
                    ▼                ▼                ▼
         ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐
         │  Auth Service    │  │   Patient    │  │   Billing    │
         │  (Port 4005)     │  │   Service    │  │   Service    │
         │                  │  │  (Port 4000) │  │  (Port 4001) │
         │ - JWT Generation │  │              │  │              │
         │ - Token Validate │  │ - CRUD Ops   │  │ - gRPC Server│
         │ - BCrypt Auth    │  │ - gRPC Client│◄─┼──(Port 9001)│
         └────────┬─────────┘  └──────┬───────┘  └──────┬───────┘
                  │                   │                  │
                  ▼                   ▼                  │
         ┌──────────────────┐  ┌──────────────┐         │
         │   PostgreSQL     │  │  PostgreSQL  │         │
         │   (Auth DB)      │  │ (Patient DB) │         │
         │   Port: 5001     │  │  Port: 5433  │         │
         └──────────────────┘  └──────────────┘         │
                                      │                  │
                                      │ Kafka Producer   │ Kafka Consumer
                                      ▼                  ▼
                              ┌────────────────────────────────┐
                              │      Apache Kafka (MSK)        │
                              │  - Patient Events Topic        │
                              │  - Billing Events Topic        │
                              └────────────┬───────────────────┘
                                           │
                                           │ Kafka Consumer
                                           ▼
                                  ┌──────────────────┐
                                  │    Analytics     │
                                  │     Service      │
                                  │   (Port 4002)    │
                                  │ - Event Processing│
                                  └──────────────────┘

Communication Patterns:
━━━━━━━━━━━━━━━━━━━━
→  HTTP/REST (Synchronous)
⟿  gRPC (High-performance RPC)
⤳  Kafka Events (Asynchronous)
```

## 🚀 Services

### 1. **API Gateway** (Port 4004)
- Entry point for all client requests
- Routes traffic to appropriate microservices
- JWT token validation using reactive WebClient
- Built with **Spring Cloud Gateway**

### 2. **Auth Service** (Port 4005)
- User authentication and authorization
- JWT token generation and validation
- Password encryption with BCrypt
- PostgreSQL database for user storage
- Technologies: **Spring Security**, **JJWT**, **JPA**

### 3. **Patient Service** (Port 4000)
- CRUD operations for patient records
- gRPC client for billing service communication
- Kafka producer for patient events
- PostgreSQL database
- Technologies: **Spring Data JPA**, **gRPC**, **Kafka**

### 4. **Billing Service** (Port 4001, gRPC 9001)
- Manages billing accounts
- Exposes gRPC server for synchronous communication
- Kafka consumer for billing events
- Technologies: **gRPC**, **Protocol Buffers**, **Kafka**

### 5. **Analytics Service** (Port 4002)
- Processes patient events from Kafka
- Real-time analytics and reporting
- Event-driven architecture
- Technologies: **Spring Kafka**, **Protocol Buffers**

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 3.5.4** - Modern Java framework
- **Java 17** - LTS version with modern features

### Communication
- **REST APIs** - HTTP-based communication
- **gRPC** - High-performance RPC framework
- **Apache Kafka** - Event streaming platform
- **Protocol Buffers** - Efficient serialization

### Security
- **Spring Security** - Authentication & Authorization
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing

### Data & Persistence
- **Spring Data JPA** - ORM framework
- **PostgreSQL** - Relational database
- **Hibernate** - JPA implementation

### API Gateway
- **Spring Cloud Gateway** - Reactive gateway
- **WebFlux** - Reactive web framework

### Infrastructure as Code
- **AWS CDK (Java)** - Cloud infrastructure definition
- **CloudFormation** - AWS resource provisioning
- **LocalStack** - Local AWS cloud stack

### DevOps & Deployment
- **Docker** - Containerization
- **AWS ECS Fargate** - Serverless container orchestration
- **Application Load Balancer** - Traffic distribution
- **AWS RDS** - Managed PostgreSQL
- **AWS MSK** - Managed Kafka

### Build & Development
- **Maven** - Dependency management
- **Protobuf Maven Plugin** - Protocol Buffers compilation
- **SpringDoc OpenAPI** - API documentation

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- LocalStack (Pro version for ECS)
- AWS CLI
- PostgreSQL (for local development)

## 🔧 Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd patient-management
```

### 2. Build All Services
```bash
# Build each service
cd auth-service && mvn clean install && cd ..
cd patient-service && mvn clean install && cd ..
cd billing-service && mvn clean install && cd ..
cd analytics-service && mvn clean install && cd ..
cd api-gateway && mvn clean install && cd ..
```

### 3. Build Docker Images
```bash
# From each service directory
docker build -t auth-service:latest .
docker build -t patient-service:latest .
docker build -t billing-service:latest .
docker build -t analytics-service:latest .
docker build -t api-gateway:latest .
```

## 🚢 Deployment

### LocalStack Deployment

1. **Start LocalStack**
```bash
docker run -d --name localstack \
  -p 4566:4566 \
  -p 4510-4560:4510-4560 \
  localstack/localstack-pro
```

2. **Generate CDK Template**
```bash
cd infrastructure
mvn compile exec:java -Dexec.mainClass="com.pm.stack.LocalStack"
```

3. **Deploy to LocalStack**
```bash
bash localstack-deploy.sh
```

The script will output the load balancer URL for testing.

## 📡 API Endpoints

### Authentication
```bash
# Login
POST http://<load-balancer-url>:4566/auth/login
Content-Type: application/json
{
  "email": "testuser@test.com",
  "password": "password123"
}

# Validate Token
GET http://<load-balancer-url>:4566/auth/validate
Authorization: Bearer <token>
```

### Patient Management
```bash
# Get All Patients
GET http://<load-balancer-url>:4566/api/patients
Authorization: Bearer <token>

# Create Patient
POST http://<load-balancer-url>:4566/api/patients
Authorization: Bearer <token>
Content-Type: application/json
{
  "name": "John Doe",
  "email": "john@example.com",
  "address": "123 Main St",
  "dateOfBirth": "1990-01-01",
  "registeredDate": "2024-01-01"
}

# Update Patient
PUT http://<load-balancer-url>:4566/api/patients/{id}
Authorization: Bearer <token>

# Delete Patient
DELETE http://<load-balancer-url>:4566/api/patients/{id}
Authorization: Bearer <token>
```

## 🔐 Security

- **JWT-based authentication** with 10-hour token expiration
- **BCrypt password hashing** for secure storage
- **Gateway-level token validation** before routing requests
- **Role-based access control** (ADMIN role)

## 📊 Event-Driven Architecture

The system uses **Apache Kafka** for asynchronous communication:

- **Patient Service** publishes patient creation/update events
- **Billing Service** consumes events to create billing accounts
- **Analytics Service** processes events for reporting

## 🧪 Testing

API request files are provided in the `api-requests/` directory:
- `auth-requests/login.http` - Authentication
- `patient-service/*.http` - Patient operations

Use IntelliJ HTTP Client or similar tools to execute requests.

## 📁 Project Structure

```
patient-management/
├── api-gateway/          # Spring Cloud Gateway
├── auth-service/         # Authentication & JWT
├── patient-service/      # Patient CRUD + gRPC client
├── billing-service/      # gRPC server + Kafka consumer
├── analytics-service/    # Kafka consumer
├── infrastructure/       # AWS CDK for IaC
├── api-requests/         # HTTP test files
└── LOCALSTACK_DEPLOYMENT.md
```

## 📚 Learning Resources

This project demonstrates enterprise-level microservices patterns. Study these topics on YouTube for deeper understanding:

- **Microservices Architecture** - System design and service decomposition
- **Spring Boot REST APIs** - Building production-ready APIs
- **Spring Cloud Gateway** - API Gateway patterns and reactive programming
- **JWT Authentication** - Stateless authentication mechanisms
- **gRPC & Protocol Buffers** - High-performance RPC communication
- **Apache Kafka** - Event-driven architecture and message streaming
- **Spring Data JPA** - ORM and database interactions
- **Docker & Containerization** - Container orchestration
- **AWS ECS & Fargate** - Serverless container deployment
- **Infrastructure as Code** - AWS CDK and CloudFormation
