package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
//import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://192.168.1.7:9999/api/slow/"

//private val logging = HttpLoggingInterceptor().apply {
//    if (BuildConfig.DEBUG) {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//}

private val okhttp = OkHttpClient.Builder()
    .connectTimeout(13, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(okhttp)
    .build()

interface PostsApiService {
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @GET("posts/{id}")
    fun getById(@Path("id") id: Long): Call<Post>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("posts/{id}")
    fun removeById(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Post>

    @DELETE("posts/{id}/likes")
    fun unlikeById(@Path("id") id: Long): Call<Post>

    @POST("posts/{id}/shares")
    fun shareById(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/views")
    fun viewById(@Path("id") id: Long): Call<Unit>
}

object PostsApi {
    val retrofitService: PostsApiService by lazy {
        retrofit.create(PostsApiService::class.java)
    }
}