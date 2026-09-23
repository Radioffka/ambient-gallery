package com.ambienttv.photosambient.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ambienttv.photosambient.data.model.AmbientDevice
import com.ambienttv.photosambient.data.model.GoogleAccountInfo
import com.ambienttv.photosambient.data.repository.AmbientPhotosRepository
import com.ambienttv.photosambient.slideshow.AmbientSlideshowController
import com.ambienttv.photosambient.ui.components.ReviewerControllerBar
import com.ambienttv.photosambient.ui.screens.AmbientSlideshowScreen
import com.ambienttv.photosambient.ui.screens.DeviceNameScreen
import com.ambienttv.photosambient.ui.screens.DisconnectDialog
import com.ambienttv.photosambient.ui.screens.OAuthQrScreen
import com.ambienttv.photosambient.ui.screens.PermissionExplanationScreen
import com.ambienttv.photosambient.ui.screens.SettingsScreen
import com.ambienttv.photosambient.ui.screens.WaitingConfigurationScreen
import com.ambienttv.photosambient.ui.screens.WelcomeScreen
import kotlinx.coroutines.launch

@Composable
fun AmbientNavHost(
    repository: AmbientPhotosRepository,
    slideshowController: AmbientSlideshowController,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("welcome") }
    var currentDeviceName by remember { mutableStateOf("Living Room TV") }
    var previousScreenBeforeDisconnect by remember { mutableStateOf("settings") }

    val authState by repository.authState.collectAsState()
    val currentDevice by repository.currentDevice.collectAsState()
    val accountInfo by repository.accountInfo.collectAsState()
    val mediaItems by repository.mediaItems.collectAsState()

    // Sync media items to slideshow controller
    if (mediaItems.isNotEmpty() && slideshowController.mediaQueue.value.isEmpty()) {
        slideshowController.setQueue(mediaItems)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (currentScreen) {
            "welcome" -> {
                WelcomeScreen(
                    onConnectClick = {
                        currentScreen = "permissions"
                    }
                )
            }
            "permissions" -> {
                PermissionExplanationScreen(
                    onContinueClick = {
                        currentScreen = "device_name"
                    },
                    onBackClick = {
                        currentScreen = "welcome"
                    }
                )
            }
            "device_name" -> {
                DeviceNameScreen(
                    currentName = currentDeviceName,
                    onNameSelected = { name ->
                        currentDeviceName = name
                        coroutineScope.launch {
                            repository.updateDeviceName(name)
                        }
                    },
                    onContinueClick = {
                        coroutineScope.launch {
                            repository.initiateAuthorization(currentDeviceName)
                        }
                        currentScreen = "oauth_qr"
                    },
                    onBackClick = {
                        currentScreen = "permissions"
                    }
                )
            }
            "oauth_qr" -> {
                OAuthQrScreen(
                    deviceName = currentDeviceName,
                    onSimulateAuthSuccess = {
                        coroutineScope.launch {
                            repository.completeAuthorization()
                        }
                        currentScreen = "waiting_setup"
                    },
                    onBackClick = {
                        currentScreen = "device_name"
                    }
                )
            }
            "waiting_setup" -> {
                WaitingConfigurationScreen(
                    deviceName = currentDeviceName,
                    settingsUri = repository.getSettingsUri(),
                    onMediaSourcesConfigured = {
                        coroutineScope.launch {
                            repository.simulateMediaSourcesConfigured()
                            val items = repository.refreshMediaItems()
                            slideshowController.setQueue(items)
                        }
                        currentScreen = "settings"
                    },
                    onBackClick = {
                        currentScreen = "oauth_qr"
                    }
                )
            }
            "settings" -> {
                val activeAccount = accountInfo ?: GoogleAccountInfo()
                val activeDev = currentDevice ?: AmbientDevice(
                    deviceId = "demo_device_82914",
                    displayName = currentDeviceName,
                    mediaSourcesSet = true,
                    settingsUri = repository.getSettingsUri()
                )

                SettingsScreen(
                    accountInfo = activeAccount,
                    device = activeDev,
                    onStartSlideshow = {
                        coroutineScope.launch {
                            if (slideshowController.mediaQueue.value.isEmpty()) {
                                val items = repository.refreshMediaItems()
                                slideshowController.setQueue(items)
                            }
                        }
                        currentScreen = "slideshow"
                    },
                    onChangeSelection = {
                        currentScreen = "waiting_setup"
                    },
                    onDisconnectClick = {
                        previousScreenBeforeDisconnect = "settings"
                        currentScreen = "disconnect_dialog"
                    }
                )
            }
            "slideshow", "photo_ambient", "video_ambient" -> {
                AmbientSlideshowScreen(
                    controller = slideshowController,
                    onOpenSettings = {
                        currentScreen = "settings"
                    }
                )
            }
            "disconnect_dialog" -> {
                DisconnectDialog(
                    deviceName = currentDeviceName,
                    onConfirmDisconnect = {
                        coroutineScope.launch {
                            repository.disconnectGooglePhotos()
                        }
                        currentScreen = "welcome"
                    },
                    onCancel = {
                        currentScreen = previousScreenBeforeDisconnect
                    }
                )
            }
        }

        // Reviewer Demonstration Quick Bar (Unobtrusive floating bar)
        ReviewerControllerBar(
            currentScreenName = currentScreen,
            onNavigateTo = { targetRoute ->
                when (targetRoute) {
                    "photo_ambient" -> {
                        coroutineScope.launch {
                            repository.simulateMediaSourcesConfigured()
                            val items = repository.refreshMediaItems()
                            slideshowController.setQueue(items)
                        }
                        currentScreen = "slideshow"
                    }
                    "video_ambient" -> {
                        coroutineScope.launch {
                            repository.simulateMediaSourcesConfigured()
                            val items = repository.refreshMediaItems()
                            slideshowController.setQueue(items)
                            slideshowController.nextItem() // Advance to video item
                        }
                        currentScreen = "slideshow"
                    }
                    "settings" -> {
                        coroutineScope.launch {
                            repository.simulateMediaSourcesConfigured()
                        }
                        currentScreen = "settings"
                    }
                    else -> {
                        currentScreen = targetRoute
                    }
                }
            },
            onResetDemo = {
                coroutineScope.launch {
                    repository.resetToCleanInstall()
                }
                currentScreen = "welcome"
            }
        )
    }
}