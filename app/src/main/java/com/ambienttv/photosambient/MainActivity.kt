package com.ambienttv.photosambient

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.data.repository.AmbientPhotosRepository
import com.ambienttv.photosambient.data.repository.GoogleAmbientPhotosRepository
import com.ambienttv.photosambient.data.repository.MockAmbientPhotosRepository
import com.ambienttv.photosambient.slideshow.AmbientSlideshowController
import com.ambienttv.photosambient.ui.navigation.AmbientNavHost
import com.ambienttv.photosambient.ui.theme.AmbientTVTheme
import com.ambienttv.photosambient.ui.theme.BackgroundDark

class MainActivity : ComponentActivity() {

    private val repository: AmbientPhotosRepository by lazy {
        if (BuildConfig.AMBIENT_DEMO_MODE) MockAmbientPhotosRepository()
        else GoogleAmbientPhotosRepository(this, BuildConfig.AMBIENT_CLIENT_ID, BuildConfig.AMBIENT_CLIENT_SECRET)
    }
    private val slideshowController = AmbientSlideshowController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmbientTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    if (!BuildConfig.AMBIENT_DEMO_MODE &&
                        (BuildConfig.AMBIENT_CLIENT_ID.isBlank() || BuildConfig.AMBIENT_CLIENT_SECRET.isBlank())
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Build this APK with local Ambient OAuth credentials.", color = Color.White, fontSize = 24.sp)
                        }
                    } else {
                        AmbientNavHost(
                            repository = repository,
                            slideshowController = slideshowController,
                            demoMode = BuildConfig.AMBIENT_DEMO_MODE
                        )
                    }
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

    override fun onDestroy() {
        slideshowController.release()
        super.onDestroy()
    }
}
