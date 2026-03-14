package com.optimeter.app.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String? = null,
    val registeredAt: Long = System.currentTimeMillis()
)
