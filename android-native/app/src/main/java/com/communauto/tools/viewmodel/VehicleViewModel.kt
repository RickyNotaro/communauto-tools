package com.communauto.tools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.communauto.tools.CommunautoApp
import com.communauto.tools.data.model.VehicleWithDistance
import com.communauto.tools.data.repository.VehicleRepository
import com.communauto.tools.util.DEFAULT_LAT
import com.communauto.tools.util.DEFAULT_LNG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

enum class SortMode { DISTANCE, NUMBER, BATTERY, BRAND }
enum class FuelFilter { ALL, ELECTRIC, GAS }

class VehicleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VehicleRepository =
        (application as CommunautoApp).vehicleRepository

    private val _vehicles = MutableStateFlow<List<VehicleWithDistance>>(emptyList())

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.DISTANCE)
    val sortMode = _sortMode.asStateFlow()

    private val _fuelFilter = MutableStateFlow(FuelFilter.ALL)
    val fuelFilter = _fuelFilter.asStateFlow()

    private val _userLat = MutableStateFlow(DEFAULT_LAT)
    private val _userLng = MutableStateFlow(DEFAULT_LNG)

    private val _selectedVehicle = MutableStateFlow<VehicleWithDistance?>(null)
    val selectedVehicle = _selectedVehicle.asStateFlow()

    val vehicles = combine(
        _vehicles, _searchQuery, _sortMode, _fuelFilter
    ) { vehicles, query, sort, fuel ->
        vehicles
            .filter { v ->
                when (fuel) {
                    FuelFilter.ALL -> true
                    FuelFilter.ELECTRIC -> v.vehicle.isElectric
                    FuelFilter.GAS -> !v.vehicle.isElectric
                }
            }
            .filter { v ->
                if (query.isBlank()) true
                else {
                    val q = query.lowercase()
                    v.vehicle.carNo.toString().contains(q) ||
                        v.vehicle.carBrand.lowercase().contains(q) ||
                        v.vehicle.carModel.lowercase().contains(q) ||
                        v.vehicle.carColor.lowercase().contains(q)
                }
            }
            .sortedWith(
                when (sort) {
                    SortMode.DISTANCE -> compareBy { it.distanceMeters }
                    SortMode.NUMBER -> compareBy { it.vehicle.carNo }
                    SortMode.BATTERY -> compareByDescending { it.vehicle.energyLevel ?: -1 }
                    SortMode.BRAND -> compareBy { it.vehicle.carBrand }
                }
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateLocation(lat: Double, lng: Double) {
        _userLat.value = lat
        _userLng.value = lng
        // Re-fetch with new location to recalculate distances
        fetchVehicles()
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortMode(mode: SortMode) { _sortMode.value = mode }
    fun setFuelFilter(filter: FuelFilter) { _fuelFilter.value = filter }
    fun selectVehicle(vehicle: VehicleWithDistance?) { _selectedVehicle.value = vehicle }

    fun fetchVehicles() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _vehicles.value = repository.getVehiclesWithDistance(
                    _userLat.value, _userLng.value
                )
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des véhicules"
            } finally {
                _loading.value = false
            }
        }
    }
}
