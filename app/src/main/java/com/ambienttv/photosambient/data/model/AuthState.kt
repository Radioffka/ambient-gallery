package com.ambienttv.photosambient.data.model

sealed interface AuthState {
    data object Unauthenticated : AuthState
    
    data class DeviceNaming(
        val proposedName: String = "Living Room TV"
    ) : AuthState
    
    data class WaitingForAuthorization(
        val userCode: String,
        val verificationUrl: String,
        val qrPayloadUri: String,
        val expiresInSeconds: Int = 900
    ) : AuthState
    
    data class ConfiguringMediaSources(
        val device: AmbientDevice
    ) : AuthState
    
    data class Connected(
        val account: GoogleAccountInfo,
        val device: AmbientDevice
    ) : AuthState
}