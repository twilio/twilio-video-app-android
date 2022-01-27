package com.twilio.video.app.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthService {

    @POST
    suspend fun getToken(
        @Url url: String,
        @Body authServiceRequestDTO: AuthServiceRequestDTO
    ): AuthServiceResponseDTO
}
