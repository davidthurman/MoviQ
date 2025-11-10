# MoviQ
<img width="1231" height="679" alt="MoviQ cover image" src="https://github.com/user-attachments/assets/8ba41ce2-9383-409c-8416-3fcd75f58aae" />

## About
I've always found that there aren't a lot of **great** examples of modern Android apps for new developers to refer to. And of the ones out there, few are built with best practices and made to scale, like you would find in real major tech companies.

So with that in mind, I wanted to build an app using the latest Android tech stack in order to refresh myself and to allow other devs to reference. 

designed MoviQ as an open source Android App using modern best practices for other Android Devs (and my future self :) ) to reference.

## What is it?
MoviQ is a smart way to track and discover new movies. 

It allows you to:
- Search for movies
- Add movies to your watchlist
- Mark movies as Seen and rate them
- Take your movie history (and ratings) and use AI to discover new movies you would like, tailored just for you

## Tech Stack
UI:
- Compose
- Material3

Data:
- Room (local)
- Firestore (server)

AI:
- Vertex (Firebase)

DI:
- Hilt

Networking:
- Retrofit
- Coil

Auth:
- Firebase (Google Auth)

Observability:
- Firebase Crashylitics
- Firebase Analytics

Testing:
- JUnit
- Truth
- Espresso

Other Android Libraries:
- WorkManager
- Billing (Google)
- Preferences



## Architecture
The overall architecture based on Clean Architecture priniciples and Google's recommended architecture ([link](https://developer.android.com/topic/architecture))
<img width="1725" height="1005" alt="image" src="https://github.com/user-attachments/assets/010f6840-dfd3-480e-88f5-82c1a5506c5d" />


The presentation work is based on the MVI pattern



## Libraries

