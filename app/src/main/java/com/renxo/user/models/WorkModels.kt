package com.renxo.user.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkType(
    val count: Int? = null,
    val priority: Int? = null,
    val type: String? = null
)

@Serializable
data class WorkSelectionModel(
    val group_type: String? = null,
    val compatible_task: String? = null,
    val id: String? = null,
    val priority: Int? = null,
    val from_area: String? = null,
    val next_hop: String? = null,
    val total_quantity: Int? = null,
    val status: String? = null,
    var cancelled: Boolean = false
)

@Serializable
data class TaskItem(
    var task_id: String? = null,
    var task_type: String? = null,
)

data class InitialTaskInfo(
    var compatible_task: String? = null,
    var default_area: String? = null,
    var group_type: String? = null,
    var next_hop: String? = null,
    var is_tasks_exists: Boolean = false,
)


