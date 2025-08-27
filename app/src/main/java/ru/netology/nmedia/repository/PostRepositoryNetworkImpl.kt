package ru.netology.nmedia.repository


import ru.netology.nmedia.dto.Post
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import java.lang.RuntimeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class PostRepositoryNetworkImpl: PostRepository {

    override fun getAll(): List<Post> {
        val response = PostsApi.retrofitService.getAll().execute()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        PostsApi.retrofitService.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }

                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                val errorMessage = when (t) {
                    is SocketTimeoutException -> "Таймаут соединения с сервером"
                    is ConnectException -> "Нет подключения к интернету"
                    is UnknownHostException -> "Сервер не найден"
                    else -> "Сервер не отвечает"
                }
                callback.onNetworkError(errorMessage)
            }
        })
    }

    override fun likeById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override fun unlikeById(id: Long): Post {
        TODO("Not yet implemented")
    }


    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun viewById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun save(post: Post) {
        PostsApi.retrofitService.save(post)
            .execute()
    }

    override fun removeById(id: Long) {
        TODO("Not yet implemented")
    }
}


