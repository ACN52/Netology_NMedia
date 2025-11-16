package ru.netology.nmedia.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.viewmodel.AuthData

// Сервис для постов
interface PostsApiService {
//    @GET("slow/posts")
//    suspend fun getAll(): Response<List<Post>>

    @GET("slow/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("slow/posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("slow/posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("slow/posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count")count: Int): Response<List<Post>>

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

    @POST("users/push-token")
    suspend fun savePushToken(@Body pushToken: PushToken): Response<Unit>
}