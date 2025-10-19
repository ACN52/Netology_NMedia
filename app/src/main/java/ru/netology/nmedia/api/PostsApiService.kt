package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthData
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://192.168.1.8:9999/api/"

// Общая конфигурация Retrofit
// ---------------------
private val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private val okhttp = OkHttpClient.Builder()
    .addInterceptor(logging)
    .addInterceptor { chain ->
        val token = AppAuth.getInstance().authStateFlow.value.token
        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token") // ← Добавьте Bearer
        }

        return@addInterceptor chain.proceed(requestBuilder.build())
    }
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(okhttp)
    .build()
// ---------------------

// Сервис для постов
interface PostsApiService {
    @GET("slow/posts")
    suspend fun getAll(): List<Post>

    @GET("slow/posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("slow/posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    @POST("slow/posts")
    suspend fun save(@Body post: Post): Post

    @POST("slow/posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("slow/posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Post

    @DELETE("slow/posts/{id}")
    suspend fun removeById(@Path("id") id: Long)

    @POST("slow/posts/{id}/shares")
    suspend fun shareById(@Path("id") id: Long)

    @POST("slow/posts/{id}/views")
    suspend fun viewById(@Path("id") id: Long)
}

// Сервис для аутентификации
interface AuthApiService {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<AuthData>
}

object PostsApi {
    val posts: PostsApiService by lazy {
        retrofit.create(PostsApiService::class.java)
    }

    val auth: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}