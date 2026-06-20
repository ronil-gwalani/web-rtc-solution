package com.renxo.user.models

import kotlinx.serialization.Serializable

@Serializable
data class LpnItemsModel(
    val lpn: String? = null,
    val id: String? = null,
    val work_area: String? = null,
    var location: String? = null,
    var checked: Boolean = false
)