package com.renxo.user.testing

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.InputType
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.models.InitializePackingWorkFlow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File


class TestingVM : ViewModel() {
    data class Par(val id: String, val data: String)
    var file: Bitmap? by mutableStateOf(null)

    var refresh by mutableStateOf(false)
    val l = listOf(
        Par(id = "1", "item1"),
        Par(id = "2", "item2"),
        Par(id = "3", "item3"),
        Par(id = "4", "item4"),
        Par(id = "5", "item5")
    )

    val flow = MutableSharedFlow<Int>(
        replay = 2, // keeps last 3 values in buffer
//        extraBufferCapacity = 1
    )

    init {
//        startEmiting()
//        startListning()
//        checkInLineFunctions()
    }

    private fun checkInLineFunctions() {
        launchScope1 {
            repeat(100) {
                delay(2000)
                Log.e("launchScope1", ": $it")
            }
        }
        launchScope2 {
            repeat(100) {
                delay(2000)
                Log.e("launchScope1", ": $it")
            }
        }

    }

    private fun startListning() {
        viewModelScope.launch {
            delay(5000)
            flow.collect {
                Log.e("startListning", ": $it")

            }
        }

    }

    private fun startEmiting() {
        viewModelScope.launch {
            repeat(100) {
                flow.emit(it)
                delay(1000)
            }
        }


    }

    val uiElements = mutableStateListOf<InputData>()

    fun getRequiredWorkflow(): List<String>? {

//        uiElements.forEach {
//            if (it.required && (it.value == null || it.value.toString()
//                    .isEmpty() || it.value.toString() == "null")
//            ) {
//                return null
//            }
//        }

        val attributes = uiElements.map {
            InitializePackingWorkFlow(
                attribute_name = it.placeholder,
                value = if (it.value.toString() == "null") "" else
                    it.value.toString()
            )
            " ${it.placeholder}->> ${
                if (it.value.toString() == "null") "" else
                    it.value.toString()
            }"
        }
        return attributes

    }

    init {
        uiElements.addAll(
            getList()
        )
        uiElements.addAll(
            getList()
        )
        uiElements.addAll(
            getList()
        )
        uiElements.addAll(
            getList()
        )
        uiElements.addAll(
            getList()
        )
        uiElements.addAll(
            getList()
        )
        uiElements.addAll(
            getList()
        )
        uiElements.addAll(
            getList()
        )
    }

    fun updateInputValue(index: Int, value: Any?) {
        uiElements[index] = uiElements[index].copy(value = value)
    }

    private fun getList(): List<InputData> {
        return listOf(
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 111",
                action = "Action 1",
                value = "Action 1"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 22222",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 222",
                action = "Action 2",
                value = "Action 9652"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 3333",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 333",
                value = "hello moto"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter 44",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 4444",
                action = "Action 1",
                value = "Action 1poiuhg"
            ),
            InputData(
                InputType.EDIT_TEXT,
                requirements = Requirements.EditTextNumberRequirements(
                    "Enter Name 5555555",
                ),
                required = true,
                editable = true,
                placeholder = "text Box 5555",
                action = "Action 5",
                value = "Action 1poiuyt"
            ),
        )
    }


}


private inline fun ViewModel.launchScope1(
    crossinline block: suspend CoroutineScope.() -> Unit
): Job {
    return viewModelScope.launch {
        block()
    }
}

fun ViewModel.launchScope2(
    block: suspend CoroutineScope.() -> Unit
): Job {
    return viewModelScope.launch {
        block()
    }
}



