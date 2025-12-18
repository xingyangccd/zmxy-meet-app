# ComectME 🎓

A modern social networking platform designed for campus communities, featuring real-time messaging, post sharing, and social circles.

## Folder Introduction:
Spr: springboot: backend project

And: Kotlin project

Video: Video

Apk: The executable apk file of the project


## 📱 Tech Stack

### Frontend (Android)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Key Libraries**:
  - Material Design 3
  - Kotlin Coroutines
  - Retrofit 2 (HTTP Client)
  - Coil (Image Loading)
  - Koin (Dependency Injection)
  - Navigation Compose

### Backend (Spring Boot)
- **Language**: Java
- **Framework**: Spring Boot 3
- **Database**: MySQL
- **Cache**: Redis
- **Storage**: MinIO (Object Storage)
- **Key Features**:
  - RESTful API
  - JWT Authentication
  - Spring Security
  - MyBatis-Plus (ORM)
  - WebSocket (Real-time Chat)

## 🏗️ Project Structure

```
campus-social/
├── android/                 # Android Client
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/xingyang/
│   │   │   │   │   ├── data/       # Data layer (API, models)
│   │   │   │   │   ├── di/         # Dependency injection
│   │   │   │   │   ├── ui/         # UI layer (screens, components)
│   │   │   │   │   └── util/       # Utilities
│   │   │   │   └── res/             # Resources (layouts, strings, images)
│   │   └── build.gradle
│   └── gradle/
│
├── server/                  # Spring Boot Server
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml              # Maven configuration
│   └── application.yml      # Application configuration
│
└── docs/                    # Documentation
    ├── API.md              # API documentation
    └── DATABASE.md         # Database schema
```

## 🚀 Features

### User Features
- ✅ User registration and authentication
- ✅ Profile management
- ✅ Post creation with images and videos
- ✅ Like, comment, and share posts
- ✅ Real-time messaging
- ✅ Push notifications
- ✅ User follow/unfollow
- ✅ Content discovery
- ✅ Social circles/groups

### Technical Features
- ✅ JWT-based authentication
- ✅ Real-time WebSocket communication
- ✅ Image and video upload
- ✅ Offline caching
- ✅ Material Design 3 UI
- ✅ Dark mode support
- ✅ Responsive design

## 📋 Prerequisites

### For Android Development
- JDK 17 or higher
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 33+
- Gradle 8.0+

### For Backend Development
- JDK 17 or higher
- Maven 3.6+ or Gradle
- MySQL 8.0+
- Redis 6.0+
- MinIO Server

## 🛠️ Setup Instructions

### Backend Setup

1. **Clone the repository**
```bash
git clone git@github.com:xingyangccd/zmxy-meet-app.git
cd campus-social/server
```

2. **Configure database**
- Create a MySQL database
- Update `application.yml` with your database credentials

3. **Configure Redis**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

4. **Configure MinIO**
```yaml
minio:
  endpoint: http://localhost:9000
  accessKey: your-access-key
  secretKey: your-secret-key
```

5. **Run the application**
```bash
mvn spring-boot:run
# or
./mvnw spring-boot:run
```

The server will start on `http://localhost:8080`

## 🔗 Access URLs

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs JSON**: http://localhost:8081/v3/api-docs
- **API Docs YAML**: http://localhost:8081/v3/api-docs.yaml

### Android Setup

1. **Open Android project**
```bash
cd campus-social/android
```
Open the `android` folder in Android Studio

2. **Configure API endpoint**
Update the base URL in `ApiService.kt`:
```kotlin
private const val BASE_URL = "http://your-server-ip:8080/"
```

3. **Sync Gradle**
Let Android Studio sync the Gradle files

4. **Run the app**
- Connect an Android device or start an emulator
- Click Run (▶️) in Android Studio

## 🔧 Configuration

### Environment Variables

Create a `local.properties` file (not committed to Git):

```properties
# Android
sdk.dir=/path/to/Android/Sdk

# Backend API
api.base.url=http://localhost:8080/
```

### Database Schema

Run the SQL scripts in `server/src/main/resources/db/`:
```bash
mysql -u username -p database_name < schema.sql
mysql -u username -p database_name < data.sql
```

## 📡 API Documentation

### 🎯 Interactive API Documentation (Swagger UI)

The backend includes **Swagger/OpenAPI** for interactive API documentation.

**Access Swagger UI:**
```
http://localhost:8081/swagger-ui.html
```

**Access OpenAPI JSON:**
```
http://localhost:8081/v3/api-docs
```

#### Features:
- ✅ Interactive API testing
- ✅ Real-time request/response examples
- ✅ JWT authentication support
- ✅ Automatic API endpoint discovery
- ✅ Request/Response schema validation

#### How to Use Swagger:
1. Start the Spring Boot backend server
2. Open browser and navigate to `http://localhost:8081/swagger-ui.html`
3. Authorize with JWT token (click "Authorize" button)
4. Test any API endpoint directly from the browser

---

### 📋 Main API Endpoints

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/logout` - User logout

#### Posts
- `GET /api/posts` - Get all posts
- `GET /api/posts/{id}` - Get post by ID
- `POST /api/posts` - Create new post
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post

#### Users
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/profile` - Update profile
- `POST /api/users/{id}/follow` - Follow user
- `DELETE /api/users/{id}/unfollow` - Unfollow user

For complete API documentation with request/response examples, visit the **Swagger UI** interface.

## 🧪 Testing

### Backend Testing
```bash
cd server
mvn test
```

### Android Testing
```bash
cd android
./gradlew test
./gradlew connectedAndroidTest
```

## 📱 Screenshots

[Add your app screenshots here]

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## 👥 Authors

- **희건춘 배민웅 비희택 향원원**

## 🙏 Acknowledgments

- Material Design 3 for UI components
- Jetpack Compose community
- Spring Boot community
- All contributors and testers

## 📞 Contact

- Email: 3266303694@qq.com
- Project Link: [https://github.com/xingyangccd/zmxy-meet-app](https://github.com/xingyangccd/zmxy-meet-app)
- Swagger API Docs: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

**Note**: This is a student project developed for educational purposes.
