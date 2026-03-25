package com.example.myapplication.data.network

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MeloloApi {
    @GET("api/melolo/{feed}")
    suspend fun feed(
        @Path("feed") feed: String,
        @Query("page") page: Int? = null
    ): JsonObject

    @GET("api/melolo/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("page") page: Int? = null
    ): JsonObject

    @GET("api/melolo/detail")
    suspend fun detail(@Query("bookId") bookId: String): JsonObject

    @GET("api/melolo/stream")
    suspend fun stream(@Query("videoId") videoId: String): JsonObject
}
