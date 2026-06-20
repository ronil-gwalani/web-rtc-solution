package com.renxo.user.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavRouts {

    @Serializable
    data object CheckInTrailerScreen : NavRouts()

    @Serializable
    data object UnloadTrailerScreen : NavRouts()


    @Serializable
    data object DepositScreen : NavRouts()


    @Serializable
    data object MainScreen : NavRouts()


    @Serializable
    data class ReceivingTrailerScreen(val params: String) : NavRouts()

    @Serializable
    data object PalletsScreen : NavRouts()


    @Serializable
    data object IBDScreen : NavRouts()

    @Serializable
    data class PackingScreen(val lpnId: String, val packingLocation: String) : NavRouts()

    @Serializable
    data class WorkSelectionScreen(val type: String?, val workList: String? = null) : NavRouts()

    @Serializable
    data class PrePackingScreen(val location: String) : NavRouts()

    @Serializable
    data class DirectedWorkScreen(val list: String) : NavRouts()

    @Serializable
    data object AcceptedWorkScreen : NavRouts()

    @Serializable
    data class PickingScreen(val data: String, val idFields: String?, val taskListIds: String?) :
        NavRouts()

    @Serializable
    data class CustomScreen(val data: String) : NavRouts()

}

sealed class OptionMenuRouts {

    @Serializable
    data object Calculator : NavRouts()

    @Serializable
    data object RenameLpn : NavRouts()

    @Serializable
    data object ChangeOwnerShip : NavRouts()

    @Serializable
    data object MarkDamage : NavRouts()

    @Serializable
    data object CancelWork : NavRouts()

    @Serializable
    data object Settings : NavRouts()
}


sealed class AuthRoutes {

    @Serializable
    data object Splash : NavRouts()

    @Serializable
    data object ConnectToServer : NavRouts()

    @Serializable
    data class SettingScreen(val list: List<String>) : NavRouts()

    @Serializable
    data object AuthenticatePage : NavRouts()

    @Serializable
    data object HomeScreen : NavRouts()
    @Serializable
    data object Testing : NavRouts()



}