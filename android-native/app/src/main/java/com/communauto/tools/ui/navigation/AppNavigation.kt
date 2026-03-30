package com.communauto.tools.ui.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.communauto.tools.ui.screen.LoginScreen
import com.communauto.tools.ui.screen.RadarScreen
import com.communauto.tools.ui.screen.VehicleListScreen
import com.communauto.tools.viewmodel.AuthViewModel
import com.communauto.tools.viewmodel.RadarViewModel
import com.communauto.tools.viewmodel.VehicleViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

enum class Screen(val route: String, val label: String, val icon: ImageVector) {
    Vehicles("vehicles", "Véhicules", Icons.Default.DirectionsCar),
    Radar("radar", "Radar", Icons.Default.Radar),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    if (!isAuthenticated) {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = { /* state-driven: isAuthenticated flips → shows main */ },
        )
        return
    }

    val navController = rememberNavController()
    val vehicleViewModel: VehicleViewModel = viewModel()
    val radarViewModel: RadarViewModel = viewModel()

    val context = LocalContext.current

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Get user location
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            requestLocation(context) { lat, lng ->
                vehicleViewModel.updateLocation(lat, lng)
                radarViewModel.updateLocation(lat, lng)
            }
        }
        vehicleViewModel.fetchVehicles()
        radarViewModel.fetchVehicles()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Vehicles.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Vehicles.route) {
                VehicleListScreen(
                    viewModel = vehicleViewModel,
                    isAuthenticated = isAuthenticated,
                    onBookVehicle = { carId ->
                        // Booking handled via ViewModel
                    },
                )
            }
            composable(Screen.Radar.route) {
                RadarScreen(
                    viewModel = radarViewModel,
                    isAuthenticated = isAuthenticated,
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun requestLocation(
    context: Context,
    onLocation: (Double, Double) -> Unit,
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocation(location.latitude, location.longitude)
            }
        }
}
