package com.example.network

import android.content.Context
import coil.Coil
import coil.ImageLoader
import com.example.data.SessionTokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object DwelloNetworkConfig {
  const val BASE_URL = "https://dwellohomes-api.jimmynathan11.workers.dev/"
  const val API_HOST = "dwellohomes-api.jimmynathan11.workers.dev"
}

/**
 * Attaches the stored Bearer session token to all authenticated API requests.
 */
class BearerAuthInterceptor(
  private val tokenStore: SessionTokenStore
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    val path = original.url.encodedPath
    val isPublicAuthEndpoint = path.endsWith("/api/auth/login") || path.endsWith("/api/auth/register")

    val token = if (isPublicAuthEndpoint) null else tokenStore.getSessionToken()
    val requestBuilder = original.newBuilder()
      .header("Accept", "application/json")

    if (!token.isNullOrBlank()) {
      requestBuilder.header("Authorization", "Bearer $token")
    }

    return chain.proceed(requestBuilder.build())
  }
}

object DwelloNetworkClient {

  fun createMoshi(): Moshi {
    return Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()
  }

  fun createApiService(
    tokenStore: SessionTokenStore,
    baseUrl: String = DwelloNetworkConfig.BASE_URL,
    extraInterceptors: List<Interceptor> = emptyList()
  ): DwelloApiService {
    val moshi = createMoshi()
    val clientBuilder = OkHttpClient.Builder()
      .addInterceptor(BearerAuthInterceptor(tokenStore))
      .connectTimeout(25, TimeUnit.SECONDS)
      .readTimeout(35, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)

    extraInterceptors.forEach { interceptor ->
      clientBuilder.addInterceptor(interceptor)
    }

    return Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(clientBuilder.build())
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(DwelloApiService::class.java)
  }

  fun configureCoilImageLoader(
    context: Context,
    tokenStore: SessionTokenStore
  ) {
    val imageClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
          .header("Accept", "image/*,*/*;q=0.8")
        if (original.url.host.equals(DwelloNetworkConfig.API_HOST, ignoreCase = true)) {
          val token = tokenStore.getSessionToken()
          if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
          }
        }
        chain.proceed(builder.build())
      }
      .connectTimeout(20, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build()

    val loader = ImageLoader.Builder(context.applicationContext)
      .okHttpClient(imageClient)
      .crossfade(true)
      .build()

    Coil.setImageLoader(loader)
  }
}
