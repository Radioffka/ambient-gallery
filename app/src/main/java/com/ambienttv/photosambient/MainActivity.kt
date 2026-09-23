package com.ambienttv.photosambient

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ambienttv.photosambient.data.repository.MockAmbientPhotosRepository
import com.ambienttv.photosambient.slideshow.AmbientSlideshowController
import com.ambienttv.photosambient.ui.navigation.AmbientNavHost
import com.ambienttv.photosambient.ui.theme.AmbientTVTheme
import com.ambienttv.photosambient.ui.theme.BackgroundDark

class MainActivity : ComponentActivity() {

    private val repository = MockAmbientPhotosRepository()
    private val slideshowController = AmbientSlideshowController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmbientTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    AmbientNavHost(
                        repository = repository,
                        slideshowController = slideshowController
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Only handle dedicated TV media playback / menu keys globally
        // Standard D-pad navigation (Up/Down/Left/Right/Center) is delegated to Compose focus system
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                slideshowController.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_INFO -> {
                slideshowController.triggerTransientOverlay()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}