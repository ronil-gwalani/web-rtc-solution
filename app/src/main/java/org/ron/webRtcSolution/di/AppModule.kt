/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webRtcSolution.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.get
import org.koin.dsl.module
import org.ron.webRtcSolution.HomeViewModel
import org.ron.webRtcSolution.R
import org.ron.webRtcSolution.RegistrationViewModel
//import org.ron.webrtccall.network.SecretInputStream

val appModule = module {
//    single<SecretInputStream> { SecretInputStream(get(), R.raw.service_account) }
    viewModelOf(::RegistrationViewModel)
    viewModelOf(::HomeViewModel)
}