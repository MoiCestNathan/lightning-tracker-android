package com.example.lightningtracker.data.remote

import com.example.lightningtracker.data.remote.dto.LightningStrikeDto
import retrofit2.http.GET

interface LightningApiClient {

    @GET("strikes")
    suspend fun getRecentStrikes(): List<LightningStrikeDto>

    companion object {
        const val BASE_URL = "http://api.blitzortung.org/"
    }
} 