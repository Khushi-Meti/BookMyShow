# BookMyShow Backend System

A Spring Boot-based backend system for a movie booking application similar to BookMyShow.

## Project Structure

```
BookMyShow/
├── src/
│   ├── main/
│   │   ├── java/com/bookmyshow/
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── model/               # JPA entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   └── BookMyShowApplication.java  # Main application class
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/bookmyshow/
├── pom.xml                          # Maven configuration
├── .gitignore
└── README.md
```

## Technology Stack

- **Java 17**: Programming language
- **Spring Boot 3.1.5**: Framework
- **Spring Web**: REST API development
- **Spring Data JPA**: ORM and database operations
- **Hibernate**: JPA implementation
- **MySQL**: Database
- **Lombok**: Reducing boilerplate code
- **Maven**: Build tool

## Project Layers

### 1. Controller Layer
REST API endpoints for handling HTTP requests. Located in `controller/` package.
- `MovieController`: Endpoints for movie operations
- `UserController`: Endpoints for user operations

### 2. Service Layer
Business logic and transaction management. Located in `service/` package.
- `MovieService`: Movie-related business operations
- `UserService`: User-related business operations

### 3. Repository Layer
Data access and database operations. Located in `repository/` package.
- `MovieRepository`: JPA operations for Movie entity
- `UserRepository`: JPA operations for User entity

### 4. Model Layer
JPA entities representing database tables. Located in `model/` package.
- `Movie`: Movie entity with properties like title, genre, duration, etc.
- `User`: User entity with properties like email, firstname, lastname, etc.

### 5. DTO Layer
Data transfer objects for API requests/responses. Located in `dto/` package.
- `MovieDTO`: Data transfer object for movies
- `UserDTO`: Data transfer object for users

## Prerequisites

- JDK 17 or higher
- MySQL 5.7 or higher
- Maven 3.6 or higher

## Setup Instructions

### 1. Database Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookmyshow_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 2. Create Database

```sql
CREATE DATABASE IF NOT EXISTS bookmyshow_db;
USE bookmyshow_db;
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/bookmyshow-1.0.0.jar
```

The application will start on `http://localhost:8080/api`

## API Endpoints

### Movie Endpoints

- `POST /api/movies` - Create a new movie
- `GET /api/movies` - Get all movies
- `GET /api/movies/{id}` - Get movie by ID
- `GET /api/movies/genre/{genre}` - Get movies by genre
- `PUT /api/movies/{id}` - Update a movie
- `DELETE /api/movies/{id}` - Delete a movie

### User Endpoints

- `POST /api/users` - Create a new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `PUT /api/users/{id}` - Update a user
- `DELETE /api/users/{id}` - Delete a user

## Sample Request/Response

### Create Movie

**Request:**
```json
POST /api/movies
{
  "title": "Inception",
  "description": "A thief who steals corporate secrets through dream-sharing technology",
  "genre": "Science Fiction",
  "duration": 148,
  "language": "English",
  "releaseDate": "2010-07-16",
  "rating": 8.8
}
```

**Response:**
```json
{
  "id": 1,
  "title": "Inception",
  "description": "A thief who steals corporate secrets through dream-sharing technology",
  "genre": "Science Fiction",
  "duration": 148,
  "language": "English",
  "releaseDate": "2010-07-16",
  "rating": 8.8,
  "isActive": true
}
```

## Notes

- JPA is configured to auto-update the database schema (`spring.jpa.hibernate.ddl-auto=update`)
- Timestamps are automatically managed for `createdAt` and `updatedAt` fields
- Logging is configured with SLF4J and Logback
- The project uses Lombok annotations to reduce boilerplate code

## Future Enhancements

- Add authentication and authorization (Spring Security)
- Add shows/showtimes functionality
- Add bookings and seat management
- Add payment processing
- Add email notifications
- Add search and filtering capabilities
- Add caching (Redis)
- Add API documentation (Swagger/SpringFox)

## License

This project is licensed under the MIT License.
