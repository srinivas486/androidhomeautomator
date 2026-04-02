package com.skillfield.androidhomeautomator.core.network

import com.skillfield.androidhomeautomator.core.storage.CredentialsManager
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideNetworkClientProvider(
        moshi: Moshi
    ): NetworkClientProvider {
        return object : NetworkClientProvider {
            override fun createRetrofit(
                baseUrl: String,
                credentialsManager: CredentialsManager
            ): Retrofit {
                val authInterceptor = Interceptor { chain ->
                    val username = credentialsManager.getUsername()
                    val password = credentialsManager.getPassword()
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        val credentials = Credentials.basic(username, password)
                        val request = chain.request().newBuilder()
                            .header("Authorization", credentials)
                            .build()
                        chain.proceed(request)
                    } else {
                        chain.proceed(chain.request())
                    }
                }

                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()

                return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
            }
        }
    }
}
