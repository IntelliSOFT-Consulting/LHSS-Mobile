package com.intellisoft.lhss.network_request

import com.intellisoft.lhss.shared.DbSignIn
import com.intellisoft.lhss.shared.DbSignInResponse
import com.intellisoft.lhss.shared.DbUserInfoResponse
import retrofit2.Call
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
//
////    @GET("provider/reset-password?idNumber={idNumber}&email={email}")
//    @GET("provider/reset-password")
//    suspend fun resetPassword(
//        @Query("idNumber") idNumber:String,
//        @Query("email", encoded = true) email:String,
//    ): Response<DbResetPassword>
//
//    @POST("provider/reset-password")
//    suspend fun setNewPassword(
//        @Body dbSetPasswordReq: DbSetPasswordReq
//    ): Response<Any>




}