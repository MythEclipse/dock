package com.mytheclipse.orchestrator

import androidx.compose.runtime.Composable
import com.mytheclipse.orchestrator.ui.navigation.AppNavGraph
import com.mytheclipse.orchestrator.ui.theme.DockorchTheme

@Composable
fun AppRoot(appContainer: AppContainer) {
    DockorchTheme {
        AppNavGraph(appContainer = appContainer)
    }
}
