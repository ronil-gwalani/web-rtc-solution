package com.renxo.user.dynamicUI

sealed class Requirements {
    data class EditTextRequirements(
        val error: String,

        ) : Requirements()

    data class EditTextNumberRequirements(
        val error: String,
        val min: String? = null,
        val max: String? = null,
//        val defaultValue: String? = null,
    ) : Requirements()

    data object ButtonRequirements : Requirements()

    data class CheckBoxRequirements(
        val error: String,
        val defaultValue: Boolean = false
    ) : Requirements()


    data class DateRequirements(

        val error: String,
        val mindate: String? = null,
        val maxdate: String? = null,
//        val defaultValue: String? = null,
    ) : Requirements()

    data class DateTimeRequirements(
        val error: String,
        val mindate: String? = null,
        val maxdate: String? = null,
//        val defaultValue: String? = null,
    ) : Requirements()


    data class DropDownRequirements(

        val error: String,
        val options: List<String?>?,
        val defaultValue: String? = null,

        ) : Requirements()

    data class MultiselectRequirements(

        val error: String,
        val options: List<String?>?,
        val defaultValue: List<String>? = null,

        ) : Requirements()

    data class RadioButtonRequirements(
        val error: String, val options: List<String>,
        val defaultValue: String? = null,
    ) : Requirements()

    data class ToggleButtonRequirements(
        val error: String,
        val defaultValue: Boolean = false,
    ) : Requirements()

    data class MultipleScanningRequirements(
        val error: String = "Some Error Occur",
        var requiredCount: Int = 0,
        val options: List<String> = emptyList(),
        val defaultValue: List<String> = emptyList(),
    ) : Requirements()

}