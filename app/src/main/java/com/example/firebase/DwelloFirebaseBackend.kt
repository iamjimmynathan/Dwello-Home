package com.example.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.data.SelectedPhotoItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

/**
 * Sole active backend configuration for Dwello Homes using Firebase Cloud Functions (dwello-home).
 */
object DwelloFirebaseConfig {
  const val DEFAULT_API_BASE_URL = "https://europe-west3-dwello-home.cloudfunctions.net/api/"

  fun apiBaseUrl(): String {
    val configured = BuildConfig.FIREBASE_API_BASE_URL.trim()
    val raw = configured.ifEmpty { DEFAULT_API_BASE_URL }
    return if (raw.endsWith("/")) raw else "$raw/"
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
  fun getCurrentFirebaseUid(): String?
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

  override fun getCurrentFirebaseUid(): String? {
    return runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
  }

  override fun signOutFirebase() {
    runCatching { Firebase.auth.signOut() }
  }
}

data class UploadedPropertyPhoto(
  val storagePath: String,
  val imageUrl: String,
  val contentType: String
)

interface PropertyPhotoUploader {
  suspend fun uploadPropertyPhoto(
    landlordId: String,
    propertyId: String,
    photo: SelectedPhotoItem
  ): UploadedPropertyPhoto
}

class FirebaseStoragePropertyPhotoUploader(
  private val context: Context,
  private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : PropertyPhotoUploader {

  override suspend fun uploadPropertyPhoto(
    landlordId: String,
    propertyId: String,
    photo: SelectedPhotoItem
  ): UploadedPropertyPhoto = withContext(Dispatchers.IO) {
    val uri = Uri.parse(photo.uriString)
    val appContext = context.applicationContext
    val resolvedMime = normalizeMimeType(
      photo.mimeType ?: runCatching { appContext.contentResolver.getType(uri) }.getOrNull(),
      photo.uriString
    )
    val extension = mimeToExtension(resolvedMime)
    val bytes = appContext.contentResolver.openInputStream(uri)?.use { stream ->
      stream.readBytes()
    } ?: throw IllegalStateException("Unable to read selected property photo from device storage.")

    if (bytes.isEmpty()) {
      throw IllegalArgumentException("Selected property photo is empty.")
    }
    if (bytes.size > MAX_PHOTO_BYTES) {
      throw IllegalArgumentException("Each property photo must be 5 MB or smaller.")
    }

    val imageId = "img_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val storagePath = DwelloStoragePathHelper.buildPropertyImagePath(
      landlordId = landlordId,
      propertyId = propertyId,
      imageId = imageId,
      extension = extension
    )

    val ref = storage.reference.child(storagePath)
    val metadata = StorageMetadata.Builder()
      .setContentType(resolvedMime)
      .build()
    ref.putBytes(bytes, metadata).await()
    val downloadUrl = ref.downloadUrl.await().toString()

    UploadedPropertyPhoto(
      storagePath = storagePath,
      imageUrl = downloadUrl,
      contentType = resolvedMime
    )
  }

  private fun normalizeMimeType(rawMime: String?, uriString: String): String {
    val cleanMime = rawMime?.trim()?.lowercase(Locale.ROOT)
    if (cleanMime != null && cleanMime.startsWith("image/")) {
      return when (cleanMime) {
        "image/jpg" -> "image/jpeg"
        else -> cleanMime
      }
    }
    val ext = uriString.substringBefore('?').substringBefore('#').substringAfterLast('.', "").lowercase(Locale.ROOT)
    return when (ext) {
      "png" -> "image/png"
      "webp" -> "image/webp"
      "heic" -> "image/heic"
      "heif" -> "image/heif"
      else -> "image/jpeg"
    }
  }

  private fun mimeToExtension(mimeType: String): String {
    return when (mimeType.lowercase(Locale.ROOT)) {
      "image/png" -> "png"
      "image/webp" -> "webp"
      "image/heic" -> "heic"
      "image/heif" -> "heif"
      else -> "jpg"
    }
  }

  companion object {
    private const val MAX_PHOTO_BYTES = 5 * 1024 * 1024
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
    val safeExt = extension.lowercase(Locale.ROOT).trim().trimStart('.').ifEmpty { "jpg" }
    return "property-images/$landlordId/$propertyId/$imageId.$safeExt"
  }
}
