package com.communauto.tools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.communauto.tools.CommunautoApp
import com.communauto.tools.data.model.VehicleWithDistance
import com.communauto.tools.data.repository.VehicleRepository
import com.communauto.tools.util.DEFAULT_LAT
import com.communauto.tools.util.DEFAULT_LNG
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RadarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VehicleRepository =
        (application as CommunautoApp).vehicleRepository

    private val authManager = (application as CommunautoApp).authManager

    private val _allVehicles = MutableStateFlow<List<VehicleWithDistance>>(emptyList())

    private val _radius = MutableStateFlow(1000f)
    val radius = _radius.asStateFlow()

    private val _fuelFilter = MutableStateFlow(FuelFilter.ALL)
    val fuelFilter = _fuelFilter.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _userLat = MutableStateFlow(DEFAULT_LAT)
    val userLat = _userLat.asStateFlow()
    private val _userLng = MutableStateFlow(DEFAULT_LNG)
    val userLng = _userLng.asStateFlow()

    // Auto-refresh
    private val _refreshIntervalSec = MutableStateFlow(0)
    val refreshIntervalSec = _refreshIntervalSec.asStateFlow()
    private val _countdown = MutableStateFlow(0)
    val countdown = _countdown.asStateFlow()
    private var refreshJob: Job? = null

    // Auto-book
    private val _autoBookActive = MutableStateFlow(false)
    val autoBookActive = _autoBookActive.asStateFlow()
    private val _autoBookMessage = MutableStateFlow<String?>(null)
    val autoBookMessage = _autoBookMessage.asStateFlow()
    private val _autoBookSuccess = MutableStateFlow(false)
    val autoBookSuccess = _autoBookSuccess.asStateFlow()

    val vehiclesInRadius = combine(
        _allVehicles, _radius, _fuelFilter
    ) { vehicles, r, fuel ->
        vehicles
            .filter { it.distanceMeters <= r }
            .filter { v ->
                when (fuel) {
                    FuelFilter.ALL -> true
                    FuelFilter.ELECTRIC -> v.vehicle.isElectric
                    FuelFilter.GAS -> !v.vehicle.isElectric
                }
            }
            .sortedBy { it.distanceMeters }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFilteredVehicles = combine(
        _allVehicles, _fuelFilter
    ) { vehicles, fuel ->
        vehicles.filter { v ->
            when (fuel) {
                FuelFilter.ALL -> true
                FuelFilter.ELECTRIC -> v.vehicle.isElectric
                FuelFilter.GAS -> !v.vehicle.isElectric
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRadius(r: Float) { _radius.value = r }
    fun setFuelFilter(f: FuelFilter) { _fuelFilter.value = f }

    fun updateLocation(lat: Double, lng: Double) {
        _userLat.value = lat
        _userLng.value = lng
    }

    fun fetchVehicles() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _allVehicles.value = repository.getVehiclesWithDistance(
                    _userLat.value, _userLng.value
                )
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement"
            } finally {
                _loading.value = false
            }
        }
    }

    fun setRefreshInterval(seconds: Int) {
        _refreshIntervalSec.value = seconds
        refreshJob?.cancel()
        if (seconds > 0) {
            _countdown.value = seconds
            refreshJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _countdown.value--
                    if (_countdown.value <= 0) {
                        _countdown.value = seconds
                        fetchVehicles()
                        // Try auto-book after refresh
                        if (_autoBookActive.value) tryAutoBook()
                    }
                }
            }
        }
    }

    fun startAutoBook() {
        _autoBookActive.value = true
        _autoBookMessage.value = null
        _autoBookSuccess.value = false
        if (_refreshIntervalSec.value == 0) setRefreshInterval(15)
        viewModelScope.launch {
            fetchVehicles()
            tryAutoBook()
        }
    }

    fun stopAutoBook() {
        _autoBookActive.value = false
    }

    private suspend fun tryAutoBook() {
        if (!_autoBookActive.value) return

        val uid = authManager.getUid()
        if (uid == null) {
            _autoBookMessage.value = "uid introuvable dans les cookies"
            _autoBookActive.value = false
            return
        }

        val cars = vehiclesInRadius.value
        if (cars.isEmpty()) return

        for (car in cars) {
            try {
                val result = repository.createBooking(uid, car.vehicle.carId)
                if (result.success) {
                    _autoBookMessage.value =
                        "Réservé: #${car.vehicle.carNo} — ${car.vehicle.carBrand} ${car.vehicle.carModel} (${car.distanceMeters.toInt()} m)"
                    _autoBookSuccess.value = true
                    _autoBookActive.value = false
                    return
                }
                if (result.errorType == 3) {
                    _autoBookMessage.value = "#${car.vehicle.carNo} limite atteinte, essai suivant..."
                    continue
                }
                _autoBookMessage.value = result.errorMessage ?: "Réservation échouée"
                return
            } catch (e: Exception) {
                _autoBookMessage.value = e.message ?: "Erreur réseau"
                return
            }
        }
        _autoBookMessage.value = "Limite atteinte sur tous les véhicules dans le rayon"
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
