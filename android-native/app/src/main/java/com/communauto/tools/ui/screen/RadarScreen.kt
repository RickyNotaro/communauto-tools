package com.communauto.tools.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.communauto.tools.data.model.VehicleWithDistance
import com.communauto.tools.ui.theme.ElectricBlue
import com.communauto.tools.ui.theme.GasGreen
import com.communauto.tools.ui.theme.VehicleGray
import com.communauto.tools.viewmodel.FuelFilter
import com.communauto.tools.viewmodel.RadarViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    viewModel: RadarViewModel,
    isAuthenticated: Boolean,
) {
    val userLat by viewModel.userLat.collectAsState()
    val userLng by viewModel.userLng.collectAsState()
    val radius by viewModel.radius.collectAsState()
    val fuelFilter by viewModel.fuelFilter.collectAsState()
    val vehiclesInRadius by viewModel.vehiclesInRadius.collectAsState()
    val allVehicles by viewModel.allFilteredVehicles.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val refreshIntervalSec by viewModel.refreshIntervalSec.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val autoBookActive by viewModel.autoBookActive.collectAsState()
    val autoBookMessage by viewModel.autoBookMessage.collectAsState()
    val autoBookSuccess by viewModel.autoBookSuccess.collectAsState()

    val userPosition = LatLng(userLat, userLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userPosition, 15f)
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
        ),
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Radar") },
                actions = {
                    if (loading) {
                        Text(
                            "...",
                            modifier = Modifier.padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.fetchVehicles() }) {
                        Icon(Icons.Default.Refresh, "Rafraîchir")
                    }
                },
            )
        },
        sheetPeekHeight = 200.dp,
        sheetContent = {
            RadarControls(
                radius = radius,
                onRadiusChange = { viewModel.setRadius(it) },
                fuelFilter = fuelFilter,
                onFuelFilterChange = { viewModel.setFuelFilter(it) },
                vehiclesInRadius = vehiclesInRadius,
                refreshIntervalSec = refreshIntervalSec,
                countdown = countdown,
                onRefreshIntervalChange = { viewModel.setRefreshInterval(it) },
                isAuthenticated = isAuthenticated,
                autoBookActive = autoBookActive,
                autoBookMessage = autoBookMessage,
                autoBookSuccess = autoBookSuccess,
                onStartAutoBook = { viewModel.startAutoBook() },
                onStopAutoBook = { viewModel.stopAutoBook() },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = false),
            ) {
                // User position circle
                Circle(
                    center = userPosition,
                    radius = 15.0,
                    fillColor = androidx.compose.ui.graphics.Color(0xFF4285F4),
                    strokeColor = androidx.compose.ui.graphics.Color.White,
                    strokeWidth = 4f,
                )

                // Radius circle
                Circle(
                    center = userPosition,
                    radius = radius.toDouble(),
                    fillColor = androidx.compose.ui.graphics.Color(0x144285F4),
                    strokeColor = androidx.compose.ui.graphics.Color(0xFF4285F4),
                    strokeWidth = 2f,
                )

                // Vehicle markers
                allVehicles.forEach { vwd ->
                    val v = vwd.vehicle
                    val inRadius = vwd.distanceMeters <= radius
                    val hue = when {
                        !inRadius -> BitmapDescriptorFactory.HUE_ORANGE
                        v.isElectric -> BitmapDescriptorFactory.HUE_AZURE
                        else -> BitmapDescriptorFactory.HUE_GREEN
                    }

                    Marker(
                        state = MarkerState(position = LatLng(v.latitude, v.longitude)),
                        title = "#${v.carNo} ${v.carBrand} ${v.carModel}",
                        snippet = buildString {
                            append("${v.carColor} · ${v.carSeatNb} places")
                            if (v.isElectric) append(" · Électrique")
                            if (v.energyLevel != null) append(" · ${v.energyLevel}%")
                            append(" · ${vwd.distanceMeters.toInt()} m")
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(hue),
                        alpha = if (inRadius) 1f else 0.4f,
                    )
                }
            }

            // FAB: center on user
            FloatingActionButton(
                onClick = {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(userPosition, 15f)
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 210.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(Icons.Default.MyLocation, "Ma position")
            }
        }
    }
}

@Composable
private fun RadarControls(
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    fuelFilter: FuelFilter,
    onFuelFilterChange: (FuelFilter) -> Unit,
    vehiclesInRadius: List<VehicleWithDistance>,
    refreshIntervalSec: Int,
    countdown: Int,
    onRefreshIntervalChange: (Int) -> Unit,
    isAuthenticated: Boolean,
    autoBookActive: Boolean,
    autoBookMessage: String?,
    autoBookSuccess: Boolean,
    onStartAutoBook: () -> Unit,
    onStopAutoBook: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        // Radius slider
        Text(
            "Rayon : ${radius.toInt()} m",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Slider(
            value = radius,
            onValueChange = onRadiusChange,
            valueRange = 100f..5000f,
            steps = 49,
        )

        // Fuel filter
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = fuelFilter == FuelFilter.ALL,
                onClick = { onFuelFilterChange(FuelFilter.ALL) },
                label = { Text("Tous") },
            )
            FilterChip(
                selected = fuelFilter == FuelFilter.ELECTRIC,
                onClick = { onFuelFilterChange(FuelFilter.ELECTRIC) },
                label = { Text("Électrique") },
                leadingIcon = if (fuelFilter == FuelFilter.ELECTRIC) {
                    { Icon(Icons.Default.ElectricCar, null, Modifier.size(16.dp)) }
                } else null,
            )
            FilterChip(
                selected = fuelFilter == FuelFilter.GAS,
                onClick = { onFuelFilterChange(FuelFilter.GAS) },
                label = { Text("Essence") },
                leadingIcon = if (fuelFilter == FuelFilter.GAS) {
                    { Icon(Icons.Default.LocalGasStation, null, Modifier.size(16.dp)) }
                } else null,
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "${vehiclesInRadius.size} véhicule${if (vehiclesInRadius.size != 1) "s" else ""} dans le rayon",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Auto-refresh
        Spacer(Modifier.height(8.dp))
        Text(
            "Rafraîchissement",
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(15, 30, 60, 90).forEach { sec ->
                FilterChip(
                    selected = refreshIntervalSec == sec,
                    onClick = { onRefreshIntervalChange(sec) },
                    label = { Text("${sec}s") },
                )
            }
            FilterChip(
                selected = refreshIntervalSec == 0,
                onClick = { onRefreshIntervalChange(0) },
                label = { Text("Off") },
            )
        }
        if (refreshIntervalSec > 0) {
            Text(
                "Prochain refresh dans ${countdown}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Auto-book
        if (isAuthenticated) {
            Spacer(Modifier.height(12.dp))
            if (!autoBookActive) {
                Button(
                    onClick = onStartAutoBook,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                ) {
                    Text("Auto-réserver (${radius.toInt()} m)")
                }
            } else {
                Button(
                    onClick = onStopAutoBook,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Arrêter auto-réservation")
                }
                Text(
                    "En attente d'un véhicule dans ${radius.toInt()} m...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            autoBookMessage?.let { msg ->
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (autoBookSuccess)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // Vehicle list in radius
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(vehiclesInRadius, key = { it.vehicle.carId }) { vwd ->
                val v = vwd.vehicle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (v.isElectric) Icons.Default.ElectricCar else Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = if (v.isElectric) ElectricBlue else GasGreen,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "#${v.carNo}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${v.carBrand} ${v.carModel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${vwd.distanceMeters.toInt()} m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
