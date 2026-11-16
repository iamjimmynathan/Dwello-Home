package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.firebase.DwelloFirestoreClient
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DwelloFirestoreRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedCaller_cannotQueryApprovedProperties() = runBlocking {
    auth.signOut()
    val client = DwelloFirestoreClient(firestore)
    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      client.fetchApprovedAvailableProperties()
    }
    assertTrue(result.isFailure)
    val fsErr = result.exceptionOrNull()?.unwrapFirestoreException()
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
  }

  @Test
  fun authenticatedUser_scopedClientQueriesAlignWithSecurityRules() = runBlocking {
    val uid = signInTestUser("tenant_rules_test@dwello.ng")
    val client = DwelloFirestoreClient(firestore)

    val propsResult = withTimeout(DEFAULT_TIMEOUT_MS) { client.fetchApprovedAvailableProperties() }
    assertTrue("Approved+available property query should align with rules", propsResult.isSuccess)

    val landlordPropsResult = withTimeout(DEFAULT_TIMEOUT_MS) { client.fetchLandlordProperties(uid) }
    assertTrue("Landlord-scoped property query should align with rules", landlordPropsResult.isSuccess)

    val enquiriesResult = withTimeout(DEFAULT_TIMEOUT_MS) { client.fetchTenantEnquiries(uid) }
    assertTrue("Tenant-scoped enquiries query should align with rules", enquiriesResult.isSuccess)

    val favsResult = withTimeout(DEFAULT_TIMEOUT_MS) { client.fetchUserFavourites(uid) }
    assertTrue("User-scoped favourites query should align with rules", favsResult.isSuccess)
  }

  @Test
  fun authenticatedUser_unfilteredPropertiesQueryRejected() = runBlocking {
    signInTestUser("tenant_unfiltered@dwello.ng")
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("properties").get().await()
      }
      fail("Expected PERMISSION_DENIED on unfiltered properties query")
    } catch (e: Exception) {
      val fsErr = e.unwrapFirestoreException()
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }
  }

  @Test
  fun authenticatedUser_cannotAccessAuthCredentialsOrSelfAssignAdminRole() = runBlocking {
    val uid = signInTestUser("attacker_rules@dwello.ng")

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("auth_credentials").document(uid).get().await()
      }
      fail("Expected PERMISSION_DENIED on auth_credentials read")
    } catch (e: Exception) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.unwrapFirestoreException()?.code)
    }

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document(uid).set(
          mapOf(
            "uid" to uid,
            "fullName" to "Spoof Admin",
            "phoneE164" to "+2348031234567",
            "role" to "admin",
            "isActive" to true,
            "phoneVerified" to false
          )
        ).await()
      }
      fail("Expected PERMISSION_DENIED on client user profile creation")
    } catch (e: Exception) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.unwrapFirestoreException()?.code)
    }
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 5000L
  }
}
