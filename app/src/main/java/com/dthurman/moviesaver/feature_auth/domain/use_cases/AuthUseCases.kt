package com.dthurman.moviesaver.feature_auth.domain.use_cases

data class AuthUseCases(
    val signInWithGoogle: SignInWithGoogleUseCase,
    val signOut: SignOutUseCase,
    val observeCurrentUser: ObserveCurrentUserUseCase
)

