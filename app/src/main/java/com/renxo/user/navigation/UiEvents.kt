package com.renxo.user.navigation

import androidx.annotation.StringRes
import com.renxo.user.utils.ShackBarState
import com.renxo.user.utils.ShowSnackBar

interface UiEvents


fun navigateTo(
    routs: NavRouts,
    finish: Boolean = false,
    finishAll: Boolean = false,
    singleTop: Boolean = true
): UiEvents = Navigation(routs, finish, finishAll, singleTop)


fun showSnackBar(
    @StringRes message: Int,
    type: ShackBarState.ShackBarType = ShackBarState.ShackBarType.NEGATIVE
): UiEvents = ShowSnackBar(message, type)
