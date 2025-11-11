# MoviQ
![MoviQGithubBanner](https://github.com/user-attachments/assets/f0f8d20b-8e80-4f29-9968-b472ae38aa51)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-23-blue.svg)](https://developer.android.com/about/versions/marshmallow)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)](https://developer.android.com/)

> A production-ready Android app demonstrating modern architecture, AI integration, and best practices. Built as a reference for developers and to showcase professional Android development skills.

## 🎬 What is MoviQ?

| 🎬 Track Movies You Watch | ❤️ Rate & Mark Your Favorites | ✨ Get AI Recs on What to Watch | 🔎 Find New Films Tailored to You |
| - | - | - | - |
| <img width="1947" height="3632" alt="Screenshot_20251102_161458-portrait" src="https://github.com/user-attachments/assets/3fdd979a-e00c-48ce-b2c0-3d423073944c" /> | <img width="1947" height="3632" alt="Screenshot_20251102_165001-portrait" src="https://github.com/user-attachments/assets/fc70356e-f632-49e3-a844-3e8443fe00fe" /> | <img width="1947" height="3632" alt="Screenshot_20251102_162345-portrait" src="https://github.com/user-attachments/assets/771b07ee-9efa-4060-b673-3850f50849d2" /> | <img width="1947" height="3632" alt="Screenshot_20251102_162452-portrait" src="https://github.com/user-attachments/assets/cc4cf772-0ca8-4fd4-a335-f660bc107c2d" /> |


---

## 🏗️ Architecture

Built with **Clean Architecture** + **MVI Pattern** following [Google's recommended architecture](https://developer.android.com/topic/architecture).

```mermaid
graph LR
    subgraph Presentation["🎨 Presentation Layer"]
        UI[Jetpack Compose UI]
        VM[ViewModels + MVI]
    end
    
    subgraph Domain["⚙️ Domain Layer"]
        UC[Use Cases]
        RI[Repository Interfaces]
    end
    
    subgraph Data["💾 Data Layer"]
        REPO[Repository Implementations]
        LOCAL[(Room Database)]
        REMOTE[(Firestore)]
        API[TMDB API]
    end
    
    UI --> VM
    VM --> UC
    UC --> RI
    RI --> REPO
    REPO --> LOCAL
    REPO --> REMOTE
    REPO --> API
    
    style Presentation fill:#e3f2fd,stroke:#1565c0,stroke-width:3px,color:#000
    style Domain fill:#fff3e0,stroke:#ef6c00,stroke-width:3px,color:#000
    style Data fill:#f3e5f5,stroke:#6a1b9a,stroke-width:3px,color:#000
    style UI fill:#bbdefb,stroke:#1565c0,stroke-width:2px,color:#000
    style VM fill:#bbdefb,stroke:#1565c0,stroke-width:2px,color:#000
    style UC fill:#ffe0b2,stroke:#ef6c00,stroke-width:2px,color:#000
    style RI fill:#ffe0b2,stroke:#ef6c00,stroke-width:2px,color:#000
    style REPO fill:#e1bee7,stroke:#6a1b9a,stroke-width:2px,color:#000
    style LOCAL fill:#c5e1a5,stroke:#558b2f,stroke-width:2px,color:#000
    style REMOTE fill:#c5e1a5,stroke:#558b2f,stroke-width:2px,color:#000
    style API fill:#c5e1a5,stroke:#558b2f,stroke-width:2px,color:#000
```

| Layer | Responsibilities | Key Technologies |
|-------|------------------|------------------|
| **Presentation** | UI components, ViewModels, state management | Jetpack Compose, Material 3, MVI |
| **Domain** | Business logic, use cases, repository contracts | Kotlin, Coroutines, Flow |
| **Data** | Data sources, repositories, sync logic | Room, Firestore, Retrofit, WorkManager |

<details open>
<summary><b>📋 Key Architectural Patterns</b></summary>

- **Offline-First:** Room as single source of truth with cloud sync
- **Unidirectional Data Flow:** MVI pattern with sealed classes
- **Dependency Injection:** Hilt for compile-time DI
- **Background Sync:** WorkManager with conflict resolution
- **Repository Pattern:** Abstract data sources from business logic
- **Use Case Pattern:** Single responsibility for each business operation

</details>

---

## 🛠️ Tech Stack

<details open>
<summary><b>Core Android</b></summary>

- **Jetpack Compose** - UI
- **Material 3** - Google's Current Design System
- **Hilt** - Dependency injection
- **Navigation Compose** - Type-safe navigation
- **Lifecycle & ViewModel** - State management

- **Additional Features:** Cloud sync across devices • Google Sign-In • In-app purchases • Dark mode • Offline-first

</details>

<details>
<summary><b>Data & Networking</b></summary>

- **Room** - Local database (SQLite)
- **Retrofit** - REST API client
- **Coil** - Image loading & caching
- **DataStore** - Preferences storage
- **WorkManager** - Background tasks

</details>

<details>
<summary><b>Firebase Services</b></summary>

- **Authentication** - Google Sign-In
- **Firestore** - Cloud database
- **Vertex AI** - Gemini 2.0 for recommendations
- **Crashlytics** - Error tracking
- **Analytics** - User insights

</details>

<details>
<summary><b>Testing</b></summary>

- **JUnit 4** - Unit testing
- **Truth** - Assertions
- **Espresso** - UI testing
- **Compose Test** - Compose UI tests
- **Hilt Testing** - DI in tests

</details>

---

## 📁 Project Structure

Feature-based modularization with clear separation of concerns:

```
app/src/main/java/com/dthurman/moviesaver/
│
├── 🎯 core/                    # Shared functionality
│   ├── app/                    # MainActivity, Navigation, Scaffold
│   ├── data/                   # Room DB, sync logic
│   ├── domain/                 # Core models (Movie, User)
│   └── observability/          # Analytics & error logging
│
├── 🎬 feature_movies/          # Movie management
│   ├── data/                   # TMDB API, Firestore sync
│   ├── domain/                 # 8 use cases (search, rate, etc.)
│   └── presentation/           # Discover, Detail, My Movies screens
│
├── 🤖 feature_ai_recs/         # AI recommendations
│   ├── data/                   # Vertex AI integration
│   ├── domain/                 # 7 use cases
│   └── presentation/           # Recommendations screen
│
├── 🔐 feature_auth/            # Authentication
├── 💳 feature_billing/         # In-app purchases
└── 🎨 ui/                      # Shared components & theme
```

---

## 🔄 Offline-First Sync Strategy

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant Room
    participant WorkManager
    participant Firestore
    
    User->>UI: Mark movie as seen
    UI->>Room: Update with PENDING_UPDATE
    Room-->>UI: ✅ Instant feedback
    UI->>WorkManager: Trigger sync
    
    Note over WorkManager: Background sync
    WorkManager->>Room: Get pending changes
    Room-->>WorkManager: Movies to sync
    WorkManager->>Firestore: Upload changes
    Firestore-->>WorkManager: ✅ Success
    WorkManager->>Room: Update to SYNCED
```
**How it works:**
1. User action updates Room immediately (instant UI feedback)
2. Item marked with sync state
3. WorkManager syncs to Firestore in background
4. Conflict resolution uses timestamps (most recent wins)
5. Periodic sync every 6 hours + on-demand triggers

---

## 🤖 AI-Powered Recommendations

Uses **Firebase Vertex AI (Gemini 2.0 Flash)** to analyze your movie history and generate personalized suggestions.

```mermaid
flowchart LR
    A[User's Seen Movies] --> B[Vertex AI<br/>Gemini 2.0]
    A1[Ratings] --> B
    A2[Watchlist] --> B
    B --> C[AI Recommendations<br/>with Reasoning]
    C --> D[TMDB API<br/>Validation]
    D --> E{Match Found?}
    E -->|Yes| F[Add to<br/>Recommendations]
    E -->|No| G[Skip]
    F --> H[Display to User]
    
    style B fill:#4285f4,color:#fff
    style F fill:#34a853,color:#fff
    style G fill:#ea4335,color:#fff
```

**Process:**
1. Analyzes your seen movies + ratings
2. Sends structured prompt to Gemini 2.0
3. AI returns 5 movie suggestions with reasoning
4. Validates against TMDB API
5. Filters duplicates and already-seen movies

**Requirements:** Minimum 5 seen movies • 1 credit per generation

---

## 🧪 Testing

Comprehensive test coverage with **50+ tests** across unit, integration, and E2E.

| Test Type | Framework | Count | Coverage |
|-----------|-----------|-------|----------|
| **Unit Tests** | JUnit + Truth | 30+ | Use cases, ViewModels |
| **UI Tests** | Compose Test + Espresso | 15+ | Screen interactions |
| **E2E Tests** | Hilt Testing | 5+ | Complete user flows |

**Testing Strategy:**
- [x] Fake repositories for isolation
- [x] Hilt test modules for DI
- [x] Coroutines test for async operations
- [x] Custom `HiltTestRunner` for instrumented tests

<details>
<summary><b>Example Test</b></summary>

```kotlin
@HiltAndroidTest
@UninstallModules(AppModule::class)
class MoviesEndToEndTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun userCanSearchAndAddMovieToWatchlist() {
        // Complete user flow test
    }
}
```

</details>

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug | 2024.2.1+
- JDK 21
- TMDB API Key ([Get one here](https://www.themoviedb.org/settings/api))
- Firebase Project ([Create here](https://console.firebase.google.com/))

### Quick Setup

1. **Clone & Configure**
   ```bash
   git clone https://github.com/yourusername/movie-saver.git
   cd movie-saver
   ```

2. **Add `local.properties`**
   ```properties
   MOVIES_API_KEY=your_tmdb_api_key
   ```

3. **Add Firebase**
   - Download `google-services.json` from Firebase Console
   - Place in `app/` directory
   - Enable: Authentication (Google), Firestore, Vertex AI, Crashlytics

4. **Build**
   ```bash
   ./gradlew assembleDebug
   ```

<details>
<summary><b>Detailed Firebase Setup</b></summary>

- [ ] Create Firebase project
- [ ] Add Android app with package name: `com.dthurman.moviesaver`
- [ ] Enable services:
  - [ ] **Authentication** → Google Sign-In provider
  - [ ] **Firestore Database** → Production mode
  - [ ] **Vertex AI** → Enable Gemini API
  - [ ] **Crashlytics** & **Analytics**
- [ ] Add SHA-1 fingerprint:
  ```bash
  ./gradlew signingReport
  ```
- [ ] Download and add `google-services.json`

</details>

---

## 💡 Key Highlights

### For Developers
- [x] Production-ready architecture
- [x] 100% Kotlin & Compose
- [x] Comprehensive testing examples
- [x] Modern Android best practices
- [x] Real-world Firebase integration
- [x] Clean code with SOLID principles

### Technical Achievements
- [x] Offline-first with sync
- [x] AI integration (Vertex AI)
- [x] In-app billing implementation
- [x] Background processing
- [x] Type-safe navigation
- [x] Material 3 theming

## 🤝 Contributing

This is a reference project, but contributions are welcome! Feel free to open issues or submit PRs.


## 📝 License

MIT License - see [LICENSE](LICENSE) file for details.

**Built by David Thurman**

[GitHub](https://github.com/davidthurman) • [LinkedIn](https://linkedin.com/in/david-thurman)

⭐ Star this repo if you find it helpful!
