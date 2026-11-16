package com.example

import com.example.data.AuthAccountRole
import com.example.data.DwelloApiResult
import com.example.data.DwelloRepository
import com.example.data.InMemorySessionTokenStore
import com.example.firebase.DwelloBackendConfig
import com.example.firebase.DwelloBackendMode
import com.example.firebase.DwelloStoragePathHelper
import com.example.firebase.FirebaseCustomTokenSessionBridge
import com.example.network.DwelloNetworkClient
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseBackendIntegrationTest {

  @Test
  fun defaultMigrationConfig_keepsCloudflareOperationalWhileFirebaseUrlIsBlank() {
    assertEquals(DwelloBackendMode.CLOUDFLARE, DwelloBackendConfig.activeMode())
    assertFalse(DwelloBackendConfig.isFirebaseApiActive())
    assertTrue(DwelloBackendConfig.activeApiBaseUrl().contains("workers.dev"))
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
  fun repository_exchangesFirebaseCustomTokenForIdTokenAndStoresEncryptedSession() = runTest {
    val tokenStore = InMemorySessionTokenStore()
    val fakeBridge = object : FirebaseCustomTokenSessionBridge {
      var exchangedCustomToken: String? = null
      var signedOut = false

      override suspend fun exchangeCustomTokenForIdToken(customToken: String): String {
        exchangedCustomToken = customToken
        return "firebase-id-token-for-$customToken"
      }

      override suspend fun getCurrentFirebaseIdToken(forceRefresh: Boolean): String? {
        return tokenStore.getSessionToken()
      }

      override fun signOutFirebase() {
        signedOut = true
      }
    }

    val interceptor = Interceptor { chain ->
      val req = chain.request()
      val path = req.url.encodedPath
      val bodyText = when {
        path.endsWith("/api/auth/register") -> """
          {
            "success": true,
            "message": "Account created successfully.",
            "custom_token": "custom_tok_reg_123",
            "user": {
              "id": "dw_user_1",
              "full_name": "Osas Landlord",
              "phone": "+2348035551111",
              "role": "landlord",
              "phone_verified": 0
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
      baseUrl = "https://europe-west3-dwello-home.cloudfunctions.net/api/",
      extraInterceptors = listOf(interceptor)
    )
    val repo = DwelloRepository(apiService, tokenStore, firebaseSessionBridge = fakeBridge)

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
    assertEquals("custom_tok_reg_123", fakeBridge.exchangedCustomToken)
    assertEquals("firebase-id-token-for-custom_tok_reg_123", tokenStore.getSessionToken())

    repo.logout()
    assertTrue(fakeBridge.signedOut)
    assertEquals(null, tokenStore.getSessionToken())
  }
}
