package com.intellisoft.lhss.network_request

import com.intellisoft.lhss.fhir.data.DbSignIn
import com.intellisoft.lhss.fhir.data.DbSignInResponse
import com.intellisoft.lhss.fhir.data.DbUserInfoResponse
import retrofit2.Response
import retrofit2.http.*


interface Interface {


    @POST("provider/login")
    suspend fun signInUser(
        @Body dbSignIn: DbSignIn
    ): Response<DbSignInResponse>

    @GET("provider/me")
    suspend fun getUserInfo(
        @Header("Authorization") token: String, // Add this line to pass the Bearer Token
    ): Response<DbUserInfoResponse>





}