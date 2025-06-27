package com.example.lightningtracker.di

import android.annotation.SuppressLint
import com.example.lightningtracker.data.remote.LightningApiClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .followRedirects(true)
            .pingInterval(30, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Provides
    @Singleton
    @Named("WebSocketClient")
    fun provideWebSocketClient(
        okHttpClient: OkHttpClient,
        trustManager: X509TrustManager
    ): OkHttpClient {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return okHttpClient.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .followRedirects(false)
            .hostnameVerifier { hostname, _ ->
                hostname == "ws1.blitzortung.org" ||
                        hostname == "ws2.blitzortung.org" ||
                        hostname == "ws3.blitzortung.org" ||
                        hostname == "ws4.blitzortung.org" ||
                        hostname == "map.blitzortung.org"
            }
            .sslSocketFactory(sslSocketFactory, trustManager)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(LightningApiClient.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideLightningApiClient(retrofit: Retrofit): LightningApiClient {
        return retrofit.create(LightningApiClient::class.java)
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Provides
    @Singleton
    fun provideTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    }
} 