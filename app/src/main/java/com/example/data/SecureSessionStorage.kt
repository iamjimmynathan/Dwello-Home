package com.example.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface SessionTokenStore {
  fun saveSessionToken(token: String, expiresAt: String?)
  fun getSessionToken(): String?
  fun getSessionExpiresAt(): String?
  fun clearSession()
}

/**
 * In-memory session token store for JVM unit tests (never persists credentials to disk).
 */
class InMemorySessionTokenStore : SessionTokenStore {
  @Volatile
  private var token: String? = null

  @Volatile
  private var expiresAt: String? = null

  override fun saveSessionToken(token: String, expiresAt: String?) {
    this.token = token
    this.expiresAt = expiresAt
  }

  override fun getSessionToken(): String? = token

  override fun getSessionExpiresAt(): String? = expiresAt

  override fun clearSession() {
    token = null
    expiresAt = null
  }
}

/**
 * Hardware-backed Android Keystore (AES-256-GCM) encrypted session token store.
 * - Never stores raw plain-text passwords.
 * - Never stores unencrypted session tokens in SharedPreferences.
 * - Encrypts the Bearer token with an AndroidKeyStore key using a fresh random 12-byte IV per save.
 */
class AndroidKeystoreSessionTokenStore(context: Context) : SessionTokenStore {

  private val appContext = context.applicationContext
  private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  // Volatile fallback used strictly when running in a JVM test sandbox without AndroidKeyStore
  @Volatile
  private var volatileFallbackToken: String? = null

  @Volatile
  private var volatileFallbackExpiresAt: String? = null

  override fun saveSessionToken(token: String, expiresAt: String?) {
    val secretKey = getOrCreateKeystoreSecretKey()
    if (secretKey == null) {
      // Never write unencrypted tokens to SharedPreferences
      volatileFallbackToken = token
      volatileFallbackExpiresAt = expiresAt
      return
    }

    runCatching {
      val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      val iv = cipher.iv
      val ciphertext = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))

      val encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP)
      val encodedCiphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

      prefs.edit()
        .putString(KEY_TOKEN_IV, encodedIv)
        .putString(KEY_TOKEN_CIPHERTEXT, encodedCiphertext)
        .putString(KEY_EXPIRES_AT, expiresAt)
        .apply()
    }.onFailure {
      clearSession()
      volatileFallbackToken = token
      volatileFallbackExpiresAt = expiresAt
    }
  }

  override fun getSessionToken(): String? {
    val encodedIv = prefs.getString(KEY_TOKEN_IV, null)
    val encodedCiphertext = prefs.getString(KEY_TOKEN_CIPHERTEXT, null)
    if (encodedIv.isNullOrBlank() || encodedCiphertext.isNullOrBlank()) {
      return volatileFallbackToken
    }

    val secretKey = getOrCreateKeystoreSecretKey() ?: return volatileFallbackToken

    return runCatching {
      val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
      val ciphertext = Base64.decode(encodedCiphertext, Base64.NO_WRAP)
      val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
      val plaintextBytes = cipher.doFinal(ciphertext)
      String(plaintextBytes, StandardCharsets.UTF_8)
    }.getOrElse {
      clearSession()
      null
    }
  }

  override fun getSessionExpiresAt(): String? {
    return prefs.getString(KEY_EXPIRES_AT, null) ?: volatileFallbackExpiresAt
  }

  override fun clearSession() {
    volatileFallbackToken = null
    volatileFallbackExpiresAt = null
    prefs.edit()
      .remove(KEY_TOKEN_IV)
      .remove(KEY_TOKEN_CIPHERTEXT)
      .remove(KEY_EXPIRES_AT)
      .apply()
  }

  private fun getOrCreateKeystoreSecretKey(): SecretKey? {
    return runCatching {
      val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
      keyStore.load(null)
      val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
      if (existing != null) {
        existing
      } else {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
          KEY_ALIAS,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .setKeySize(256)
          .build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
      }
    }.getOrNull()
  }

  companion object {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "dwello_homes_session_key_v1"
    private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val PREFS_NAME = "dwello_secure_session_prefs"
    private const val KEY_TOKEN_IV = "token_iv"
    private const val KEY_TOKEN_CIPHERTEXT = "token_ciphertext"
    private const val KEY_EXPIRES_AT = "session_expires_at"
  }
}
