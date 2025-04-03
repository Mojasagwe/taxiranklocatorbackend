# Taxi Rank Backend

A Spring Boot application for managing taxi ranks, routes, and users.

[![CI/CD Status](https://github.com/Mojasagwe/taxiranklocatorbackend/actions/workflows/backend.yml/badge.svg)](https://github.com/Mojasagwe/taxiranklocatorbackend/actions/workflows/backend.yml)

## Features

- User Management (CRUD operations)
- Route Management
- Rank Management
- Security with Spring Security
- H2 Database for development and testing

## Prerequisites

- Java 17 or later
- Gradle 8.x
- H2 Database (included)

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/yourusername/taxi-rank-backend.git
cd taxi-rank-backend
```

2. Build the project:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew bootRun
```

4. Access the H2 Console:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:taxiranktest_db
- Username: sa
- Password: password

## API Endpoints

### Users
- GET /api/users - List all users
- GET /api/users/by-role/{role} - Get users by role
- GET /api/users/{id} - Get user by ID
- POST /api/users - Create new user
- PUT /api/users/{id} - Update user
- DELETE /api/users/{id} - Delete user

### Routes
- GET /api/routes - List all routes
- GET /api/routes/{id} - Get route by ID
- POST /api/routes - Create new route
- PUT /api/routes/{id} - Update route
- DELETE /api/routes/{id} - Delete route

### Ranks
- GET /api/ranks - List all ranks
- GET /api/ranks/{id} - Get rank by ID
- POST /api/ranks - Create new rank
- PUT /api/ranks/{id} - Update rank
- DELETE /api/ranks/{id} - Delete rank

## Development

The project uses:
- Spring Boot 3.2.3
- Spring Data JPA
- Spring Security
- H2 Database
- Lombok
- Gradle

## Testing

The application uses H2 in-memory database for testing. To run tests:
```bash
./gradlew test
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 