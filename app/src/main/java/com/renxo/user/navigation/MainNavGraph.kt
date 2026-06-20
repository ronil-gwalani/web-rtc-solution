package com.renxo.user.navigation

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.renxo.user.models.CustomData
import com.renxo.user.models.DataForPicking
import com.renxo.user.models.PickingAttribute
import com.renxo.user.models.TaskInfo
import com.renxo.user.models.WorkType
import com.renxo.user.screens.AcceptedWorkScreen
import com.renxo.user.screens.CheckInTrailerScreen
import com.renxo.user.screens.CustomScreen
import com.renxo.user.screens.DepositScreen
import com.renxo.user.screens.DirectedWorkScreen
import com.renxo.user.screens.IBDScreen
import com.renxo.user.screens.MainScreen
import com.renxo.user.screens.PalletBuildingScreen
import com.renxo.user.screens.PickingScreen
import com.renxo.user.screens.ReceivingTrailerScreen
import com.renxo.user.screens.UnloadTrailerScreen
import com.renxo.user.screens.WorkSelectionScreen
import com.renxo.user.tabletscreens.PackingScreen
import com.renxo.user.tabletscreens.PrePackingScreen
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.utils.LockScreenOrientation
import com.renxo.user.utils.MyAnimation
import com.renxo.user.utils.json
import com.renxo.user.utils.showToast
import com.renxo.user.viewmodels.PackingVM
import com.renxo.user.viewmodels.PickingFixFields
import com.renxo.user.viewmodels.PickingVM
import com.renxo.user.viewmodels.PrePackingVM
import com.renxo.user.viewmodels.WorkSelectionVM


@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier,
    openMenu: () -> Unit,
    showWorkFlow: (String, String, String) -> Unit,
) {


    val lifecycleOwner = LocalLifecycleOwner.current


    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavRouts.MainScreen,
        enterTransition = { MyAnimation.myEnterAnimation() },
        exitTransition = { MyAnimation.myExitAnimation() },
        popEnterTransition = { MyAnimation.myEnterAnimation() },
        popExitTransition = { MyAnimation.myExitAnimation() },
    ) {


        composable<NavRouts.MainScreen> {
            MainScreen(openMenu = openMenu, startDeposit = {
                navController.navigateTo(NavRouts.DepositScreen)
            }, startPicking = { dataForPicking, workflow, taskListIds ->
                navController.navigateTo(
                    NavRouts.PickingScreen(
                        data = json.encodeToString(
                            dataForPicking
                        ),
                        idFields = json.encodeToString(workflow),
                        taskListIds = json.encodeToString(taskListIds),
                    )
                )
            })
        }
        composable<NavRouts.CheckInTrailerScreen> {
            CheckInTrailerScreen({ questions, extraParams, transaction ->
                showWorkFlow(
                    questions, extraParams, transaction
                )

            }, {
                navController.finish()
            })


        }


        composable<NavRouts.UnloadTrailerScreen> {

            UnloadTrailerScreen(onNavigateBack = {
                navController.navigateTo(NavRouts.MainScreen, true)
            })


        }

        composable<NavRouts.IBDScreen> {
            IBDScreen { params ->
                navController.navigateTo(
                    NavRouts.ReceivingTrailerScreen(params), finish = true, singleTop = false
                )
            }
        }

        composable<NavRouts.PackingScreen> { entry ->
            // Get params from route
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            val data = remember {
                entry.toRoute<NavRouts.PackingScreen>()
            }
            val packingVm: PackingVM = viewModel()
            PackingScreen(packingVm.also {
                it.inventoryLpn = data.lpnId
                it.packingLocation = data.packingLocation
            })

        }

        composable<NavRouts.PrePackingScreen> {
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            val location = remember {
                it.toRoute<NavRouts.PrePackingScreen>().location
            }
            val vm = viewModel<PrePackingVM>().apply {
                packingLocation = location
            }
            PrePackingScreen(vm) { params ->
                navController.navigateTo(NavRouts.PackingScreen(params, location), finish = true)
            }
        }

        composable<NavRouts.DirectedWorkScreen> { entry ->
            val data = remember {
                json.decodeFromString<List<WorkType>>(
                    entry.toRoute<NavRouts.DirectedWorkScreen>().list
                )
            }
            DirectedWorkScreen(
                data,
                onTaskClick = { navController.navigateTo(NavRouts.WorkSelectionScreen(it)) })
        }


        composable<NavRouts.WorkSelectionScreen> { entry ->
            val homeVm = LocalHomeViewModelProvider.current
            val viewModel: WorkSelectionVM = viewModel()
            GetOneTimeBlock {
                val taskType = entry.toRoute<NavRouts.WorkSelectionScreen>().type
                val workList = entry.toRoute<NavRouts.WorkSelectionScreen>().workList
                viewModel.setAreaAndEquipment(homeVm.selectedArea, homeVm.selectedEquipment)
                taskType?.let { viewModel.setTaskType(it) }
                workList?.let { viewModel.updateList(json.decodeFromString(it)) }

            }


            WorkSelectionScreen(
                viewModel,
                onBack = {
                    if (it) {
                        navController.popBackStack(NavRouts.MainScreen, inclusive = false)
                    } else {
                        navController.finish()
                    }
                },
                onNavigateToTask = {
                    navController.navigateTo(NavRouts.AcceptedWorkScreen)
                }

            )
        }
        composable<NavRouts.AcceptedWorkScreen> {
            AcceptedWorkScreen(
                onFinish = { needRefresh ->
                    navController.setResults(
                        NavigationResults.ResultOK,
                        finish = true,
                        sendExtras = {
                            putExtra(AppConstants.Defaults.NEED_REFRESH, needRefresh)
                        })
                }
            )
        }


        composable<NavRouts.ReceivingTrailerScreen> { entry ->
            // Get params from route
            val params = remember {
                json.decodeFromString<HashMap<String, String?>>(
                    entry.toRoute<NavRouts.ReceivingTrailerScreen>().params
                )
            }

            ReceivingTrailerScreen(
                initialParams = params,  // Pass params to screen
                onRefresh = { currentParams ->
                    // Refresh screen with same params
                    val uniqueParams = json.encodeToString(currentParams)
                    navController.navigateTo(
                        NavRouts.ReceivingTrailerScreen(uniqueParams),
                        finish = true,
                        singleTop = false
                    )
                }

            ) {
                navController.finish()
            }
        }

        composable<NavRouts.PalletsScreen> {
            val homeVM = LocalHomeViewModelProvider.current
            PalletBuildingScreen(
                homeVM.selectedArea, updateSelectedAreaType = {
                    homeVM.selectedAreaType.value = it
                }, clearSelectedArea = {
                    homeVM.selectedArea = ""
                }, navigate = {
                    homeVM.selectedAreaType.value = null
                    navController.finish()
                })
        }
        composable<NavRouts.DepositScreen> {
            DepositScreen {
                navController.finish()
            }
        }

        composable<NavRouts.PickingScreen> {
            val data = remember {
                json.decodeFromString<DataForPicking>(
                    it.toRoute<NavRouts.PickingScreen>().data
                )
            }
            val workFlow = remember {
                json.decodeFromString<List<PickingAttribute?>?>(
                    it.toRoute<NavRouts.PickingScreen>().idFields.toString()
                )
            }
            val taskListId = remember {
                json.decodeFromString<List<TaskInfo?>?>(
                    it.toRoute<NavRouts.PickingScreen>().taskListIds.toString()
                )
            }
            val context = LocalContext.current
            val vm = viewModel<PickingVM>()
            GetOneTimeBlock {
                vm.apply {
                    this.taskListIds.clear()
                    taskListId?.let { it1 -> this.taskListIds.addAll(it1) }
                    dataForPicking = data
                    dataForPickingValues = PickingFixFields()
                    partialSubmitAllowed = true
                    getDataForUI(context, workFlow, true)
                }
            }
            PickingScreen(
                vm,
                onComplete = {
                    context.showToast("Picking Successful")
                    navController.navigateTo(NavRouts.MainScreen)
                }
            )
        }

        composable<NavRouts.CustomScreen> {
            val data = remember {
                json.decodeFromString<CustomData>(
                    it.toRoute<NavRouts.CustomScreen>().data
                )
            }
            CustomScreen(data, onExit = { navController.popBackStack() })
        }


    }


}

