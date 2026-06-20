package com.renxo.user.screens

import android.os.Process
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.renxo.user.R
import com.renxo.user.models.WorkFlowQuestions
import com.renxo.user.navigation.IdleDetectionApp
import com.renxo.user.navigation.MainNavGraph
import com.renxo.user.navigation.NavRouts
import com.renxo.user.navigation.Navigation
import com.renxo.user.navigation.OptionMenuRouts
import com.renxo.user.navigation.isCurrentDestination
import com.renxo.user.navigation.navigateTo
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.GetAlertDialogue
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.ShowSnackBar
import com.renxo.user.utils.WebSocketInterceptor.WebSocketLogs
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.utils.getWindowInfo
import com.renxo.user.utils.json
import com.renxo.user.utils.showToast
import com.renxo.user.viewmodels.HomeVM
import com.renxo.user.viewmodels.WorkFlowVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    openSettingsPage: (List<String>) -> Unit,
    onIdle: () -> Unit,
) {
//    val snack by remember { mutableStateOf(SnackBarsss()) }
    val context = LocalContext.current
    val viewModel = LocalHomeViewModelProvider.current
    val windowInfo = remember { getWindowInfo(context) }
    val scope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }
    val snackBarState = LocalSnackBar.current
    val navController = rememberNavController()

    val drawerState =
        rememberDrawerState(initialValue = DrawerValue.Closed, confirmStateChange = {
            viewModel.unableGesture = it == DrawerValue.Open
            true
        })
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = remember {

        screenWidth * if (windowInfo.isPortrait) {
            if (windowInfo.isTablet) {
                0.4f
            } else {
                0.6f
            }
        } else {
            0.3f
        }
    }



    BackHandler {
        if (backPressedOnce) {
            Process.killProcess(Process.myPid())
        } else {
            backPressedOnce = true
            context.showToast(R.string.press_back_again_to_exit)
            scope.launch {
                delay(2000) // Reset after 2 seconds
                backPressedOnce = false
            }
        }
    }

    GetOneTimeBlock {

        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is Navigation -> {
                    navController.navigateTo(
                        event.routs, event.finish, event.finishAll, event.singleTop
                    )
                }


                is HomeVM.AddMenuItem -> {
                    viewModel.addMenuItem(
                        windowInfo, event.screenType, event.routs, event.title
                    )
                }

                is HomeVM.ManageResponse -> {
                    viewModel.handelResponse(context, event.result, snackBarState)
                }

                is ShowSnackBar -> {
                    snackBarState.showSnackBar(context.getString(event.message))

                }
            }

        }
    }

    if (viewModel.showConfirmationDialogueForWork.show) {
        WarningDialogueForNextHop(
            viewModel.showConfirmationDialogueForWork.message,
            onButton1Click = {
                viewModel.showConfirmationDialogueForWork =
                    viewModel.showConfirmationDialogueForWork.copy(show = false)
            }, onButton2Click = {
                viewModel.acceptWarning()
            })
    }


    if (viewModel.findWorkWarning.show) {
        FindWorkWarningDialogue(
            viewModel.findWorkWarning.title ?: stringResource(R.string.warning),
            viewModel.findWorkWarning.description ?: stringResource(R.string.some_error_occurred),
//            onButton1Click = {
//                viewModel.showFindWorkWarning(false)
//            },
            onButton2Click = {
                viewModel.showFindWorkWarning(false)
                navController.navigateTo(NavRouts.AcceptedWorkScreen)
            }
        )
    }

    if (viewModel.messageDialog.show) {
        MessageDialog(
            title = viewModel.messageDialog.title ?: "",
            description = viewModel.messageDialog.description ?: "",
            onOkayClick = {
                viewModel.showMessageDialogue(false) // Dismiss the dialog
            })
    }

    if (viewModel.showRenameLpnDialog) {
        RenameLpn(onDismissRequest = {
            viewModel.showRenameLpnDialog = false
        }) { old, new ->
            viewModel.renameLpn(old, new)
        }
    }

    if (viewModel.showLanguageChangeDialogue.show) {
        LanguageChangeWarning(openSettingsPage)
    }
    if (viewModel.showOwnerShipChangeDialog) {
        ChangeOwnerShip(onDismissRequest = {
            viewModel.showOwnerShipChangeDialog = false
        }) { lpn, old, new ->
            viewModel.changeOwnership(lpn, old, new)
        }
    }

    WebSocketLogs {
        viewModel.showProfileMenu = false
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = viewModel.unableGesture, // Allow gestures to close
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth)
            ) {
                DrawerContent(viewModel.navigationItems) {
                    scope.launch {
                        drawerState.close()
                        navController.popBackStack(NavRouts.MainScreen, inclusive = false)
                            .let { _ ->
                                viewModel.clearResponse()
                                viewModel.selectedAreaType.value = null
                                if (it is NavRouts.PrePackingScreen) {
                                    viewModel.startPacking()
                                } else {
                                    navController.navigateTo(it, false)
                                }
                            }
                    }
                }
            }
        },
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    snackBarState.hostState,
                    snackbar = {
                        Snackbar(
                            it,
                            containerColor = Color.Red,
                            contentColor = AppColors.whiteColor
                        )
                    }
                )
            },
            modifier = Modifier
                .fillMaxSize()
           , topBar = {
                AppTopBar(navController, openSettingsPage)
            },
        ) { padding ->
            IdleDetectionApp(
                idleTimeoutMillis = AppConstants.IDEAL_TIME, onIdle = onIdle
            ) {
                viewModel.workFlowsList.forEach {
                    WorkFlowQuestions(it, finish = {
                        viewModel.workFlowsList.remove(it)
                    })
                }

                MainNavGraph(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),

                    openMenu = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    showWorkFlow = { questions, extraParams, transaction ->
                        val value = json.decodeFromString<WorkFlowQuestions>(questions)
                        val params =
                            json.decodeFromString<HashMap<String, String?>>(extraParams)
                        viewModel.workFlowsList.add(
                            WorkFlowVM(value, transaction, params)
                        )
                    })



                if (viewModel.showCalculator) {
                    CalculatorScreen {
                        viewModel.showCalculator = false
                    }
                }
            }
        }
    }


}

@Composable
private fun LanguageChangeWarning(openSettingsPage: (List<String>) -> Unit) {
    val homeVM = LocalHomeViewModelProvider.current
    GetAlertDialogue {
        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.language),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp),
            )

            Text(
                text = stringResource(R.string.change_language),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.change_language_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        openSettingsPage(homeVM.equipmentList.toList())
                        homeVM.showLanguageChangeDialogue = DefaultLangDialogue()
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6E40)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.change),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center

                    )
                }
                Button(
                    onClick = {
                        homeVM.showLanguageChangeDialogue = DefaultLangDialogue()
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B894)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FindWorkWarningDialogue(
    title: String,
    description: String,
//    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit
) {
    GetAlertDialogue {
        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
//                Button(
//                    onClick = {
//                        onButton1Click()
//                    },
//                    modifier = Modifier
//                        .padding(horizontal = 4.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFFF6E40)
//                    ),
//                    shape = RoundedCornerShape(24.dp)
//                ) {
//                    Text(
//                        text = stringResource(id = R.string.add_more),
//                        style = MaterialTheme.typography.bodyMedium.copy(
//                            fontWeight = FontWeight.Bold
//                        ),
//                        textAlign = TextAlign.Center
//
//                    )
//                }
                Button(
                    onClick = {
                        onButton2Click()

                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B894)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.show_tasks),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
private inline fun DrawerContent(
    navigationItems: List<HomeVM.DrawerNavItem>,
    crossinline navigate: (NavRouts) -> Unit,
) {
    Column {
        DrawerHeader()
        navigationItems.forEach { navItem ->
            NavigationDrawerItem(
                label = { Text(navItem.titleResId) },
                selected = false, onClick = {
                    navigate(navItem.route)
                })
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    navController: NavHostController,
    openSettingsPage: (List<String>) -> Unit,
) {
    val viewModel = LocalHomeViewModelProvider.current

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.whiteColor),
        navigationIcon = {
            HomeIcon(navController)
        },
        title = {
            ExposedDropdownMenuBox(
                modifier = Modifier.width(IntrinsicSize.Min),
                expanded = viewModel.areaDropDownExpanded,
                onExpandedChange = {
                    viewModel.areaDropDownExpanded = !viewModel.areaDropDownExpanded
                }) {

                TextField(
                    shape = RoundedCornerShape(15.dp),
                    colors = getTextFiledColors().copy(focusedContainerColor = AppColors.whiteColor),
                    value = viewModel.selectedArea,
                    placeholder = {
                        Text(text = stringResource(R.string.select_area))
                    },
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    maxLines = 1,
                    trailingIcon = {
                        Icon(
                            imageVector = if (viewModel.areaDropDownExpanded) Icons.Filled.KeyboardArrowUp
                            else Icons.Filled.KeyboardArrowDown, contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .width(IntrinsicSize.Min) // This makes the TextField take only needed width
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .padding(end = 8.dp)
                )


                ExposedDropdownMenu(
                    expanded = viewModel.areaDropDownExpanded,
                    containerColor = AppColors.whiteColor,
                    onDismissRequest = { viewModel.areaDropDownExpanded = false }) {

                    viewModel.workAreaList.forEach { location ->
                        DropdownMenuItem(text = { Text(text = location) }, onClick = {
                            viewModel.selectedArea = location
                            viewModel.areaDropDownExpanded = false

                        }
                        )
                    }
                }
            }


        },
        actions = {
            Box {
                IconButton(
                    onClick = { viewModel.equipmentExpanded = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (viewModel.equipmentExpanded) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.12f
                            )
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tools),
                        tint = Color.Black,
                        contentDescription = stringResource(R.string.select_equipment),
                        modifier = Modifier.size(24.dp),
                    )
                }

                DropdownMenu(
                    expanded = viewModel.equipmentExpanded,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.background(AppColors.whiteColor),
                    onDismissRequest = { viewModel.equipmentExpanded = false }) {
                    viewModel.equipmentList.forEach { equipment ->
                        DropdownMenuItem(text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = equipment)
                                if (equipment == viewModel.selectedEquipment) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }, onClick = {
                            viewModel.equipmentExpanded = false
                            viewModel.selectedEquipment = equipment
                        })
                    }
                }
            }

            Box(Modifier.background(Color.Transparent)) {

                IconButton(
                    onClick = { viewModel.showProfileMenu = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (viewModel.showProfileMenu) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.12f
                            )
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        modifier = Modifier.size(28.dp),
                        tint = if (viewModel.showProfileMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                ProfileMenu(
                    expanded = viewModel.showProfileMenu,
                    onDismissRequest = { viewModel.showProfileMenu = false },
                    viewModel.menuOptionModel.filter {
                        if (navController.isCurrentDestination(NavRouts.MainScreen)) {
                            true
                        } else {
                            it.routs != OptionMenuRouts.Settings
                        }
                    },
                    onMenuItemClicked = { tool ->
                        viewModel.showProfileMenu = false
                        when (tool.routs) {
                            OptionMenuRouts.Calculator -> {
                                viewModel.showCalculator = true
                            }

                            OptionMenuRouts.RenameLpn -> {
                                viewModel.showRenameLpnDialog = true
                            }

                            OptionMenuRouts.ChangeOwnerShip -> {
                                viewModel.showOwnerShipChangeDialog = true
                            }

                            OptionMenuRouts.CancelWork -> {
                                if (!navController.isCurrentDestination(NavRouts.AcceptedWorkScreen)) {
                                    navController.navigateTo(NavRouts.AcceptedWorkScreen)
                                }
                            }

                            OptionMenuRouts.Settings -> {
                                openSettingsPage(viewModel.equipmentList.toList())
                            }

                            OptionMenuRouts.MarkDamage -> {

                            }

                            else -> {

                            }
                        }
                    },
                    onLogoutClick = {
                        viewModel.showProfileMenu = false
                        viewModel.clearWebsocket()

                    }

                )

            }
        })
}

@Composable
private fun HomeIcon(navController: NavHostController) {
    val homeVM = LocalHomeViewModelProvider.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val show by remember(navBackStackEntry) {
        mutableStateOf(
            !navController.isCurrentDestination(
                NavRouts.MainScreen
            )
        )
    }
    if (show) {
        IconButton(
            modifier = Modifier
                .clip(shape = CircleShape)
                .background(Color.Transparent),
            onClick = {
                if (navController.isCurrentDestination(NavRouts.CustomScreen(""))) {
                    homeVM.onCloseFromCustomScreen()
                } else {
                    navController.popBackStack(NavRouts.MainScreen, inclusive = false)
                }
            }) {
            Icon(
                tint = AppColors.accentColor,
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.home)
            )
        }
    }

}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .background(AppColors.backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.menu_text),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold, fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(
            thickness = 2.dp,
            // modifier = Modifier.padding(top = 8.dp)
        )
    }
}






