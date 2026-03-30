package com.communauto.tools.data.model

import com.google.gson.annotations.SerializedName

/** Wrapper for all WCF responses (everything is nested under `.d`). */
data class WcfResponse<T>(val d: T)

data class VehiclesResponse(
    @SerializedName("Success") val success: Boolean,
    @SerializedName("Vehicles") val vehicles: List<Vehicle>,
    @SerializedName("ErrorMessage") val errorMessage: String?,
)

data class BookingResponse(
    @SerializedName("Success") val success: Boolean,
    @SerializedName("ErrorMessage") val errorMessage: String?,
    @SerializedName("ErrorType") val errorType: Int?,
)

data class Vehicle(
    @SerializedName("CarId") val carId: Int,
    @SerializedName("CarVin") val carVin: String,
    @SerializedName("CarPlate") val carPlate: String,
    @SerializedName("CarModel") val carModel: String,
    @SerializedName("CarNo") val carNo: Int,
    @SerializedName("CarBrand") val carBrand: String,
    @SerializedName("CarColor") val carColor: String,
    @SerializedName("CarSeatNb") val carSeatNb: Int,
    @SerializedName("CarAccessories") val carAccessories: List<Int>,
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("EnergyLevel") val energyLevel: Int?,
    @SerializedName("IsElectric") val isElectric: Boolean,
    @SerializedName("LastUseDate") val lastUseDate: String?,
    @SerializedName("LastUse") val lastUse: Int?,
    @SerializedName("isPromo") val isPromo: Boolean,
    @SerializedName("BookingStatus") val bookingStatus: Int,
    @SerializedName("BoardComputerType") val boardComputerType: Int,
    @SerializedName("CityID") val cityId: Int,
    @SerializedName("IsVehicleReturnFlex") val isVehicleReturnFlex: Boolean,
    @SerializedName("CarStationId") val carStationId: Int?,
    @SerializedName("VehiclePromotions") val vehiclePromotions: List<VehiclePromotion>?,
)

data class VehiclePromotion(
    @SerializedName("VehiculePromotionType") val promotionType: Int,
    @SerializedName("EndDate") val endDate: String?,
    @SerializedName("PriorityOrder") val priorityOrder: Int,
)

/** Vehicle enriched with distance from user. */
data class VehicleWithDistance(
    val vehicle: Vehicle,
    val distanceMeters: Double,
)

/** Human-readable accessory names based on accessory codes. */
object Accessories {
    private val map = mapOf(
        4 to "Bluetooth",
        8 to "Siège chauffant",
        16 to "Toit ouvrant",
        32 to "Régulateur",
        64 to "Caméra de recul",
        128 to "Apple CarPlay",
        256 to "Android Auto",
    )

    fun names(codes: List<Int>): List<String> =
        codes.mapNotNull { map[it] }
}

/** Booking status labels. */
fun Vehicle.statusLabel(): String = when (bookingStatus) {
    0 -> "Disponible"
    1 -> "Réservé"
    2 -> "En cours"
    else -> "Inconnu"
}
