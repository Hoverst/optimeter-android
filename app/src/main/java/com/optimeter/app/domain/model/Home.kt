package com.optimeter.app.domain.model

data class Home(
    val id: String = "",
    val userId: String = "", // Links this home to the authenticated user
    val name: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
