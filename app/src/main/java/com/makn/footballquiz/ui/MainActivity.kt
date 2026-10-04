package com.makn.footballquiz.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.makn.footballquiz.FootballQuizApplication
import com.makn.footballquiz.ui.navigation.FootballQuizNavHost
import com.makn.footballquiz.ui.theme.FootballQuizTheme

/** AppCompat so the in-app language picker also works on Android 12 and older. */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as FootballQuizApplication).container

        // Consent first (Google UMP); ads initialise only once it allows them. A previous run's
        // consent lets ads start immediately while the status refreshes in the background.
        container.adsManager.initializeIfAllowed()
        container.consentManager.gather(this) { container.adsManager.initializeIfAllowed() }

        setContent {
            FootballQuizTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FootballQuizNavHost(container = container)
                }
            }
        }
    }
}
