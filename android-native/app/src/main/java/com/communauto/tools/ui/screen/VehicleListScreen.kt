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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.communauto.tools.data.model.VehicleWithDistance
import com.communauto.tools.ui.component.VehicleCard
import com.communauto.tools.ui.component.VehicleDetailSheet
import com.communauto.tools.viewmodel.FuelFilter
import com.communauto.tools.viewmodel.SortMode
import com.communauto.tools.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    viewModel: VehicleViewModel,
    isAuthenticated: Boolean,
    onBookVehicle: (Int) -> Unit,
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val fuelFilter by viewModel.fuelFilter.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()

    var searchActive by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Detail bottom sheet
    selectedVehicle?.let { vehicle ->
        VehicleDetailSheet(
            vehicle = vehicle,
            isAuthenticated = isAuthenticated,
            onDismiss = { viewModel.selectVehicle(null) },
            onBook = {
                onBookVehicle(vehicle.vehicle.carId)
                viewModel.selectVehicle(null)
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Véhicules") },
                actions = {
                    // Sort menu
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.Default.FilterList, "Trier")
                        }
                        SortDropdown(
                            expanded = sortMenuExpanded,
                            currentSort = sortMode,
                            onSelect = { viewModel.setSortMode(it); sortMenuExpanded = false },
                            onDismiss = { sortMenuExpanded = false },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search bar
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onSearch = { searchActive = false },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Rechercher...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Rechercher") },
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {}

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = fuelFilter == FuelFilter.ALL,
                    onClick = { viewModel.setFuelFilter(FuelFilter.ALL) },
                    label = { Text("Tous") },
                )
                FilterChip(
                    selected = fuelFilter == FuelFilter.ELECTRIC,
                    onClick = { viewModel.setFuelFilter(FuelFilter.ELECTRIC) },
                    label = { Text("Électrique") },
                )
                FilterChip(
                    selected = fuelFilter == FuelFilter.GAS,
                    onClick = { viewModel.setFuelFilter(FuelFilter.GAS) },
                    label = { Text("Essence") },
                )
            }

            // Count
            Text(
                "${vehicles.size} véhicule${if (vehicles.size > 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            // Vehicle list
            PullToRefreshBox(
                isRefreshing = loading,
                onRefresh = { viewModel.fetchVehicles() },
                modifier = Modifier.fillMaxSize(),
            ) {
                if (error != null && vehicles.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            error ?: "",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(vehicles, key = { it.vehicle.carId }) { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                isSelected = selectedVehicle?.vehicle?.carId == vehicle.vehicle.carId,
                                onClick = { viewModel.selectVehicle(vehicle) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortDropdown(
    expanded: Boolean,
    currentSort: SortMode,
    onSelect: (SortMode) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        SortMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = {
                    Text(
                        when (mode) {
                            SortMode.DISTANCE -> "Distance"
                            SortMode.NUMBER -> "Numéro"
                            SortMode.BATTERY -> "Batterie"
                            SortMode.BRAND -> "Marque"
                        },
                    )
                },
                onClick = { onSelect(mode) },
                leadingIcon = if (currentSort == mode) {
                    { Text("✓") }
                } else null,
            )
        }
    }
}
