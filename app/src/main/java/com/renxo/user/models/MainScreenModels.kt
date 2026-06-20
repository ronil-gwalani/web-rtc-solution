package com.renxo.user.models

import kotlinx.serialization.Serializable


@Serializable
data class PickingPacks(
    val packsize: String? = null,
    val value: Int? = null,
)

@Serializable
data class InvSrcId(
    val location: String? = null,
    val lpn: String? = null,
    val sub_lpn: String? = null,
)


@Serializable
data class DataForPicking(
    val task_id: String? = null,
    val task_group_id: String? = null,
    val allocation_id: String? = null,
    val area: String? = null,
    val inv_detail_id: String? = null,
    val product_id: String? = null,
    val quantity: Int? = null,
    val travel_sequence: Int? = null,
    val inv_src_id: InvSrcId? = null,
    val pack: List<PickingPacks>? = listOf(),
)





