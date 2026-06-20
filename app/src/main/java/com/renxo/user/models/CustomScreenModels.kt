package com.renxo.user.models

import com.renxo.user.utils.DefaultSerializer
import com.renxo.user.utils.ListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Fields(
    val attribute_name: String? = null,
    @Serializable(with = DefaultSerializer::class)
    val value: String? = null,
    @Serializable(with = ListSerializer::class)
    val list_of_values: List<String>? = null,
    val type: String? = null,
    val mandatory: Boolean? = false,
    val editable: Boolean? = false,
    val action: String? = null,
)

@Serializable
data class CustomScreenAttributes(
    val fields: List<Fields>? = null,
    val init_function: String? = null,
    val onClose: String? = null
)

@Serializable
data class CustomFieldResult(
    val attribute_name: String? = null,
    @Serializable(with = DefaultSerializer::class)
    val value: String? = null,
    @Serializable(with = DefaultSerializer::class)
    val message: String? = null,
    val status: String? = null

)

@Serializable
data class CustomField(
    val attribute_name: String,
    val editable: Boolean,
    val mandatory: Boolean,
    val value: String? = null
)




