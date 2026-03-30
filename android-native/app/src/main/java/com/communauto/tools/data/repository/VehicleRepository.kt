package com.communauto.tools.data.repository

import com.communauto.tools.data.api.NetworkModule
import com.communauto.tools.data.model.BookingResponse
import com.communauto.tools.data.model.Vehicle
import com.communauto.tools.data.model.VehicleWithDistance
import com.communauto.tools.util.haversineDistance

class VehicleRepository {

    private val api = NetworkModule.api

    suspend fun getAvailableVehicles(): List<Vehicle> {
        val response = api.getAvailableVehicles()
        if (!response.d.success) {
            throw Exception(response.d.errorMessage ?: "Erreur inconnue")
        }
        return response.d.vehicles
    }

    suspend fun getVehiclesWithDistance(
        userLat: Double,
        userLng: Double,
    ): List<VehicleWithDistance> {
        return getAvailableVehicles().map { vehicle ->
            VehicleWithDistance(
                vehicle = vehicle,
                distanceMeters = haversineDistance(
                    userLat, userLng,
                    vehicle.latitude, vehicle.longitude,
                ),
            )
        }
    }

    suspend fun createBooking(customerId: Int, carId: Int): BookingResponse {
        return api.createBooking(customerId, carId).d
    }

    suspend fun cancelBooking(customerId: Int): BookingResponse {
        return api.cancelBooking(customerId).d
    }
}
