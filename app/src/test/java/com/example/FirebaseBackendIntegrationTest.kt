package com.example

import com.example.data.AuthAccountRole
import com.example.data.DwelloApiResult
import com.example.data.DwelloRepository
import com.example.data.InMemoryPropertyImageDataSource
import com.example.data.InMemorySessionTokenStore
import com.example.data.RentalPeriod
import com.example.data.SelectedPhotoItem
import com.example.firebase.DwelloFirebaseConfig
import com.example.firebase.DwelloStoragePathHelper
import com.example.network.DwelloNetworkClient
import com.example.network.DwelloNetworkConfig
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseBackendIntegrationTest {

  @Test
  fun cloudflareActiveBackend_usesVerifiedWorkersEndpointWhilePreservingDormantFirebaseConfig() {
    assertEquals("https://dwellohomes-api.jimmynathan11.workers.dev/", DwelloNetworkConfig.BASE_URL)
    val dormantFirebaseUrl = DwelloFirebaseConfig.DEFAULT_API_BASE_URL
    assertTrue(dormantFirebaseUrl.startsWith("https://"))
    assertTrue(dormantFirebaseUrl.contains("dwello-home"))
    assertFalse(dormantFirebaseUrl.contains("workers.dev"))
  }

  @Test
  fun storagePathHelper_formatsCompliantPropertyImagePath() {
    val path = DwelloStoragePathHelper.buildPropertyImagePath(
      landlordId = "dw_landlord_1",
      propertyId = "prop_abc",
      imageId = "img_01",
      extension = ".PNG"
    )
    assertEquals("property-images/dw_landlord_1/prop_abc/img_01.png", path)
  }

  @Test
  fun repository_registersLogsInAndUploadsMultipartPropertyImagesViaCloudflareApi() = runTest {
    val tokenStore = InMemorySessionTokenStore()
    val recordedRequests = mutableListOf<Pair<String, String>>()

    val interceptor = Interceptor { chain ->
      val req = chain.request()
      val path = req.url.encodedPath
      val buf = Buffer()
      req.body?.writeTo(buf)
      recordedRequests.add("${req.method} $path" to buf.readUtf8())
      val bodyText = when {
        path.endsWith("/api/auth/register") -> """
          {
            "success": true,
            "message": "Account created successfully."
          }
        """.trimIndent()
        path.endsWith("/api/auth/login") -> """
          {
            "success": true,
            "token": "cf_jwt_reg_123",
            "user": {
              "id": "dw_user_1",
              "full_name": "Osas Landlord",
              "phone": "+2348035551111",
              "role": "landlord"
            }
          }
        """.trimIndent()
        path.endsWith("/api/properties") -> """
          {
            "success": true,
            "message": "Submitted",
            "property_id": "prop_cf_101",
            "moderation_status": "pending_approval"
          }
        """.trimIndent()
        path.endsWith("/api/properties/prop_cf_101/images") -> """
          {
            "success": true,
            "message": "Property image uploaded.",
            "image": {
              "id": "img_doc_1",
              "property_id": "prop_cf_101",
              "image_key": "properties/prop_cf_101/img_1.jpg",
              "image_url": "/api/images/properties%2Fprop_cf_101%2Fimg_1.jpg",
              "display_order": 0
            }
          }
        """.trimIndent()
        path.endsWith("/api/auth/logout") -> """{"success": true, "message": "Logged out"}"""
        else -> """{"success": false, "message": "Not found"}"""
      }
      Response.Builder()
        .request(req)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(bodyText.toResponseBody("application/json".toMediaType()))
        .build()
    }

    val apiService = DwelloNetworkClient.createApiService(
      tokenStore = tokenStore,
      baseUrl = DwelloNetworkConfig.BASE_URL,
      extraInterceptors = listOf(interceptor)
    )
    val repo = DwelloRepository(
      apiService = apiService,
      tokenStore = tokenStore,
      imageDataSource = InMemoryPropertyImageDataSource(
        fallbackSampleBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
      )
    )

    val result = repo.registerAndLogin(
      fullName = "Osas Landlord",
      normalizedPhone = "+2348035551111",
      email = null,
      passwordChars = "StrongPass1!".toCharArray(),
      role = AuthAccountRole.LANDLORD
    )

    assertTrue(result is DwelloApiResult.Success)
    val user = (result as DwelloApiResult.Success).data
    assertEquals(AuthAccountRole.LANDLORD, user.role)
    assertEquals("cf_jwt_reg_123", tokenStore.getSessionToken())

    val propResult = repo.createPropertyWithImages(
      title = "Executive 2 Bedroom Flat in GRA",
      propertyTypeDisplay = "2 Bedroom",
      bedrooms = 2,
      bathrooms = 2,
      rentAmount = 750000L,
      rentalPeriod = RentalPeriod.YEARLY,
      description = "Borehole water, prepaid meter, fenced gate.",
      neighbourhood = "GRA",
      fullAddress = "14 Boundary Road, GRA, Benin City",
      landmark = "Near Golf Course",
      photos = listOf(SelectedPhotoItem("file:///tmp/101.jpg", "image/jpeg", "101.jpg"))
    )
    assertTrue(propResult is com.example.data.PropertySubmissionResult.Success)
    val success = propResult as com.example.data.PropertySubmissionResult.Success
    assertEquals("prop_cf_101", success.propertyId)
    assertEquals(1, success.uploadedImages.size)
    assertTrue(recordedRequests.any { (route, _) -> route == "POST /api/properties/prop_cf_101/images" })

    repo.logout()
    assertEquals(null, tokenStore.getSessionToken())
  }
}
