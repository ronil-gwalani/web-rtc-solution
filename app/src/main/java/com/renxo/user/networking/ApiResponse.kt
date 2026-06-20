package com.renxo.user.networking

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String? = null,
    val url: String? = null,
    val default_language: String? = null,
    val expiry: Long? = null
)


@Serializable
data class AuthModel(
    val username: String,
    val password: String,
    val device_id: String,
    val warehouse: String,
    val language_id: String,
)

@Serializable
data object NotImportant : Any()


@Serializable
data class RefreshTokenRequest(val token: String)