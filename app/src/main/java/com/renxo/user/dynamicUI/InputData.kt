package com.renxo.user.dynamicUI

data class InputData(
    val type: InputType,
    val requirements: Requirements,
    val required: Boolean,
    val editable: Boolean,
    val placeholder: String,
    val action: String? = null,
    var value: Any? = null,
    val toShow: Boolean = true,
    val hint: String? = null,
    val showLiableOutside: Boolean = false,
    val identification: Boolean? = null,
)