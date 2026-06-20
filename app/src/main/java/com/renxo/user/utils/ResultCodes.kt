package com.renxo.user.utils

import android.content.Context
import com.renxo.user.R
import com.renxo.user.models.Result

fun Context.getRequiredMessage(result: Result): String? {
    val code = result.code
    return when (code) {

//                                SUCCESS CODES
        AppConstants.SuccessCodes.SUCCESS000 -> getString(R.string.SUCCESS000).replace(
            AppConstants.ResponseVariables.user,
            result.variables?.get(AppConstants.Params.user) ?: ""
        )

        AppConstants.SuccessCodes.SUCCESS002 -> getString(R.string.SUCCESS002).replace(
            AppConstants.ResponseVariables.created_lpn,
            result.variables?.get(AppConstants.Params.created_lpn) ?: ""
        )

//        AppConstants.SuccessCodes.SUCCESS008 -> getString(R.string.SUCCESS008).replace(
//            AppConstants.ResponseVariables.primaryKey,
//            result.variables?.get(AppConstants.Params.primaryKey) ?: ""
//        )

        AppConstants.SuccessCodes.SUCCESS020 -> getString(R.string.SUCCESS020)
        AppConstants.SuccessCodes.SUCCESS122 -> getString(R.string.SUCCESS122)
        AppConstants.SuccessCodes.SUCCESS199 -> getString(R.string.SUCCESS199)
//        AppConstants.SuccessCodes.SUCCESS200 -> getString(R.string.SUCCESS200)
//        AppConstants.SuccessCodes.SUCCESS201 -> getString(R.string.SUCCESS201)
        AppConstants.SuccessCodes.SUCCESS500 -> getString(R.string.SUCCESS500)
        AppConstants.SuccessCodes.SUCCESS220 -> getString(R.string.SUCCESS220)
//        AppConstants.SuccessCodes.SUCCESS233 -> getString(R.string.SUCCESS233)
        AppConstants.SuccessCodes.SUCCESS206 -> getString(R.string.SUCCESS206).replace(
            AppConstants.ResponseVariables.primaryKey,
            result.variables?.get(AppConstants.Params.primaryKey) ?: ""
        )

        AppConstants.SuccessCodes.SUCCESS211 -> getString(R.string.SUCCESS211)

        AppConstants.SuccessCodes.SUCCESS323 -> getString(R.string.SUCCESS323).replace(
            AppConstants.ResponseVariables.transportEquipment,
            result.variables?.get(AppConstants.Params.transportEquipment) ?: ""
        )

        AppConstants.SuccessCodes.SUCCESS324 -> getString(R.string.SUCCESS324).replace(
            AppConstants.ResponseVariables.lpn,
            result.variables?.get(AppConstants.Params.lpn) ?: ""
        ).replace(
            AppConstants.ResponseVariables.update,
            result.variables?.get(AppConstants.Params.update) ?: ""
        )

        AppConstants.SuccessCodes.SUCCESS325 -> getString(R.string.SUCCESS325).replace(
            AppConstants.ResponseVariables.ownership,
            result.variables?.get(AppConstants.Params.ownership) ?: ""
        ).replace(
            AppConstants.ResponseVariables.update,
            result.variables?.get(AppConstants.Params.update) ?: ""
        )

//        AppConstants.SuccessCodes.SUCCESS511 -> getString(R.string.SUCCESS511)
        AppConstants.SuccessCodes.SUCCESS501 -> getString(R.string.SUCCESS501)
        AppConstants.SuccessCodes.SUCCESS522 -> getString(R.string.SUCCESS522)
        AppConstants.SuccessCodes.SUCCESS542 -> getString(R.string.SUCCESS542).replace(
            AppConstants.ResponseVariables.user,
            result.variables?.get(AppConstants.Params.user) ?: ""
        )

//        AppConstants.SuccessCodes.SUCCESS545 -> getString(R.string.SUCCESS545)
        AppConstants.SuccessCodes.SUCCESS546 -> getString(R.string.SUCCESS546)
//        AppConstants.SuccessCodes.SUCCESS547 -> getString(R.string.SUCCESS547)
        AppConstants.SuccessCodes.SUCCESS655 -> getString(R.string.SUCCESS655)
        AppConstants.SuccessCodes.SUCCESS656 -> getString(R.string.SUCCESS656).replace(
            AppConstants.ResponseVariables.dock,
            result.variables?.get(AppConstants.Params.dock) ?: ""
        ).replace(
            AppConstants.ResponseVariables.ibd,
            result.variables?.get(AppConstants.Params.ibd) ?: ""
        )

        AppConstants.SuccessCodes.SUCCESS2001 -> getString(R.string.SUCCESS2001).replace(
            AppConstants.ResponseVariables.transportEquipment,
            result.variables?.get(AppConstants.Params.transportEquipment) ?: ""
        )

        AppConstants.SuccessCodes.SUCCESS5998 -> getString(R.string.SUCCESS5998).replace(
            AppConstants.ResponseVariables.users,
            result.variables?.get(AppConstants.Params.users) ?: ""
        )

        AppConstants.SuccessCodes.SUCCESS1234 -> getString(R.string.SUCCESS1234)
        AppConstants.SuccessCodes.SUCCESS1235 -> getString(R.string.SUCCESS1235)


//                                  ERROR CODES
        AppConstants.ErrorCodes.ERR000 -> getString(R.string.ERR000).replace(
            AppConstants.ResponseVariables.user,
            result.variables?.get(AppConstants.Params.user) ?: ""
        )

        AppConstants.ErrorCodes.ERR001 -> getString(R.string.ERR001)
        AppConstants.ErrorCodes.ERR002 -> getString(R.string.ERR002).replace(
            AppConstants.ResponseVariables.entityName,
            result.variables?.get(AppConstants.Params.entityName) ?: ""
        )

        AppConstants.ErrorCodes.ERR003 -> getString(R.string.ERR003)
        AppConstants.ErrorCodes.ERR004 -> getString(R.string.ERR004)
        AppConstants.ErrorCodes.ERR005 -> getString(R.string.ERR005)
        AppConstants.ErrorCodes.ERR006 -> getString(R.string.ERR006).replace(
            AppConstants.ResponseVariables.library,
            result.variables?.get(AppConstants.Params.library) ?: ""
        )

        AppConstants.ErrorCodes.ERR007 -> getString(R.string.ERR007).replace(
            AppConstants.ResponseVariables.library,
            result.variables?.get(AppConstants.Params.library) ?: ""
        )

        AppConstants.ErrorCodes.ERR104 -> getString(R.string.ERR104)
        AppConstants.ErrorCodes.ERR105 -> getString(R.string.ERR105)
        AppConstants.ErrorCodes.ERR106 -> getString(R.string.ERR106)
        AppConstants.ErrorCodes.ERR107 -> getString(R.string.ERR107).replace(
            AppConstants.ResponseVariables.user,
            result.variables?.get(AppConstants.Params.user) ?: ""
        )

        AppConstants.ErrorCodes.ERR108 -> getString(R.string.ERR108)
        AppConstants.ErrorCodes.ERR109 -> getString(R.string.ERR109)
        AppConstants.ErrorCodes.ERR110 -> getString(R.string.ERR110)
        AppConstants.ErrorCodes.ERR111 -> getString(R.string.ERR111)
        AppConstants.ErrorCodes.ERR122 -> getString(R.string.ERR122)
        AppConstants.ErrorCodes.ERR123 -> getString(R.string.ERR123)
        AppConstants.ErrorCodes.ERR124 -> getString(R.string.ERR124)
        AppConstants.ErrorCodes.ERR156 -> getString(R.string.ERR156)
        AppConstants.ErrorCodes.ERR158 -> getString(R.string.ERR158).replace(
            AppConstants.ResponseVariables.entityName,
            result.variables?.get(AppConstants.Params.entityName) ?: ""
        )

        AppConstants.ErrorCodes.ERR400 -> getString(R.string.ERR400).replace(
            AppConstants.ResponseVariables.action,
            result.variables?.get(AppConstants.Params.action) ?: ""
        )

        AppConstants.ErrorCodes.ERR226 -> getString(R.string.ERR226).replace(
            AppConstants.ResponseVariables.user,
            result.variables?.get(AppConstants.Params.user) ?: ""
        )

        AppConstants.ErrorCodes.ERR232 -> getString(R.string.ERR232)
        AppConstants.ErrorCodes.ERR235 -> getString(R.string.ERR235).replace(
            AppConstants.ResponseVariables.user,
            result.variables?.get(AppConstants.Params.user) ?: ""
        ).replace(
            AppConstants.ResponseVariables.function,
            result.variables?.get(AppConstants.Params.function) ?: ""
        )

        AppConstants.ErrorCodes.ERR212 -> getString(R.string.ERR212)
        AppConstants.ErrorCodes.ERR217 -> getString(R.string.ERR217).replace(
            AppConstants.ResponseVariables.library,
            result.variables?.get(AppConstants.Params.library) ?: ""
        )

        AppConstants.ErrorCodes.ERR219 -> getString(R.string.ERR219).replace(
            AppConstants.ResponseVariables.library,
            result.variables?.get(AppConstants.Params.library) ?: ""
        )

        AppConstants.ErrorCodes.ERR230 -> getString(R.string.ERR230).replace(
            AppConstants.ResponseVariables.error,
            result.variables?.get(AppConstants.Params.error) ?: ""
        )

        AppConstants.ErrorCodes.ERR231 -> getString(R.string.ERR231).replace(
            AppConstants.ResponseVariables.function,
            result.variables?.get(AppConstants.Params.function) ?: ""
        )

        AppConstants.ErrorCodes.ERR244 -> getString(R.string.ERR244)
        AppConstants.ErrorCodes.ERR253 -> getString(R.string.ERR253)
        AppConstants.ErrorCodes.ERR255 -> getString(R.string.ERR255)
        AppConstants.ErrorCodes.ERR256 -> getString(R.string.ERR256)
        AppConstants.ErrorCodes.ERR373 -> getString(R.string.ERR373)
        AppConstants.ErrorCodes.ERR374 -> getString(R.string.ERR374)
        AppConstants.ErrorCodes.ERR375 -> getString(R.string.ERR375).replace(
            AppConstants.ResponseVariables.transportEquipment,
            result.variables?.get(AppConstants.Params.transportEquipment) ?: ""
        )

        AppConstants.ErrorCodes.ERR376 -> getString(R.string.ERR376).replace(
            AppConstants.ResponseVariables.transportEquipment,
            result.variables?.get(AppConstants.Params.transportEquipment) ?: ""
        )

        AppConstants.ErrorCodes.ERR377 -> getString(R.string.ERR377)
        AppConstants.ErrorCodes.ERR404 -> getString(R.string.ERR404).replace(
            AppConstants.ResponseVariables.function,
            result.variables?.get(AppConstants.Params.function) ?: ""
        )

        AppConstants.ErrorCodes.ERR407 -> getString(R.string.ERR407)
        AppConstants.ErrorCodes.ERR408 -> getString(R.string.ERR408)
        AppConstants.ErrorCodes.ERR401 -> getString(R.string.ERR401)
        AppConstants.ErrorCodes.ERR405 -> getString(R.string.ERR405)
        AppConstants.ErrorCodes.ERR402 -> getString(R.string.ERR402)
        AppConstants.ErrorCodes.ERR403 -> getString(R.string.ERR403)
        AppConstants.ErrorCodes.ERR406 -> getString(R.string.ERR406).replace(
            AppConstants.ResponseVariables.entityName,
            result.variables?.get(AppConstants.Params.entityName) ?: ""
        )

        AppConstants.ErrorCodes.ERR409 -> getString(R.string.ERR409)
        AppConstants.ErrorCodes.ERR418 -> getString(R.string.ERR418)
        AppConstants.ErrorCodes.ERR452 -> getString(R.string.ERR452)
        AppConstants.ErrorCodes.ERR457 -> getString(R.string.ERR457)
        AppConstants.ErrorCodes.ERR458 -> getString(R.string.ERR458)
        AppConstants.ErrorCodes.ERR469 -> getString(R.string.ERR469)
        AppConstants.ErrorCodes.ERR470 -> getString(R.string.ERR470)
        AppConstants.ErrorCodes.ERR471 -> getString(R.string.ERR471)
        AppConstants.ErrorCodes.ERR473 -> getString(R.string.ERR473)
        AppConstants.ErrorCodes.ERR474 -> getString(R.string.ERR474).replace(
            AppConstants.ResponseVariables.transportEquipment,
            result.variables?.get(AppConstants.Params.transportEquipment) ?: ""
        )

        AppConstants.ErrorCodes.ERR475 -> getString(R.string.ERR475)
        AppConstants.ErrorCodes.ERR476 -> getString(R.string.ERR476)
        AppConstants.ErrorCodes.ERR477 -> getString(R.string.ERR477).replace(
            AppConstants.ResponseVariables.inboundDelivery,
            result.variables?.get(AppConstants.Params.inboundDelivery) ?: ""
        )

        AppConstants.ErrorCodes.ERR423 -> getString(R.string.ERR423)
        AppConstants.ErrorCodes.ERR479 -> getString(R.string.ERR479).replace(
            AppConstants.ResponseVariables.inboundDelivery,
            result.variables?.get(AppConstants.Params.inboundDelivery) ?: ""
        )

        AppConstants.ErrorCodes.ERR480 -> getString(R.string.ERR480)
        AppConstants.ErrorCodes.ERR481 -> getString(R.string.ERR481)
        AppConstants.ErrorCodes.ERR482 -> getString(R.string.ERR482)
        AppConstants.ErrorCodes.ERR367 -> getString(R.string.ERR367)
        AppConstants.ErrorCodes.ERR369 -> getString(R.string.ERR369)
        AppConstants.ErrorCodes.ERR483 -> getString(R.string.ERR483)
        AppConstants.ErrorCodes.ERR500 -> getString(R.string.ERR500)
        AppConstants.ErrorCodes.ERR501 -> getString(R.string.ERR501).replace(
            AppConstants.ResponseVariables.function,
            result.variables?.get(AppConstants.Params.function) ?: ""
        )

        AppConstants.ErrorCodes.ERR502 -> getString(R.string.ERR502)
        AppConstants.ErrorCodes.ERR503 -> getString(R.string.ERR503)
        AppConstants.ErrorCodes.ERR504 -> getString(R.string.ERR504)
        AppConstants.ErrorCodes.ERR506 -> getString(R.string.ERR506)
        AppConstants.ErrorCodes.ERR507 -> getString(R.string.ERR507)
        AppConstants.ErrorCodes.ERR118 -> getString(R.string.ERR118)
        AppConstants.ErrorCodes.ERR511 -> getString(R.string.ERR511)
        AppConstants.ErrorCodes.ERR512 -> getString(R.string.ERR512)
        AppConstants.ErrorCodes.ERR513 -> getString(R.string.ERR513)
        AppConstants.ErrorCodes.ERR509 -> getString(R.string.ERR509)
        AppConstants.ErrorCodes.ERR510 -> getString(R.string.ERR510)
        AppConstants.ErrorCodes.ERR514 -> getString(R.string.ERR514)
        AppConstants.ErrorCodes.ERR515 -> getString(R.string.ERR515)
        AppConstants.ErrorCodes.ERR572 -> getString(R.string.ERR572)
        AppConstants.ErrorCodes.ERR545 -> getString(R.string.ERR545)
        AppConstants.ErrorCodes.ERR602 -> getString(R.string.ERR602)
        AppConstants.ErrorCodes.ERR603 -> getString(R.string.ERR603)
        AppConstants.ErrorCodes.ERR604 -> getString(R.string.ERR604)
        AppConstants.ErrorCodes.ERR605 -> getString(R.string.ERR605)
        AppConstants.ErrorCodes.ERR606 -> getString(R.string.ERR606)
        AppConstants.ErrorCodes.ERR607 -> getString(R.string.ERR607)
        AppConstants.ErrorCodes.ERR608 -> getString(R.string.ERR608)
        AppConstants.ErrorCodes.ERR609 -> getString(R.string.ERR609)
        AppConstants.ErrorCodes.ERR610 -> getString(R.string.ERR610)
        AppConstants.ErrorCodes.ERR611 -> getString(R.string.ERR611)
        AppConstants.ErrorCodes.ERR612 -> getString(R.string.ERR612)
        AppConstants.ErrorCodes.ERR617 -> getString(R.string.ERR617)
        AppConstants.ErrorCodes.ERR615 -> getString(R.string.ERR615).replace(
            AppConstants.ResponseVariables.lpn, result.variables?.get(AppConstants.Params.lpn) ?: ""
        )

        AppConstants.ErrorCodes.ERR651 -> getString(R.string.ERR651)
        AppConstants.ErrorCodes.ERR657 -> getString(R.string.ERR657).replace(
            AppConstants.ResponseVariables.primaryKey,
            result.variables?.get(AppConstants.Params.primaryKey) ?: ""
        )

        AppConstants.ErrorCodes.ERR624 -> getString(R.string.ERR624).replace(
            AppConstants.ResponseVariables.location,
            result.variables?.get(AppConstants.Params.packing_Location) ?: ""
        )
        AppConstants.ErrorCodes.ERR411 -> getString(R.string.ERR411)
        AppConstants.ErrorCodes.ERR758 -> getString(R.string.ERR758)
        AppConstants.ErrorCodes.ERR759 -> getString(R.string.ERR759)
        AppConstants.ErrorCodes.ERR760 -> getString(R.string.ERR760).replace(
            AppConstants.ResponseVariables.inboundDelivery,
            result.variables?.get(AppConstants.Params.inboundDelivery) ?: ""
        )

        AppConstants.ErrorCodes.ERR766 -> getString(R.string.ERR766)
        AppConstants.ErrorCodes.ERR769 -> getString(R.string.ERR769)
        AppConstants.ErrorCodes.ERR775 -> getString(R.string.ERR775)
        AppConstants.ErrorCodes.ERR851 -> getString(R.string.ERR851)
        AppConstants.ErrorCodes.ERR951 -> getString(R.string.ERR951).replace(
            AppConstants.ResponseVariables.lpn, result.variables?.get(AppConstants.Params.lpn) ?: ""
        )

        AppConstants.ErrorCodes.ERR412 -> getString(R.string.ERR412)
        AppConstants.ErrorCodes.ERR420 -> getString(R.string.ERR420)
        AppConstants.ErrorCodes.ERR444 -> getString(R.string.ERR444)
        AppConstants.ErrorCodes.ERR4041 -> getString(R.string.ERR4041)
        AppConstants.ErrorCodes.ERR201 -> getString(R.string.ERR201)


//                                 WARNING CODES
        AppConstants.WarningCodes.WARN000 -> getString(R.string.WARN000)
        AppConstants.WarningCodes.WARN001 -> getString(R.string.WARN001).replace(
            AppConstants.ResponseVariables.lpn,
            result.variables?.get(AppConstants.Params.lpn) ?: ""
        )

        AppConstants.WarningCodes.WARN002 -> getString(R.string.WARN002)
        AppConstants.WarningCodes.WARN003 -> getString(R.string.WARN003).replace(
            AppConstants.ResponseVariables.locationType,
            result.variables?.get(AppConstants.Params.locationType) ?: ""
        )
//        AppConstants.WarningCodes.WARN4044 -> getString(R.string.WARN4044)


        else -> null
    }
}