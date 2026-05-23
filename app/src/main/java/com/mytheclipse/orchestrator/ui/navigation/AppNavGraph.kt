package com.mytheclipse.orchestrator.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mytheclipse.orchestrator.AppContainer
import com.mytheclipse.orchestrator.ui.screens.auditlogs.AuditLogsScreen
import com.mytheclipse.orchestrator.ui.screens.auditlogs.AuditLogsViewModel
import com.mytheclipse.orchestrator.ui.screens.containers.ContainerLogsScreen
import com.mytheclipse.orchestrator.ui.screens.containers.ContainersScreen
import com.mytheclipse.orchestrator.ui.screens.containers.ContainersViewModel
import com.mytheclipse.orchestrator.ui.screens.dashboard.DashboardScreen
import com.mytheclipse.orchestrator.ui.screens.login.LoginScreen
import com.mytheclipse.orchestrator.ui.screens.login.LoginViewModel
import com.mytheclipse.orchestrator.ui.screens.nodes.NodesScreen
import com.mytheclipse.orchestrator.ui.screens.users.UsersScreen
import com.mytheclipse.orchestrator.ui.screens.users.UsersViewModel

@Composable
fun AppNavGraph(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (appContainer.authRepository.hasSession()) {
        AppDestination.Dashboard.route
    } else {
        AppDestination.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestination.Login.route) {
            val loginViewModel = remember { LoginViewModel(appContainer.authRepository) }
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Dashboard.route) {
            AuthenticatedNavShell(
                navController = navController,
                currentRoute = AppDestination.Dashboard.route
            ) { modifier ->
                DashboardScreen(
                    appContainer = appContainer,
                    modifier = modifier,
                    onUnauthorized = {
                        appContainer.authRepository.clear()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(AppDestination.Nodes.route) {
            AuthenticatedNavShell(
                navController = navController,
                currentRoute = AppDestination.Nodes.route
            ) { modifier ->
                NodesScreen(
                    appContainer = appContainer,
                    modifier = modifier,
                    onUnauthorized = {
                        appContainer.authRepository.clear()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(AppDestination.Containers.route) {
            AuthenticatedNavShell(
                navController = navController,
                currentRoute = AppDestination.Containers.route
            ) { modifier ->
                val viewModel = remember {
                    ContainersViewModel(
                        appContainer.containerRepository,
                        appContainer.nodeRepository,
                        appContainer.userRepository
                    )
                }
                ContainersScreen(
                    viewModel = viewModel,
                    modifier = modifier,
                    onUnauthorized = {
                        appContainer.authRepository.clear()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(AppDestination.Users.route) {
            AuthenticatedNavShell(
                navController = navController,
                currentRoute = AppDestination.Users.route
            ) { modifier ->
                val viewModel = remember {
                    UsersViewModel(appContainer.userRepository)
                }
                UsersScreen(
                    viewModel = viewModel,
                    modifier = modifier,
                    onUnauthorized = {
                        appContainer.authRepository.clear()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(AppDestination.AuditLogs.route) {
            AuthenticatedNavShell(
                navController = navController,
                currentRoute = AppDestination.AuditLogs.route
            ) { modifier ->
                val viewModel = remember {
                    AuditLogsViewModel(appContainer.auditLogRepository)
                }
                AuditLogsScreen(
                    viewModel = viewModel,
                    modifier = modifier,
                    onUnauthorized = {
                        appContainer.authRepository.clear()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            AppDestination.ContainerLogs.route,
            arguments = listOf(
                navArgument("containerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val containerId = backStackEntry.arguments?.getString("containerId") ?: ""
            AuthenticatedNavShell(
                navController = navController,
                currentRoute = AppDestination.ContainerLogs.route,
                showBottomNav = false
            ) { modifier ->
                val viewModel = remember {
                    ContainersViewModel(
                        appContainer.containerRepository,
                        appContainer.nodeRepository,
                        appContainer.userRepository
                    )
                }
                ContainerLogsScreen(
                    containerId = containerId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun AuthenticatedNavShell(
    navController: NavHostController,
    currentRoute: String,
    showBottomNav: Boolean = true,
    content: @Composable (Modifier) -> Unit
) {
    if (showBottomNav) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    } else {
        content(Modifier)
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = currentDestination == AppDestination.Dashboard.route,
            onClick = {
                navController.navigate(AppDestination.Dashboard.route) {
                    popUpTo(AppDestination.Dashboard.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Dns, contentDescription = "Nodes") },
            label = { Text("Nodes") },
            selected = currentDestination == AppDestination.Nodes.route,
            onClick = {
                navController.navigate(AppDestination.Nodes.route) {
                    popUpTo(AppDestination.Dashboard.route)
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Storage, contentDescription = "Containers") },
            label = { Text("Containers") },
            selected = currentDestination == AppDestination.Containers.route,
            onClick = {
                navController.navigate(AppDestination.Containers.route) {
                    popUpTo(AppDestination.Dashboard.route)
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.SupervisedUserCircle, contentDescription = "Users") },
            label = { Text("Users") },
            selected = currentDestination == AppDestination.Users.route,
            onClick = {
                navController.navigate(AppDestination.Users.route) {
                    popUpTo(AppDestination.Dashboard.route)
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.VerifiedUser, contentDescription = "Audit Logs") },
            label = { Text("Audit") },
            selected = currentDestination == AppDestination.AuditLogs.route,
            onClick = {
                navController.navigate(AppDestination.AuditLogs.route) {
                    popUpTo(AppDestination.Dashboard.route)
                    launchSingleTop = true
                }
            }
        )
    }
}
