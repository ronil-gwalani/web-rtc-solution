package com.renxo.user.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.renxo.user.screens.AuthenticateScreen
import com.renxo.user.screens.ConnectToServer
import com.renxo.user.screens.HomeScreen
import com.renxo.user.screens.SettingScreen
import com.renxo.user.screens.SplashScreen
import com.renxo.user.testing.Testing
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.MyAnimation
import com.renxo.user.utils.preferenceManager
import com.renxo.user.viewmodels.AuthGraphPageVM
import com.renxo.user.viewmodels.ReAuthVM
import com.renxo.user.viewmodels.SettingsVM
import kotlinx.coroutines.launch


@Composable
fun AuthNavGraph(
    navController: NavHostController,
    startTokenRefreshTimer: ()->Unit,
    viewModel: AuthGraphPageVM = viewModel(),
) {
    val snackBar = LocalSnackBar.current
    val scope = rememberCoroutineScope()
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is AuthGraphPageVM.StartTokenRefreshTimer -> {
                    startTokenRefreshTimer()
                }

                is AuthGraphPageVM.WebSocketConnectionMessage -> {
                    snackBar.showSnackBar(event.message)
                }

                is Navigation -> {
                    if (!navController.isCurrentDestination(event.routs)) {
                        navController.navigateTo(
                            event.routs,
                            event.finish,
                            event.singleTop,
                            event.finishAll
                        )
                    }

                }
            }
        }

    }

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // This ensures proper padding when the keyboard is visible
            .navigationBarsPadding() // Respect bottom nav bar
            .statusBarsPadding(),

        navController = navController,
        startDestination = AuthRoutes.Splash,
//        startDestination = AuthRoutes.Testing,
        enterTransition = { MyAnimation.myEnterAnimation() },
        exitTransition = { MyAnimation.myExitAnimation() },
        popEnterTransition = { MyAnimation.myEnterAnimation() },
        popExitTransition = { MyAnimation.myExitAnimation() },
    ) {
        composable<AuthRoutes.Testing> {
            Testing()
        }
        composable<AuthRoutes.Splash> {
            SplashScreen {
                preferenceManager.getAuthUrl()?.let {
                    navController.navigateTo(AuthRoutes.AuthenticatePage, true)
                } ?: navController.navigateTo(AuthRoutes.ConnectToServer, true)
            }
        }
        composable<AuthRoutes.ConnectToServer> {
            ConnectToServer {
                navController.navigateTo(AuthRoutes.AuthenticatePage, true)
            }
        }
        composable<AuthRoutes.SettingScreen> {
            val list = remember { it.toRoute<AuthRoutes.SettingScreen>().list }
            val settingsVm: SettingsVM = viewModel<SettingsVM>().apply {
                updateEquipmentList(list)
            }
            SettingScreen(settingsVm, restartHome = {
//                navController.popBackStack(AuthRoutes.HomeScreen, inclusive = true)
                navController.navigateTo(AuthRoutes.HomeScreen, finishAll = true)
            }, finish = {
                navController.finish()
            })
        }
        composable<AuthRoutes.AuthenticatePage> {
            AuthenticateScreen(openConfigurePage = {
                scope.launch {
                    preferenceManager.clearAllPreferences()
                    navController.navigateTo(AuthRoutes.ConnectToServer, finishAll = true)
                }
            }, languageChanged = {
                navController.navigateTo(AuthRoutes.AuthenticatePage, finish = true)
            }) {
                scope.launch {
                    preferenceManager.getMainUrl().let { mainUrl ->
                        preferenceManager.getAuthToken().let { token ->
                            viewModel.connect(mainUrl, token)
                        }
                    }
                }
            }
        }

        composable<AuthRoutes.HomeScreen> {
            CompositionLocalProvider(LocalHomeViewModelProvider provides viewModel()) {
                HomeScreen(openSettingsPage = {
                    navController.navigateTo(AuthRoutes.SettingScreen(it))
                }) {
                    viewModel.clearWebsocket()
//                    navController.navigateTo(AuthRoutes.AuthenticatePage, finishAll = true)
                }
            }
        }


    }
}
