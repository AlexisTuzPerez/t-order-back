## Overview

T-Order is a real-world, production-ready self-service cafeteria management system with the purpose of change the dining experience by eliminating the need for traditional waitstaff. This innovative solution allows customers to place orders directly from their tables using their mobile devices, streamlining the ordering process and enhancing customer satisfaction and order speed.

## Key Features

### API Endpoints
- 🔐 Secure authentication and authorization
- 🛒 Order management system
- 🏢 Multi-sucursal (branch) management
- 📊 Business analytics and reporting
- 📱 User and employee management

### Technical Highlights
- 🔐 Robust JWT-based authentication
- 🛡️ Role-based access control
- 🔄 Real-time order updates
- 📦 Modular architecture for easy maintenance

## Technical Stack

### Backend
- **Framework**: Spring Boot 3.4.5
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Validation**: Bean Validation
- **ORM**: Spring Data JPA
- **Dependency Injection**: Spring IoC

### Key Dependencies
```xml
- Spring Boot Web
- Spring Data JPA
- PostgreSQL Driver
- JWT (Java JWT)
- Lombok
- Spring Security
- Dotenv for environment variables
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6.0 or higher
- PostgreSQL 15

### Installation
1. Clone the repository
```bash
git clone https://github.com/AlexisTuzPerez/t-order-back
cd t-order
```

2. Set up environment variables
Create a `.env` file in the root directory:
```properties
DB_URL=jdbc:postgresql://localhost:5432/torder
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
```

3. Build and run the application
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Project Structure

```
t-order/
├── src/main/java/com/torder/
│   ├── auth/              # Authentication and authorization
│   ├── negocioCliente/    # Business management
│   ├── sucursal/          # Branch management
│   ├── orden/            # Order management
│   ├── producto/         # Product management
│   ├── user/             # User management
│   ├── config/           # Application configuration
│   └── TOrderApplication.java
├── pom.xml
└── .env
```

## License

This project is proprietary and confidential. All rights reserved.


