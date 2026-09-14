package com.ruflo.footballquiz.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ruflo.footballquiz.FootballQuizApplication
import com.ruflo.footballquiz.ui.navigation.FootballQuizNavHost
import com.ruflo.footballquiz.ui.theme.FootballQuizTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as FootballQuizApplication).container

        setContent {
            FootballQuizTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FootballQuizNavHost(container = container)
                }
            }
        }
    }
}
