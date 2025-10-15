# PicoContainer Microservice

A basic microservice showing a **PicoContainer dependency injection** with CRUD operations for User management.
Reference: http://picocontainer.com/

### Key Differences from Spring Boot:

1. **Manual Container Configuration**: Explicitly register components
2. **Constructor Injection**: PicoContainer focuses on constructor-based dependency injection
4. **No Annotations**: No `@Component`, `@Service`, `@Repository` annotations

### PicoContainer Registration Pattern:

```java
// In ApplicationConfig.java
MutablePicoContainer container = new DefaultPicoContainer();

// Register implementations
container.addComponent(UserRepository.class, H2UserRepository.class);
container.addComponent(UserService.class, UserServiceImpl.class);

// Dependencies are automatically resolved via constructor injection
```

## Running the Application

### Using Gradle

```bash
# Build the project
./gradlew clean build
```

```bash
#Run the application
 ./gradlew run
```

## API Usage Examples

### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "age": 30
  }'
```

### Get All Users
```bash
curl http://localhost:8080/api/users
```

### Get User by ID
```bash
curl http://localhost:8080/api/users/1
```

### Update User
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "age": 28
  }'
```

### Delete User
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

### Health Check
```bash
curl http://localhost:8080/health
```