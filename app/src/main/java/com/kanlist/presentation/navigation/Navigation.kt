package com.kanlist.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import com.kanlist.presentation.ui.home.HomeScreen
import com.kanlist.presentation.ui.explore.ExploreScreen
import com.kanlist.presentation.ui.search.SearchScreen
import com.kanlist.presentation.ui.library.LibraryScreen
import com.kanlist.presentation.ui.profile.ProfileScreen
import com.kanlist.presentation.ui.workdetail.WorkDetailScreen
import com.kanlist.presentation.ui.onboarding.OnboardingScreen
import com.kanlist.presentation.ui.home.HomeViewModel
import com.kanlist.presentation.ui.explore.ExploreViewModel
import com.kanlist.presentation.ui.search.SearchViewModel
import com.kanlist.presentation.ui.library.LibraryViewModel
import com.kanlist.presentation.ui.profile.ProfileViewModel
import com.kanlist.presentation.ui.workdetail.WorkDetailViewModel
import com.kanlist.presentation.ui.onboarding.OnboardingViewModel
import dagger.hilt.android.EntryPoint
import dagger.hilt.android.EntryPoints
import javax.inject.Inject

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Search : Screen("search")
    object Library : Screen("library")
    object Profile : Screen("profile")
    class WorkDetail(val mediaId: String) : Screen("workdetail/$mediaId")
    class WebView(val url: String, val title: String) : Screen("webview")
}

@Composable
fun AppNavHost(
    navController: androidx.navigation.NavController,
    startDestination: String = Screen.Home.route
) {
    androidx.navigation.compose.NavHost(navController, startDestination) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(viewModel = viewModel)
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(viewModel = viewModel)
        }

        composable(Screen.Explore.route) {
            val viewModel: ExploreViewModel = hiltViewModel()
            ExploreScreen(viewModel = viewModel)
        }

        composable(Screen.Search.route) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(viewModel = viewModel)
        }

        composable(Screen.Library.route) {
            val viewModel: LibraryViewModel = hiltViewModel()
            LibraryScreen(viewModel = viewModel)
        }

        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(viewModel = viewModel)
        }

        composable(
            route = Screen.WorkDetail("").route,
            arguments = listOf(navArgument("mediaId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.getString() ?: ""
            val viewModel: WorkDetailViewModel = hiltViewModel()
            WorkDetailScreen(mediaId = mediaId, viewModel = viewModel)
        }
    }
}

// Bottom Navigation Item
@Composable
fun BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String
) {
    androidx.compose.material3.NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { icon() },
        label = { androidx.compose.material3.Text(text = label, style = androidx.compose.material3.Typography.labelSmall) },
        alwaysShowLabel = true
    )
}

// Navigation extensions
fun androidx.navigation.NavController.navigateToWorkDetail(mediaId: String) {
    navigate(Screen.WorkDetail(mediaId).route)
}

fun androidx.navigation.NavController.navigateToHome() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Home.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun androidx.navigation.NavController.navigateToOnboarding() {
    navigate(Screen.Onboarding.route) {
        popUpTo(Screen.Onboarding.route) { inclusive = true }
        launchSingleTop = true
    }
}