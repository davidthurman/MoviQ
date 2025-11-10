# MoviQ

<img width="1231" height="679" alt="MoviQ cover image" src="https://github.com/user-attachments/assets/8ba41ce2-9383-409c-8416-3fcd75f58aae" />

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-23-blue.svg)](https://developer.android.com/about/versions/marshmallow)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)](https://developer.android.com/)

## 📖 About

I've always found that there aren't a lot of **great** examples of modern Android apps for new developers to refer to. And of the ones out there, few are built with best practices and designed to scale, like you would find in real major tech companies.

So with that in mind, I wanted to build an app using the latest Android tech stack in order to refresh myself and to allow other devs to reference. MoviQ is designed as an open source Android app using modern best practices for other Android developers (and my future self 😊) to reference.

## 🎬 What is MoviQ?

MoviQ is a smart way to track and discover new movies using AI-powered recommendations.

### Core Features

- **🔍 Discover Movies** - Search and browse popular movies from The Movie Database (TMDB)
- **📋 Watchlist Management** - Save movies you want to watch later
- **✅ Track Watched Movies** - Mark movies as seen and rate them (1-5 stars)
- **⭐ Favorites** - Mark your favorite movies for quick access
- **🤖 AI-Powered Recommendations** - Get personalized movie suggestions based on your watch history and ratings using Firebase Vertex AI (Gemini 2.0)
- **☁️ Cloud Sync** - Your movie data syncs across devices via Firebase Firestore
- **🔐 Google Authentication** - Secure sign-in with Google
- **💳 In-App Purchases** - Purchase AI recommendation credits via Google Play Billing
- **🌙 Dark Mode** - Full dark mode support with Material 3 theming
- **📊 Analytics & Crash Reporting** - Firebase Analytics and Crashlytics integration

## 🏗️ Architecture

The app follows **Clean Architecture** principles and Google's recommended architecture guidelines, organized into clear layers:

<img width="1725" height="1005" alt="Clean Architecture Diagram" src="https://github.com/user-attachments/assets/010f6840-dfd3-480e-88f5-82c1a5506c5d" />

### Layer Breakdown

#### **Presentation Layer** (UI + ViewModels)
- Built with **Jetpack Compose** and **Material 3**
- Follows **MVI (Model-View-Intent)** pattern
- ViewModels manage UI state using `StateFlow`
- Unidirectional data flow with sealed classes for events
- Hilt for dependency injection

#### **Domain Layer** (Business Logic)
- Contains use cases that encapsulate business logic
- Repository interfaces define data contracts
- Domain models (entities) are framework-agnostic
- Single Responsibility Principle - each use case does one thing

#### **Data Layer** (Data Sources)
- Repository implementations coordinate between data sources
- **Local**: Room database for offline-first architecture
- **Remote**: Firebase Firestore for cloud sync, Retrofit for TMDB API
- Data sync with conflict resolution based on timestamps
- Background sync using WorkManager

### Key Architectural Patterns

- **Clean Architecture** - Separation of concerns with clear dependencies
- **MVI Pattern** - Predictable state management in presentation layer
- **Repository Pattern** - Abstract data sources from business logic
- **Use Case Pattern** - Encapsulate business logic in reusable components
- **Dependency Injection** - Hilt for compile-time DI
- **Offline-First** - Room as source of truth, sync to cloud when available

## 🛠️ Tech Stack

### UI & Design
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Latest Material Design components and theming
- **Coil** - Image loading with memory/disk caching
- **Splash Screen API** - Native splash screen implementation with animation

### Data & Persistence
- **Room** - Local SQLite database with coroutines support
- **Firebase Firestore** - Cloud NoSQL database for sync
- **DataStore** - Type-safe data storage for preferences
- **WorkManager** - Background sync with constraints (network, battery)

### Networking
- **Retrofit** - Type-safe HTTP client
- **Gson** - JSON serialization/deserialization
- **OkHttp** - HTTP client (via Retrofit)

### AI & Machine Learning
- **Firebase Vertex AI** - Gemini 2.0 Flash for personalized recommendations
- Custom prompt engineering for movie suggestions

### Authentication & Billing
- **Firebase Authentication** - Google Sign-In integration
- **Google Play Billing** - In-app purchases for AI credits
- **Credential Manager** - Modern credential handling

### Dependency Injection
- **Hilt** - Compile-time DI built on Dagger
- **Hilt ViewModel** - ViewModel injection
- **Hilt Worker** - WorkManager integration

### Observability
- **Firebase Crashlytics** - Crash reporting and analysis
- **Firebase Analytics** - User behavior tracking
- Custom error logging abstraction

### Testing
- **JUnit 4** - Unit testing framework
- **Truth** - Fluent assertions library
- **Espresso** - UI testing framework
- **Compose UI Test** - Compose-specific UI testing
- **Hilt Testing** - DI for tests
- **Coroutines Test** - Testing coroutines and flows
- **Fake Repositories** - Test doubles for isolation
- **End-to-End Tests** - Complete user flow testing

### Build & Tooling
- **Gradle Version Catalogs** - Centralized dependency management
- **KSP** - Kotlin Symbol Processing (faster than kapt)
- **ProGuard** - Code shrinking and obfuscation for release builds
- **Kotlin 2.1.0** - Latest Kotlin with compiler improvements

## 📁 Project Structure

```
app/src/main/java/com/dthurman/moviesaver/
├── core/                           # Core shared functionality
│   ├── app/                        # App-level components (MainActivity, Navigation, Scaffold)
│   ├── data/                       # Core data implementations
│   │   ├── local/                  # Room database (MovieDao, MovieDatabase)
│   │   ├── remote/                 # Firestore data sources
│   │   ├── repository/             # Core repository implementations
│   │   └── sync/                   # Sync logic (MovieSyncService, SyncManager)
│   ├── domain/                     # Core domain models and contracts
│   │   ├── model/                  # Domain entities (Movie, User, SyncState)
│   │   ├── repository/             # Repository interfaces
│   │   └── use_cases/              # Core use cases
│   ├── observability/              # Analytics and error logging
│   └── util/                       # Utilities and test tags
│
├── di/                             # Dependency injection modules
│   ├── AppModule.kt                # Main DI module (300+ lines of providers)
│   └── ObservabilityModule.kt     # Analytics/logging DI
│
├── feature_movies/                 # Movie management feature
│   ├── data/
│   │   ├── remote/
│   │   │   ├── data_source/        # Firestore movie data source
│   │   │   ├── movie_information/  # TMDB API integration
│   │   │   └── themoviedb/         # TMDB DTOs and API interface
│   │   ├── repository/             # Movie repository implementation
│   │   └── sync/                   # Background sync worker
│   ├── domain/
│   │   ├── repository/             # Movie repository interface
│   │   └── use_cases/              # 8 movie-related use cases
│   └── presentation/
│       ├── discover/               # Search and discover movies
│       ├── detail/                 # Movie detail bottom sheet
│       ├── my_movies/              # User's movie lists (seen, watchlist)
│       └── shared/                 # Shared UI components
│
├── feature_ai_recs/                # AI recommendations feature
│   ├── data/
│   │   └── repository/             # AI service and repository
│   ├── domain/
│   │   ├── repository/             # AI repository interface
│   │   └── use_cases/              # 7 AI-related use cases
│   └── presentation/               # Recommendations screen and components
│
├── feature_auth/                   # Authentication feature
│   ├── data/
│   │   └── repository/             # Auth repository implementation
│   ├── domain/
│   │   ├── AuthRepository.kt       # Auth repository interface
│   │   └── use_cases/              # 4 auth use cases
│   └── presentation/               # Login screen and Google auth button
│
├── feature_billing/                # In-app billing feature
│   ├── data/
│   │   └── repository/             # Billing repository implementation
│   ├── domain/
│   │   ├── BillingManager.kt       # Google Play Billing wrapper
│   │   ├── repository/             # Billing repository interface
│   │   └── use_cases/              # 4 billing use cases
│
└── ui/                             # Shared UI components and theme
    ├── components/                 # Settings modal, top bar
    └── theme/                      # Material 3 theme, colors, typography
```

## 🔄 Data Flow & Sync Strategy

### Offline-First Architecture

1. **Room is the Single Source of Truth**
   - All UI reads from Room database
   - Immediate UI updates on user actions
   - Works fully offline

2. **Optimistic Updates with Sync States**
   ```kotlin
   enum class SyncState {
       SYNCED,           // In sync with cloud
       PENDING_CREATE,   // New item, needs upload
       PENDING_UPDATE,   // Modified item, needs upload
       PENDING_DELETE,   // Deleted item, needs cloud deletion
       FAILED            // Sync failed, will retry
   }
   ```

3. **Background Sync with WorkManager**
   - Periodic sync every 6 hours
   - Immediate sync on user actions
   - Constraints: network required, battery not low
   - Automatic retry with exponential backoff (up to 3 attempts)

4. **Conflict Resolution**
   - Timestamp-based: most recent change wins
   - `lastModified` field tracks changes
   - Prevents overwriting newer data

### Sync Flow Example

```
User marks movie as seen
    ↓
Update Room with PENDING_UPDATE
    ↓
UI updates immediately
    ↓
Trigger WorkManager sync
    ↓
Upload to Firestore
    ↓
Update Room with SYNCED
```

## 🤖 AI Recommendations

The AI recommendation system uses Firebase Vertex AI (Gemini 2.0 Flash) to generate personalized movie suggestions.

### How It Works

1. **Data Collection**
   - Analyzes user's seen movies and ratings
   - Considers watchlist preferences
   - Excludes previously rejected recommendations

2. **AI Prompt Engineering**
   - Sends structured data to Gemini 2.0
   - Requests 5 personalized recommendations
   - Includes reasoning for each suggestion

3. **Movie Matching**
   - Searches TMDB API for each AI suggestion
   - Validates year and title matches
   - Filters out duplicates and already-seen movies

4. **Credit System**
   - Each recommendation generation costs 1 credit
   - Users can purchase credits via Google Play Billing
   - Minimum 5 seen movies required for quality recommendations

## 🧪 Testing Strategy

### Unit Tests (`/test`)
- **Use Case Tests** - Business logic validation
- **Fake Repositories** - Test doubles for isolation
- **Coroutines Test** - Testing async operations
- **Truth Assertions** - Readable test assertions

### Instrumented Tests (`/androidTest`)
- **UI Tests** - Compose UI testing with semantics
- **End-to-End Tests** - Complete user flows
- **Hilt Testing** - DI in test environment
- **Espresso** - View interactions and assertions

### Test Coverage
- ✅ 30+ unit tests across all features
- ✅ 20+ instrumented tests for UI and E2E flows
- ✅ Custom `HiltTestRunner` for DI in tests
- ✅ `TestAppModule` for test-specific dependencies
- ✅ Mock data factories for consistent test data

### Example Test Structure

```kotlin
@HiltAndroidTest
@UninstallModules(AppModule::class, AppBindingModule::class)
class MoviesEndToEndTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun userCanSearchAndAddMovieToWatchlist() {
        // Test implementation
    }
}
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug | 2024.2.1 or newer
- JDK 21
- Android SDK 36
- Gradle 8.13+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/movie-saver.git
   cd movie-saver
   ```

2. **Get API Keys**
   - [TMDB API Key](https://www.themoviedb.org/settings/api) - For movie data
   - [Firebase Project](https://console.firebase.google.com/) - For auth, Firestore, Vertex AI

3. **Configure Firebase**
   - Download `google-services.json` from Firebase Console
   - Place it in `app/` directory

4. **Create `local.properties`**
   ```properties
   sdk.dir=/path/to/android/sdk
   MOVIES_API_KEY=your_tmdb_api_key_here
   STORE_PASSWORD=your_keystore_password
   KEY_PASSWORD=your_key_password
   ```

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Run button

### Firebase Setup

1. Enable **Authentication** → Google Sign-In
2. Enable **Firestore Database** → Start in production mode
3. Enable **Vertex AI** → Gemini API
4. Enable **Crashlytics** and **Analytics**
5. Add SHA-1 fingerprint for Google Sign-In:
   ```bash
   ./gradlew signingReport
   ```

## 📱 App Screenshots

*Coming soon - Add screenshots of key screens*

## 🔐 Security & Privacy

- **No hardcoded secrets** - API keys in `local.properties` (gitignored)
- **ProGuard** - Code obfuscation in release builds
- **Secure authentication** - Firebase Auth with Google Sign-In
- **Data encryption** - Firestore encryption at rest and in transit
- **Minimal permissions** - Only Internet and Network State

## 📦 Building for Release

```bash
./gradlew assembleRelease
```

The release APK/AAB will be signed with your release keystore and optimized with ProGuard.

## 🤝 Contributing

This is primarily a reference project, but contributions are welcome! Please feel free to:
- Report bugs
- Suggest features
- Submit pull requests
- Ask questions in Issues

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Acknowledgments

- [The Movie Database (TMDB)](https://www.themoviedb.org/) - Movie data and images
- [Firebase](https://firebase.google.com/) - Backend services
- [Google](https://developer.android.com/) - Android platform and libraries
- [Android Community](https://developer.android.com/community) - Inspiration and best practices

## 📧 Contact

David Thurman - [GitHub](https://github.com/yourusername)

---