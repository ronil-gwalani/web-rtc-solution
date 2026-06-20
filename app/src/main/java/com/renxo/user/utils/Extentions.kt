package com.renxo.user.utils

    import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.ButtonColors
import androidx.core.text.layoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.ui.theme.AppColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Locale


fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//    toastIconSuccess(message)
}

inline fun getMap(
    crossinline init: HashMap<String, Any?>.() -> Unit = {}
): HashMap<String, Any?> {
    return HashMap<String, Any?>().apply {
        init()
    }
}

inline fun ViewModel.launchScope(
    crossinline block: suspend CoroutineScope.() -> Unit
): Job {
    return viewModelScope.launch {
        block()
    }
}


fun Context.showToast(@StringRes message: Int) {
    showToast(getString(message))
}

//private fun Context.toastIconSuccess(message: String?) {
//    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//    val customView = inflater.inflate(R.layout.toast_icon_text, null)
//    customView.findViewById<TextView>(R.id.message).text = message
//    val toast = Toast(this)
//    toast.duration = Toast.LENGTH_LONG
//    toast.apply {
//        view = customView
//        show()
//    }
//}

fun basicButtonColors(): ButtonColors {
    return ButtonColors(
        containerColor = AppColors.accentColor,
        contentColor = AppColors.whiteColor,
        disabledContentColor = AppColors.accentColor,
        disabledContainerColor = AppColors.whiteColor
    )

}


val preferenceManager = MyApplication.preferenceManager


val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}


fun getTransactionId(): String {
    return "K${System.currentTimeMillis()}"
}

fun setLanguageChanges(context: Context, languageCode: String, success: (Int) -> Unit = {}) {
    val locale = if (languageCode.contains("-")) {
        val split = languageCode.split("-")
        Locale(split[0], split[1])
    } else {
        Locale(languageCode)
    }
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale) // Ensure RTL layout
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    CoroutineScope(Dispatchers.IO).launch {
        preferenceManager.saveLanguage(languageCode)
        withContext(Dispatchers.Main) {
            success(locale.layoutDirection)
        }
    }
}



