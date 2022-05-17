package com.ellcie.backofficelibrary.network.api

import com.ellcie.backofficelibrary.network.model.CustomToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.QueryMap

interface EllcieApi{

}

interface BackOfficeApi : EllcieApi {

    @GET("/fbauth/token")
    suspend fun getCustomToken(): CustomToken
    // Cgu urls call
    @GET("/firebase/me/user/cgu/driver")
    suspend fun checkNewCguDriver(): ResponseBody

    @GET("/firebase/infos/cgus/driver")
    fun getLatestCguDriver(): ResponseBody

    @PUT("/firebase/me/user/cgu")
    suspend fun validateCgu(@Body jsonObject: RequestBody?): ResponseBody

    @GET("/firebase/me/user")
    suspend fun getLastValidatedCgu(): ResponseBody

    @GET("/firebase/cgus?")
    suspend fun getValidatedCguUrl(@QueryMap params: Map<String?, String?>?): ResponseBody


    @GET("/firebase/me/user/cgu/fall-assisted")
    suspend fun checkNewCguSerenity(): ResponseBody

    @GET("/firebase/infos/cgus/fall-assisted")
    suspend fun getLatestCguSerenity(): ResponseBody


    @POST("/firebase/fallprevention")
    @Multipart
    suspend fun uploadPreventionFile(
        @Part("description") description: RequestBody?,
        @Part file: Part?
    ): ResponseBody
}