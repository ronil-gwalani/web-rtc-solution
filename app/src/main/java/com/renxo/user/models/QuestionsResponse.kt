package com.renxo.user.models

import kotlinx.serialization.Serializable


@Serializable
data class WorkFlowQuestions(
    val questions: List<Question>? = null,
    val stop_operation_on_failure: Boolean? = false,
    val type: String? = null,
    val workflow: String? = null,
)

@Serializable
data class Question(
    val question: String? = null, // text Question
    val data_type: String? = null, //[boolean(Radio Button),String(Edit Text),int,float,CheckBox]
    val possible_values: List<String>? = null,
    val min: Int? = null,
    val max: Int? = null,
)
