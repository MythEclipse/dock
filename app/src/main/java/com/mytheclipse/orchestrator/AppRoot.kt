package com.mytheclipse.orchestrator

import androidx.compose.runtime.Composable
import com.mytheclipse.orchestrator.ui.navigation.AppNavGraph

@Composable
fun AppRoot(appContainer: AppContainer) {
    AppNavGraph(appContainer = appContainer)
}
