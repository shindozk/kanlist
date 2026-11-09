package com.kanlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kanlist.domain.model.ThemeMode
import com.kanlist.presentation.navigation.AppNavHost
import com.kanlist.presentation.navigation.BottomNavItem
import com.kanlist.presentation.navigation.Screen
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.ui.explore.ExploreViewModel
import com.kanlist.presentation.ui.home.HomeViewModel
import com.kanlist.presentation.ui.library.LibraryViewModel
import com.kanlist.presentation.ui.profile.ProfileViewModel
import com.kanlist.presentation.ui.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val preferencesViewModel: PreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KanlistTheme {
                val navController = rememberNavController()
                val prefs by preferencesViewModel.preferences.collectAsStateWithLifecycle()
                
                // Check if onboarding is needed
                val showOnboarding = prefs.name.isBlank()
                
                AppNavHost(navController, startDestination = if (showOnboarding) Screen.Onboarding.route else Screen.Home.route)
            }
        }
    }
}

@Composable
fun MainNavHost(
    navController: androidx.navigation.NavController,
    onNavigateToWorkDetail: (String) -> Unit,
    onNavigateToWebView: (String, String) -> Unit
) {
    NavHost(navController, Screen.Home.route) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = { navController.navigateToHome() }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToWorkDetail = onNavigateToWorkDetail,
                onNavigateToWebView = onNavigateToWebView
            )
        }

        composable(Screen.Explore.route) {
            val viewModel: ExploreViewModel = hiltViewModel()
            ExploreScreen(
                viewModel = viewModel,
                onNavigateToWorkDetail = onNavigateToWorkDetail
            )
        }

        composable(Screen.Search.route) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                viewModel = viewModel,
                onNavigateToWorkDetail = onNavigateToWorkDetail
            )
        }

        composable(Screen.Library.route) {
            val viewModel: LibraryViewModel = hiltViewModel()
            LibraryScreen(
                viewModel = viewModel,
                onNavigateToWorkDetail = onNavigateToWorkDetail
            )
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

@Composable
fun BottomNavigationBar(
    navController: androidx.navigation.NavController,
    onNavigateToSearch: () -> Unit
) {
    val items = listOf(
        Screen.Home to "Início",
        Screen.Explore to "Explorar",
        Screen.Library to "Biblioteca",
        Screen.Profile to "Perfil"
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (screen, label) ->
            val selected = currentRoute?.startsWith(screen.route) == true
            BottomNavItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        if (screen == Screen.Home) {
                            navController.navigateToHome()
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                avoidMultipleCopies = true
                            }
                        }
                    }
                },
                icon = { ScreenIcon(screen) },
                label = label
            )
        }
        
        // FAB for search/add
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .wrapContentSize(align = androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            androidx.compose.material3.FloatingActionButton(
                onClick = onNavigateToSearch,
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                androidx.compose.material.icons.outlined.Add(
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ScreenIcon(screen: Screen) {
    val icon = when (screen) {
        Screen.Home -> androidx.compose.material.icons.outlined.Home
        Screen.Explore -> androidx.compose.material.icons.outlined.Explore
        Screen.Search -> androidx.compose.material.icons.outlined.Search
        Screen.Library -> androidx.compose.material.icons.outlined.LibraryBooks
        Screen.Profile -> androidx.compose.material.icons.outlined.Person
        else -> androidx.compose.material.icons.outlined.Help
    }
    icon(
        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// Preferences ViewModel for observing theme/name
@dagger.hilt.android.lifecycle.HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val prefsUseCases: com.kanlist.domain.usecase.PreferencesUseCases
) : ViewModel() {

    val preferences = prefsUseCases.observe()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(),
            initialValue = com.kanlist.domain.model.UserPreferences()
        )
}