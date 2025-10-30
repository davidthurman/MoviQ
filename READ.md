# Movie Saver - Android App

An Android application for discovering, tracking, and getting AI-powered movie recommendations.

## Features

- 🎬 **Discover Movies**: Browse popular movies and search by title
- ✅ **Track Watched Movies**: Mark movies as seen and rate them
- ⭐ **Manage Favorites**: Save your favorite movies
- 📝 **Watchlist**: Keep track of movies you want to watch
- 🤖 **AI Recommendations**: Get personalized movie suggestions powered by Firebase Vertex AI
- 🔍 **Smart Search**: Ask AI for movie recommendations based on mood or preferences

## Tech Stack

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Hilt** - Dependency injection
- **Room** - Local database
- **Retrofit** - API communication
- **Firebase** - Backend services
  - Vertex AI (Gemini) - AI-powered recommendations
  - Authentication - User management (ready for implementation)
  - Firestore - Cloud database (ready for implementation)
- **MVVM Architecture** - Clean architecture pattern
- **Coroutines & Flow** - Asynchronous programming

## Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd movie-saver
```

### 2. Add API Keys

Create or edit `local.properties` in the project root and add:

```properties
MOVIES_API_KEY=your_tmdb_api_key_here
```

Get your TMDB API key from [The Movie Database](https://www.themoviedb.org/settings/api).

### 3. Firebase Setup

**Important**: To use AI Recommendations, you must set up Firebase.

Quick steps:
1. Create a Firebase project
2. Add your Android app with package name: `com.dthurman.moviesaver`
3. Download `google-services.json` and place it in the `app/` directory
4. Enable Vertex AI in Firebase Console
5. Build and run the app

### 4. Build and Run

Open the project in Android Studio and run the app on an emulator or physical device.

## Project Structure

```
app/src/main/java/com/dthurman/moviesaver/
├── data/               # Data layer
│   ├── ai/            # Firebase AI integration
│   ├── local/         # Room database
│   ├── remote/        # API services
│   └── di/            # Dependency injection modules
├── domain/            # Business logic layer
│   ├── model/         # Domain models
│   └── repository/    # Repository interfaces
└── ui/                # Presentation layer
    ├── components/    # Reusable UI components
    ├── features/      # Feature screens
    │   ├── feature_discover/
    │   ├── feature_seen/
    │   ├── feature_recommendations/
    │   └── feature_detail/
    └── theme/         # App theming
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the Apache License 2.0 - see the license headers in files for details.