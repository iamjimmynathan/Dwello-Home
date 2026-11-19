package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

data class PreparedUploadImage(
  val fileName: String,
  val mimeType: String,
  val bytes: ByteArray
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PreparedUploadImage) return false
    return fileName == other.fileName && mimeType == other.mimeType && bytes.contentEquals(other.bytes)
  }

  override fun hashCode(): Int {
    var result = fileName.hashCode()
    result = 31 * result + mimeType.hashCode()
    result = 31 * result + bytes.contentHashCode()
    return result
  }
}

interface PropertyImageDataSource {
  suspend fun readImageForUpload(photo: SelectedPhotoItem, index: Int): Result<PreparedUploadImage>
}

/**
 * In-memory image reader for JVM/unit tests.
 */
class InMemoryPropertyImageDataSource(
  private val fixtures: Map<String, PreparedUploadImage> = emptyMap(),
  private val fallbackSampleBytes: ByteArray? = null
) : PropertyImageDataSource {
  override suspend fun readImageForUpload(photo: SelectedPhotoItem, index: Int): Result<PreparedUploadImage> {
    fixtures[photo.uriString]?.let { return Result.success(it) }
    val sample = fallbackSampleBytes
    if (sample != null && sample.isNotEmpty()) {
      val name = photo.fileName?.takeIf { it.isNotBlank() } ?: "property_${index + 1}.jpg"
      val mime = photo.mimeType?.takeIf { it.isNotBlank() } ?: "image/jpeg"
      return Result.success(PreparedUploadImage(fileName = name, mimeType = mime, bytes = sample))
    }
    return Result.failure(IllegalStateException("No image bytes available for ${photo.uriString}"))
  }
}

/**
 * Reads selected Android Photo Picker (content://) or local file URIs via ContentResolver,
 * normalizes large camera images or HEIC photos to JPEG when decodable, and preserves valid raw image bytes.
 */
class AndroidContentResolverImageDataSource(
  context: Context
) : PropertyImageDataSource {
  private val appContext = context.applicationContext

  override suspend fun readImageForUpload(photo: SelectedPhotoItem, index: Int): Result<PreparedUploadImage> {
    return runCatching {
      val uri = Uri.parse(photo.uriString)
      val rawBytes = readUriBytes(uri)
        ?: error("Unable to read selected photo. Please select the image again.")
      if (rawBytes.isEmpty()) {
        error("Selected photo file is empty.")
      }

      val detectedMime = resolveMimeType(uri, photo.mimeType)
      val baseName = resolveFileName(uri, photo.fileName, index)

      val shouldNormalizeToJpeg = detectedMime in setOf("image/heic", "image/heif") ||
        rawBytes.size > MAX_RAW_PASSTHROUGH_BYTES

      if (shouldNormalizeToJpeg) {
        val compressed = tryReencodeAsJpeg(rawBytes)
        if (compressed != null && compressed.isNotEmpty()) {
          val jpegName = replaceExtensionWithJpg(baseName)
          return@runCatching PreparedUploadImage(
            fileName = jpegName,
            mimeType = "image/jpeg",
            bytes = compressed
          )
        }
      }

      val finalMime = detectedMime ?: guessMimeFromFileName(baseName) ?: "image/jpeg"
      val finalName = ensureExtension(baseName, finalMime)
      PreparedUploadImage(
        fileName = finalName,
        mimeType = finalMime,
        bytes = rawBytes
      )
    }
  }

  private fun readUriBytes(uri: Uri): ByteArray? {
    if (uri.scheme.equals("file", ignoreCase = true)) {
      val path = uri.path ?: return null
      val file = File(path)
      if (!file.exists() || !file.canRead()) return null
      return file.readBytes()
    }
    return appContext.contentResolver.openInputStream(uri)?.use { input ->
      input.readBytes()
    }
  }

  private fun resolveMimeType(uri: Uri, hintMime: String?): String? {
    val resolverMime = runCatching { appContext.contentResolver.getType(uri) }.getOrNull()
    val raw = (resolverMime ?: hintMime)?.trim()?.lowercase(Locale.ROOT)
    return when (raw) {
      "image/jpg" -> "image/jpeg"
      else -> raw?.takeIf { it.startsWith("image/") }
    }
  }

  private fun resolveFileName(uri: Uri, hintName: String?, index: Int): String {
    val queriedName = runCatching {
      if (uri.scheme.equals("content", ignoreCase = true)) {
        appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
          ?.use { cursor ->
            if (cursor.moveToFirst()) {
              val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
              if (col >= 0) cursor.getString(col) else null
            } else {
              null
            }
          }
      } else {
        null
      }
    }.getOrNull()

    val raw = queriedName?.takeIf { it.isNotBlank() }
      ?: hintName?.takeIf { it.isNotBlank() }
      ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
      ?: "property_photo_${index + 1}.jpg"

    return raw.replace(Regex("[^A-Za-z0-9._-]"), "_")
  }

  private fun tryReencodeAsJpeg(rawBytes: ByteArray): ByteArray? {
    return runCatching {
      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
      if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

      var sampleSize = 1
      while (bounds.outWidth / sampleSize > MAX_DIMENSION_PX || bounds.outHeight / sampleSize > MAX_DIMENSION_PX) {
        sampleSize *= 2
      }

      val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
      val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts) ?: return null
      try {
        val scaled = scaleDownIfNeeded(decoded)
        try {
          val out = ByteArrayOutputStream()
          scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
          out.toByteArray()
        } finally {
          if (scaled !== decoded) {
            scaled.recycle()
          }
        }
      } finally {
        decoded.recycle()
      }
    }.getOrNull()
  }

  private fun scaleDownIfNeeded(bitmap: Bitmap): Bitmap {
    val maxSide = maxOf(bitmap.width, bitmap.height)
    if (maxSide <= MAX_DIMENSION_PX) return bitmap
    val ratio = MAX_DIMENSION_PX.toFloat() / maxSide.toFloat()
    val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
    val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
  }

  private fun replaceExtensionWithJpg(name: String): String {
    val dotIdx = name.lastIndexOf('.')
    val stem = if (dotIdx > 0) name.substring(0, dotIdx) else name
    return "$stem.jpg"
  }

  private fun guessMimeFromFileName(name: String): String? {
    return when (name.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
      "jpg", "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "webp" -> "image/webp"
      "heic" -> "image/heic"
      "heif" -> "image/heif"
      else -> null
    }
  }

  private fun ensureExtension(name: String, mimeType: String): String {
    if (name.contains('.')) return name
    val ext = when (mimeType) {
      "image/png" -> "png"
      "image/webp" -> "webp"
      else -> "jpg"
    }
    return "$name.$ext"
  }

  companion object {
    private const val MAX_RAW_PASSTHROUGH_BYTES = 1_500_000
    private const val MAX_DIMENSION_PX = 1600
    private const val JPEG_QUALITY = 85
  }
}
