package com.example.subscriptions.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.subscriptions.SubscriptionsApp
import com.example.subscriptions.notifications.ReminderNotifier
import com.example.subscriptions.ui.auth.LoginScreen
import com.example.subscriptions.ui.auth.RegisterScreen
import com.example.subscriptions.ui.details.DetailsScreen
import com.example.subscriptions.ui.edit.AddEditScreen
import com.example.subscriptions.ui.home.HomeScreen
import com.example.subscriptions.ui.settings.SettingsScreen
import com.example.subscriptions.ui.stats.StatsScreen

private const val LOADING = -1L

/** Пункт нижнего меню */
private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "Главная", Icons.Outlined.Home, Icons.Filled.Home),
    BottomItem(Routes.STATS, "Статистика", Icons.Outlined.PieChart, Icons.Filled.PieChart),
    BottomItem(Routes.SETTINGS, "Настройки", Icons.Outlined.Settings, Icons.Filled.Settings)
)

/**
 * Корень приложения: проверяет, вошёл ли пользователь,
 * и показывает навигацию между экранами.
 */
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as SubscriptionsApp).container }

    // id вошедшего пользователя из DataStore: LOADING — ещё читаем, null — никто не вошёл
    val userId by container.authRepository.currentUserId.collectAsState(initial = LOADING)

    if (userId == LOADING) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Стартовый экран выбирается один раз: главная, если сессия сохранена, иначе вход
    val startDestination = remember { if (userId != null) Routes.HOME else Routes.LOGIN }

    // После входа один раз просим разрешение на уведомления (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(userId) {
        if (userId != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !ReminderNotifier.hasPermission(context)
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in Routes.tabs) {
                BottomBar(currentRoute = currentRoute, navController = navController)
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.HOME) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.addEdit()) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить подписку")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onOpenRegister = {
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onOpenLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onAddClick = { navController.navigate(Routes.addEdit()) },
                    onSubscriptionClick = { id -> navController.navigate(Routes.details(id)) }
                )
            }
            composable(
                route = Routes.DETAILS,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) {
                DetailsScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.addEdit(id)) }
                )
            }
            composable(
                route = Routes.ADD_EDIT,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) {
                AddEditScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomBar(currentRoute: String?, navController: NavHostController) {
    NavigationBar {
        bottomItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = null
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
