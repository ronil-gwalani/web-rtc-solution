package com.renxo.user.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageHandling(
    val title: String? = null,
    val description: String? = null
)
