package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DwelloApiService {

  // Authentication
  @POST("api/auth/register")
  suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

  @POST("api/auth/login")
  suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

  @GET("api/auth/me")
  suspend fun getCurrentUser(): Response<MeResponse>

  @POST("api/auth/logout")
  suspend fun logout(): Response<ApiMessageResponse>

  // Tenant Property Discovery & Details
  @GET("api/properties")
  suspend fun getApprovedProperties(): Response<PropertiesListResponse>

  @GET("api/properties/{id}")
  suspend fun getPropertyById(@Path("id") id: String): Response<PropertyDetailResponse>

  // Favourites
  @GET("api/favourites")
  suspend fun getFavourites(): Response<PropertiesListResponse>

  @POST("api/favourites/{id}")
  suspend fun addFavourite(@Path("id") propertyId: String): Response<ApiMessageResponse>

  @DELETE("api/favourites/{id}")
  suspend fun removeFavourite(@Path("id") propertyId: String): Response<ApiMessageResponse>

  // Tenant Enquiries
  @POST("api/enquiries")
  suspend fun submitEnquiry(@Body request: CreateEnquiryRequest): Response<CreateEnquiryResponse>

  @GET("api/tenant/enquiries")
  suspend fun getTenantEnquiries(): Response<EnquiriesListResponse>

  // Landlord Properties & Enquiries
  @POST("api/properties")
  suspend fun createProperty(@Body request: CreatePropertyRequest): Response<CreatePropertyResponse>

  @GET("api/landlord/properties")
  suspend fun getLandlordProperties(): Response<PropertiesListResponse>

  @PATCH("api/properties/{id}")
  suspend fun updateProperty(
    @Path("id") propertyId: String,
    @Body request: UpdatePropertyRequest
  ): Response<UpdatePropertyResponse>

  @PATCH("api/properties/{id}/availability")
  suspend fun updatePropertyAvailability(
    @Path("id") propertyId: String,
    @Body request: UpdateAvailabilityRequest
  ): Response<UpdateAvailabilityResponse>

  @GET("api/landlord/enquiries")
  suspend fun getLandlordEnquiries(): Response<EnquiriesListResponse>

  @PATCH("api/landlord/enquiries/{id}")
  suspend fun updateEnquiryStatus(
    @Path("id") enquiryId: String,
    @Body request: UpdateEnquiryStatusRequest
  ): Response<UpdateEnquiryStatusResponse>
}
