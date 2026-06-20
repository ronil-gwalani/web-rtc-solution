package com.renxo.user.models

import androidx.annotation.StringRes
import com.renxo.user.navigation.NavRouts
import com.renxo.user.utils.DefaultSerializer
import com.renxo.user.utils.ListSerializer
import kotlinx.serialization.Serializable


@Serializable
data class MainMenu(
    val screen_id: String? = null,
    val screen_name: String? = null,
    val screen_type: String? = null,
    val type: String? = null,
    val custom_data: CustomData? = null,
)


@Serializable
data class CustomData(
    val fields: List<Field>? = null,
    val init_function: String? = null,
    val onClose: String? = null,
    val id: String? = null,

)

@Serializable
data class Field(
    val action: String? = null,
    val attribute_name: String? = null,
    val type: String? = null,
    val mandatory: Boolean = false,
    val editable: Boolean = true,
    @Serializable(with = ListSerializer::class)
    val list_of_values: List<String>? = null,
    @Serializable(with = DefaultSerializer::class)
    val value: String? = null,
)


data class MenuOptionModel(@StringRes val title: Int, val routs: NavRouts)





