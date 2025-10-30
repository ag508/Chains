# Chain - Decentralized Messaging Platform

Chain is a revolutionary decentralized messaging platform that eliminates the need for central servers by leveraging blockchain technology and peer-to-peer communication.

## Project Setup

This Android project is built with:

### Core Technologies
- **Kotlin** - Primary programming language
- **Android Jetpack Compose** - Modern UI toolkit
- **Hilt/Dagger** - Dependency injection
- **Room Database with SQLCipher** - Encrypted local storage
- **MVVM with Clean Architecture** - Application architecture

### Key Dependencies
- Room Database with SQLCipher encryption
- Hilt for dependency injection
- Jetpack Compose for UI
- Coroutines for asynchronous programming
- Navigation Compose for navigation
- Security Crypto for encrypted preferences

### Architecture

The project follows Clean Architecture principles with MVVM pattern:

```
presentation/     # UI layer (Activities, Composables, ViewModels)
├── base/        # Base classes and common UI components
├── chatlist/    # Chat list feature
├── navigation/  # Navigation setup
└── theme/       # UI theme and styling

domain/          # Business logic layer
├── model/       # Domain models
├── repository/  # Repository interfaces
└── usecase/     # Use cases

data/            # Data layer
├── local/       # Local database (Room + SQLCipher)
├── repository/  # Repository implementations
└── remote/      # Remote data sources (future)

di/              # Dependency injection modules
```

### Security Features
- SQLCipher database encryption
- Encrypted SharedPreferences for sensitive data
- Android Keystore integration
- Secure key management

### Build and Run

1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Run on device or emulator

### Testing

The project includes:
- Unit tests for business logic
- Integration tests for database operations
- UI tests for Compose screens (to be added)

## Next Steps

This completes the basic project setup and core infrastructure. The next tasks will implement:
- Cryptographic foundation and key management
- Local database schema and operations
- Blockchain integration
- Core messaging features

## Requirements Addressed

This setup addresses the following requirements:
- **8.1**: Cross-platform compatibility foundation (Android)
- **8.2**: Multi-device synchronization infrastructure