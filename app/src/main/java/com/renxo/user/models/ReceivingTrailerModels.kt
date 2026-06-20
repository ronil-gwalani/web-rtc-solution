package com.renxo.user.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
data class ReceivingTrailerOutput(
    val attributes: List<ReceivingTrailerWorkFlow?>? = null,
)

@Serializable
data class ReceivingTrailerWorkFlow(
    val attribute_name: String? = null,
    val data_type: String? = null,
    val default_value: String? = null,
    val list_of_values: List<String?>? = null,
    val mandatory: Boolean? = null,
)

