package com.dthurman.moviesaver.domain.model

data class User(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val credits: Int = 10
)

