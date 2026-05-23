package com.mytheclipse.orchestrator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mytheclipse.orchestrator.ui.theme.DockorchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DockorchTheme {
                AppRoot(appContainer = AppContainer(applicationContext))
            }
        }
    }
}