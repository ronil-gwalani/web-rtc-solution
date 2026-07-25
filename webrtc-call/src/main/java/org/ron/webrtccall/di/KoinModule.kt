/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Dependency Injection Module
 */
package org.ron.webrtccall.di

import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.ron.webrtccall.CallViewModel
import org.ron.webrtccall.data.PreferenceManager
import org.ron.webrtccall.data.PreferenceProvider
import org.ron.webrtccall.manager.CallManager
import org.ron.webrtccall.manager.WebRtcCallManager
import org.ron.webrtccall.network.*
import org.ron.webrtccall.repository.FirebaseUserRepository
import org.ron.webrtccall.repository.UserRepository
import org.ron.webrtccall.utils.ProximityManager
import org.ron.webrtccall.utils.ProximitySensor
import org.ron.webrtccall.webrtc.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val libraryModule = module {
    // Data
    single<PreferenceProvider> { PreferenceManager(androidContext()) }
    
    // Firebase
    single { FirebaseFirestore.getInstance() }
    single<UserRepository> { FirebaseUserRepository(get(), get()) }
    
    // Network
    single {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmApiService::class.java)
    }
    
    single<SignalingService> { FcmNotificationSender(androidContext(), get(), get(),get()) }
    single<SignalingClientFactory> { FirebaseSignalingFactory() }
    
    // WebRTC
    single<WebRtcPeerConnectionProvider> { WebRtcPeerConnectionManager(androidContext()) }
    single<SessionManagerFactory> { WebRtcSessionManagerFactory(androidContext(), get()) }
    
    // Utils
    single<ProximitySensor> { ProximityManager(androidContext()) }
    
    // Managers
    single<CallManager> { WebRtcCallManager(get(), get(), get()) }
    
    // ViewModels
    viewModel { CallViewModel(get(), get(), get()) }
}
