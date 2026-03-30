package com.communauto.tools.data.api

import com.communauto.tools.data.model.BookingResponse
import com.communauto.tools.data.model.VehiclesResponse
import com.communauto.tools.data.model.WcfResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CommunautoApi {

    @GET("WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles")
    suspend fun getAvailableVehicles(
        @Query("BranchID") branchId: Int = 1,
        @Query("LanguageID") languageId: Int = 2,
        @Query("CityID") cityId: Int? = null,
    ): WcfResponse<VehiclesResponse>

    @GET("WCF/LSI/LSIBookingServiceV3.svc/CreateBooking")
    suspend fun createBooking(
        @Query("CustomerID") customerId: Int,
        @Query("CarID") carId: Int,
    ): WcfResponse<BookingResponse>

    @GET("WCF/LSI/LSIBookingServiceV3.svc/CancelBooking")
    suspend fun cancelBooking(
        @Query("CustomerID") customerId: Int,
        @Query("BranchID") branchId: Int = 1,
    ): WcfResponse<BookingResponse>

    @GET("WCF/LSI/LSIBookingServiceV3.svc/GetCurrentBooking")
    suspend fun getCurrentBooking(
        @Query("CustomerID") customerId: Int,
    ): WcfResponse<BookingResponse>
}
