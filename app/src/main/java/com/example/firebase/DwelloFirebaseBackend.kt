package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.network.DwelloNetworkConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class DwelloBackendMode {
  CLOUDFLARE,
  FIREBASE
}

object DwelloBackendConfig {
  fun configuredMode(): DwelloBackendMode {
    return if (BuildConfig.DWELLO_BACKEND_MODE.trim().equals("firebase", ignoreCase = true)) {
      DwelloBackendMode.FIREBASE
    } else {
      DwelloBackendMode.CLOUDFLARE
    }
  }

  fun firebaseApiBaseUrl(): String {
    val raw = BuildConfig.FIREBASE_API_BASE_URL.trim()
    if (raw.isEmpty()) return ""
    return if (raw.endsWith("/")) raw else "$raw/"
  }

  /**
   * Keeps the existing Cloudflare backend active during migration until both
   * DWELLO_BACKEND_MODE=firebase and FIREBASE_API_BASE_URL are explicitly configured.
   */
  fun activeMode(): DwelloBackendMode {
    return if (configuredMode() == DwelloBackendMode.FIREBASE && firebaseApiBaseUrl().isNotEmpty()) {
      DwelloBackendMode.FIREBASE
    } else {
      DwelloBackendMode.CLOUDFLARE
    }
  }

  fun isFirebaseApiActive(): Boolean = activeMode() == DwelloBackendMode.FIREBASE

  fun activeApiBaseUrl(): String {
    return if (isFirebaseApiActive()) {
      firebaseApiBaseUrl()
    } else {
      DwelloNetworkConfig.BASE_URL
    }
  }
}

enum class OperationType(val value: String) {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete"),
  LIST("list"),
  GET("get"),
  WRITE("write")
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
  val causes = generateSequence(exception) { it.cause }
  val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
    ?: causes.firstOrNull { it !is CancellationException }
    ?: exception

  val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()

  val providerInfoList = currentUser?.providerData?.map { provider ->
    JSONObject().apply {
      put("providerId", provider.providerId)
      put("email", provider.email)
    }
  } ?: emptyList()

  val authInfoJson = JSONObject().apply {
    put("userId", currentUser?.uid)
    put("email", currentUser?.email)
    put("emailVerified", currentUser?.isEmailVerified)
    put("tenantId", currentUser?.tenantId)
    put("providerInfo", JSONArray(providerInfoList))
  }

  val errorInfoJson = JSONObject().apply {
    put("error", rootError.message ?: rootError.toString())
    put("operationType", operationType.value)
    put("path", path)
    put("authInfo", authInfoJson)
  }

  val jsonString = errorInfoJson.toString()
  Log.e("FirestoreError", "Firestore Error: $jsonString")
  return jsonString
}

/**
 * Bridges Nigerian phone + password server responses (custom Firebase token)
 * to Firebase Authentication ID tokens without using Anonymous or Email/Password auth.
 */
interface FirebaseCustomTokenSessionBridge {
  suspend fun exchangeCustomTokenForIdToken(customToken: String): String
  suspend fun getCurrentFirebaseIdToken(forceRefresh: Boolean = false): String?
  fun signOutFirebase()
}

class AndroidFirebaseSessionBridge : FirebaseCustomTokenSessionBridge {
  override suspend fun exchangeCustomTokenForIdToken(customToken: String): String {
    val authResult = Firebase.auth.signInWithCustomToken(customToken).await()
    val user = authResult.user
      ?: throw IllegalStateException("Firebase custom-token sign-in succeeded without a user.")
    val tokenResult = user.getIdToken(false).await()
    return tokenResult.token
      ?: throw IllegalStateException("Unable to obtain Firebase ID token.")
  }

  override suspend fun getCurrentFirebaseIdToken(forceRefresh: Boolean): String? {
    val user = Firebase.auth.currentUser ?: return null
    return runCatching { user.getIdToken(forceRefresh).await().token }.getOrNull()
  }

  override fun signOutFirebase() {
    runCatching { Firebase.auth.signOut() }
  }
}

/**
 * Cloud Firestore client bound strictly to the provisioned named database ID
 * (`R.string.firestore_database_id`), aligned with `firestore.rules`.
 */
class DwelloFirestoreClient(private val firestore: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  suspend fun fetchApprovedAvailableProperties(): Result<List<Map<String, Any?>>> {
    val path = COLLECTION_PROPERTIES
    return try {
      val snapshot = firestore.collection(path)
        .whereEqualTo("moderationStatus", "approved")
        .whereEqualTo("availabilityStatus", "available")
        .get()
        .await()
      Result.success(snapshot.documents.map { doc -> doc.data.orEmpty() + ("id" to doc.id) })
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, path)
      Result.failure(e)
    }
  }

  suspend fun fetchLandlordProperties(landlordId: String): Result<List<Map<String, Any?>>> {
    val path = COLLECTION_PROPERTIES
    return try {
      val snapshot = firestore.collection(path)
        .whereEqualTo("landlordId", landlordId)
        .get()
        .await()
      Result.success(snapshot.documents.map { doc -> doc.data.orEmpty() + ("id" to doc.id) })
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, path)
      Result.failure(e)
    }
  }

  suspend fun fetchTenantEnquiries(tenantId: String): Result<List<Map<String, Any?>>> {
    val path = COLLECTION_ENQUIRIES
    return try {
      val snapshot = firestore.collection(path)
        .whereEqualTo("tenantId", tenantId)
        .get()
        .await()
      Result.success(snapshot.documents.map { doc -> doc.data.orEmpty() + ("id" to doc.id) })
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, path)
      Result.failure(e)
    }
  }

  suspend fun fetchUserFavourites(userId: String): Result<List<Map<String, Any?>>> {
    val path = COLLECTION_FAVOURITES
    return try {
      val snapshot = firestore.collection(path)
        .whereEqualTo("userId", userId)
        .get()
        .await()
      Result.success(snapshot.documents.map { doc -> doc.data.orEmpty() + ("id" to doc.id) })
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, path)
      Result.failure(e)
    }
  }

  companion object {
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PROPERTIES = "properties"
    const val COLLECTION_PROPERTY_IMAGES = "property_images"
    const val COLLECTION_ENQUIRIES = "enquiries"
    const val COLLECTION_FAVOURITES = "favourites"
  }
}

object DwelloStoragePathHelper {
  fun buildPropertyImagePath(
    landlordId: String,
    propertyId: String,
    imageId: String,
    extension: String = "jpg"
  ): String {
    val safeExt = extension.lowercase().trim().trimStart('.').ifEmpty { "jpg" }
    return "property-images/$landlordId/$propertyId/$imageId.$safeExt"
  }
}
