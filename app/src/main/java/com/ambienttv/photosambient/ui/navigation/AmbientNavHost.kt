package com.ambienttv.photosambient.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.data.model.AuthState
import com.ambienttv.photosambient.data.repository.AmbientPhotosRepository
import com.ambienttv.photosambient.slideshow.AmbientSlideshowController
import com.ambienttv.photosambient.ui.components.ButtonVariant
import com.ambienttv.photosambient.ui.components.TvButton
import com.ambienttv.photosambient.ui.components.rememberInitialFocusRequester
import com.ambienttv.photosambient.ui.screens.AmbientSlideshowScreen
import com.ambienttv.photosambient.ui.screens.DeviceNameScreen
import com.ambienttv.photosambient.ui.screens.DisconnectDialog
import com.ambienttv.photosambient.ui.screens.OAuthQrScreen
import com.ambienttv.photosambient.ui.screens.PermissionExplanationScreen
import com.ambienttv.photosambient.ui.screens.SettingsScreen
import com.ambienttv.photosambient.ui.screens.WaitingConfigurationScreen
import com.ambienttv.photosambient.ui.screens.WelcomeScreen
import com.ambienttv.photosambient.ui.theme.BackgroundDark
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

@Composable
fun AmbientNavHost(
    repository: AmbientPhotosRepository,
    slideshowController: AmbientSlideshowController,
    demoMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val authState by repository.authState.collectAsState()
    val device by repository.currentDevice.collectAsState()
    val account by repository.accountInfo.collectAsState()
    val items by repository.mediaItems.collectAsState()
    val accessToken by repository.accessToken.collectAsState()
    val queue by slideshowController.mediaQueue.collectAsState()

    var screen by remember { mutableStateOf(if (repository.currentDevice.value == null) "welcome" else "waiting_setup") }
    var deviceName by remember { mutableStateOf(device?.displayName ?: "Living Room TV") }
    var errorMessage by remember { mutableStateOf("") }
    var waitingMessage by remember { mutableStateOf("") }
    var returnToSettings by remember { mutableStateOf(false) }

    fun showError(error: Exception) {
        errorMessage = error.message ?: "Google Photos could not complete this step."
        screen = "error"
    }

    fun cancelSetup() {
        scope.launch {
            if (repository.disconnectGooglePhotos()) {
                slideshowController.setQueue(emptyList())
                screen = "welcome"
            } else {
                errorMessage = "Could not delete the Google Photos device. Check the connection and try again."
                screen = "error"
            }
        }
    }

    // Polling is tied to the authorization screen. Leaving it cancels the pending code.
    LaunchedEffect(screen, authState) {
        if (screen == "oauth_qr" && authState is AuthState.WaitingForAuthorization) {
            try {
                if (demoMode) delay(1000)
                repository.completeAuthorization()
                screen = "waiting_setup"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showError(error)
            }
        }
    }

    // The repository publishes ConfiguringMediaSources as soon as device creation
    // succeeds. That state change can cancel the OAuth polling effect above.
    LaunchedEffect(authState) {
        if (screen == "oauth_qr" && authState is AuthState.ConfiguringMediaSources) {
            screen = "waiting_setup"
        }
    }

    // Source selection is output-only. The TV observes it at Google's returned interval.
    LaunchedEffect(screen) {
        if (screen != "waiting_setup") return@LaunchedEffect
        var emptyAttempts = 0
        try {
            if (demoMode) {
                delay(1000)
                repository.simulateMediaSourcesConfigured()
            }
            while (coroutineContext.isActive && screen == "waiting_setup") {
                if (repository.checkMediaSourcesSet()) {
                    waitingMessage = "Selection saved. Looking for eligible photos..."
                    val fetched = repository.refreshMediaItems()
                    if (fetched.isNotEmpty()) {
                        slideshowController.setQueue(fetched)
                        screen = "slideshow"
                        break
                    }
                    emptyAttempts++
                    waitingMessage = "Google has not returned media yet. Retrying automatically."
                    delay((60_000L shl (emptyAttempts - 1).coerceAtMost(3)).coerceAtMost(600_000L))
                } else {
                    waitingMessage = "Waiting for album selection in the Google Photos app."
                    val seconds = repository.currentDevice.value?.pollingConfig?.pollInterval
                        ?.removeSuffix("s")?.toDoubleOrNull()?.toLong()?.coerceAtLeast(5) ?: 5L
                    delay(seconds * 1000L)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            showError(error)
        }
    }

    // Base URLs are short lived. Refresh the queue well before an hour has elapsed.
    LaunchedEffect(screen) {
        if (screen != "slideshow" || demoMode) return@LaunchedEffect
        while (coroutineContext.isActive && screen == "slideshow") {
            delay(30 * 60 * 1000L)
            try {
                val fresh = repository.refreshMediaItems()
                if (fresh.isNotEmpty()) slideshowController.setQueue(fresh)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Keep the current queue and retry at the next interval.
            }
        }
    }

    LaunchedEffect(items) {
        if (items.isNotEmpty() && slideshowController.mediaQueue.value.isEmpty()) {
            slideshowController.setQueue(items)
        }
    }

    LaunchedEffect(screen, queue.isEmpty()) {
        if (screen == "slideshow" && queue.isEmpty()) {
            waitingMessage = "No playable media is available yet. Retrying automatically."
            screen = "waiting_setup"
        }
    }

    BackHandler(enabled = screen != "welcome") {
        if (screen == "waiting_setup" && !returnToSettings) {
            cancelSetup()
        } else screen = when (screen) {
            "permissions" -> "welcome"
            "device_name" -> "permissions"
            "oauth_qr", "oauth_loading" -> "device_name"
            "waiting_setup" -> "settings"
            "selection_settings", "disconnect_dialog" -> "settings"
            "slideshow" -> "settings"
            "settings" -> "slideshow"
            "error" -> if (device == null) "device_name" else "waiting_setup"
            else -> "welcome"
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (screen) {
            "welcome" -> WelcomeScreen(onConnectClick = { screen = "permissions" })
            "permissions" -> PermissionExplanationScreen(
                onContinueClick = { screen = "device_name" },
                onBackClick = { screen = "welcome" }
            )
            "device_name" -> DeviceNameScreen(
                currentName = deviceName,
                onNameSelected = { deviceName = it },
                onContinueClick = {
                    screen = "oauth_loading"
                    scope.launch {
                        try {
                            repository.initiateAuthorization(deviceName)
                            screen = "oauth_qr"
                        } catch (error: Exception) {
                            showError(error)
                        }
                    }
                },
                onBackClick = { screen = "permissions" }
            )
            "oauth_loading" -> StatusScreen("Requesting a Google device code...")
            "oauth_qr" -> {
                val waiting = authState as? AuthState.WaitingForAuthorization
                if (waiting == null) {
                    StatusScreen("Preparing authorization...")
                } else {
                    OAuthQrScreen(
                        userCode = waiting.userCode,
                        verificationUrl = waiting.verificationUrl,
                        qrPayload = waiting.qrPayloadUri,
                        deviceName = deviceName,
                        onBackClick = { screen = "device_name" }
                    )
                }
            }
            "waiting_setup" -> WaitingConfigurationScreen(
                deviceName = device?.displayName ?: deviceName,
                settingsUri = repository.getSettingsUri(),
                statusMessage = waitingMessage,
                onBackClick = if (returnToSettings) ({ screen = "settings" }) else ({ cancelSetup() })
            )
            "selection_settings" -> WaitingConfigurationScreen(
                deviceName = device?.displayName ?: deviceName,
                settingsUri = repository.getSettingsUri(),
                statusMessage = "Choose a new album on your phone, then return to Settings and restart the slideshow.",
                activelyPolling = false,
                onBackClick = { screen = "settings" }
            )
            "settings" -> {
                val activeDevice = device
                val activeAccount = account
                if (activeDevice == null || activeAccount == null) {
                    StatusScreen("Checking Google Photos connection...")
                } else {
                    SettingsScreen(
                        accountInfo = activeAccount,
                        device = activeDevice,
                        onStartSlideshow = {
                            scope.launch {
                                try {
                                    val fresh = repository.refreshMediaItems()
                                    if (fresh.isNotEmpty()) {
                                        slideshowController.setQueue(fresh)
                                        screen = "slideshow"
                                    } else {
                                        returnToSettings = true
                                        waitingMessage = "Google has not returned media yet. Retrying automatically."
                                        screen = "waiting_setup"
                                    }
                                } catch (error: Exception) {
                                    showError(error)
                                }
                            }
                        },
                        onChangeSelection = {
                            screen = "selection_settings"
                        },
                        onDisconnectClick = { screen = "disconnect_dialog" }
                    )
                }
            }
            "slideshow" -> AmbientSlideshowScreen(
                controller = slideshowController,
                accessToken = accessToken,
                onOpenSettings = { screen = "settings" }
            )
            "disconnect_dialog" -> DisconnectDialog(
                deviceName = device?.displayName ?: deviceName,
                onConfirmDisconnect = {
                    scope.launch {
                        if (repository.disconnectGooglePhotos()) {
                            slideshowController.setQueue(emptyList())
                            returnToSettings = false
                            screen = "welcome"
                        } else {
                            errorMessage = "Could not delete the Google Photos device. Check the connection and try again."
                            screen = "error"
                        }
                    }
                },
                onCancel = { screen = "settings" }
            )
            "error" -> ErrorScreen(
                message = errorMessage,
                onRetry = { screen = if (device == null) "device_name" else "waiting_setup" }
            )
        }
    }
}

@Composable
private fun StatusScreen(message: String) {
    Box(Modifier.fillMaxSize().background(BackgroundDark), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CircularProgressIndicator()
            Text(message, color = Color.White, fontSize = 22.sp)
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    val primaryFocus = rememberInitialFocusRequester()
    Box(Modifier.fillMaxSize().background(BackgroundDark), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Google Photos setup needs attention", color = Color.White, fontSize = 30.sp)
            Text(message, color = Color.LightGray, fontSize = 18.sp)
            TvButton(
                text = "Retry",
                onClick = onRetry,
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.focusRequester(primaryFocus)
            )
        }
    }
}
