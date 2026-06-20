package com.renxo.user

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.renxo.user.navigation.AuthNavGraph
import com.renxo.user.ui.theme.RenxoUserAppTheme
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.ShackBarState
import com.renxo.user.utils.preferenceManager
import com.renxo.user.utils.setLanguageChanges
import com.renxo.user.viewmodels.ReAuthVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setLanguage(this)
        setContent {
            RenxoUserAppTheme {
                App()
            }
        }
    }

    private fun setLanguage(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            setLanguageChanges(context, preferenceManager.getLanguage()) {
                window?.decorView?.layoutDirection = it
            }
        }
    }

}


@Composable
fun App() {
    val navController = rememberNavController()
    val reAuthVM: ReAuthVM = hiltViewModel()
    val snackBar by remember { mutableStateOf(ShackBarState()) }
    CompositionLocalProvider(LocalSnackBar provides snackBar) {
        AuthNavGraph(navController, { reAuthVM.startTokenRefreshTimer() })
    }
}




