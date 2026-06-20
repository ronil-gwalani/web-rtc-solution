package com.renxo.user.models

import com.renxo.user.utils.DefaultSerializer
import com.renxo.user.utils.ListSerializer
import kotlinx.serialization.Serializable


@Serializable
data class TaskInfo(
    val task_group_id: String? = null,
    val task_id: String? = null
)

@Serializable
data class PickingAttribute(
    val attribute_name: String? = null,
    val data_type: String? = null,
    @Serializable(with = DefaultSerializer::class)
    val value: String? = null,
    @Serializable(with = ListSerializer::class)
    val list_of_values: List<String?>? = null,
    val mandatory: Boolean? = null,
    val prompted: Boolean? = null,
    val identification: Boolean? = null,
)
