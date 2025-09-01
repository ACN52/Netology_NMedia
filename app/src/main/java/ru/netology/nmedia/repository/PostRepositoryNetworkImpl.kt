package ru.netology.nmedia.repository


import android.util.Log
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
        val response = PostsApi.retrofitService.likeById(id).execute()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("Пустое тело ответа")
        } else {
            throw RuntimeException("Ошибка в постановке Like: ${response.message()}")
        }
    }

    override fun likeByIdAsync(id: Long, callback: PostRepository.ActionCallback) {
        PostsApi.retrofitService.likeById(id).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body() ?: throw RuntimeException("Пустое тело ответа"))
                } else {
                    callback.onError(RuntimeException("Ошибка в постановке Like: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onNetworkError(getNetworkErrorMessage(t))
            }
        })
    }

    override fun unlikeById(id: Long): Post {
        val response = PostsApi.retrofitService.unlikeById(id).execute()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("Пустое тело ответа")
        } else {
            throw RuntimeException("Ошибка в постановке DisLike: ${response.message()}")
        }
    }

    override fun unlikeByIdAsync(id: Long, callback: PostRepository.ActionCallback) {
        PostsApi.retrofitService.unlikeById(id).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body() ?: throw RuntimeException("Пустое тело ответа"))
                } else {
                    callback.onError(RuntimeException("Ошибка в постановке DisLike: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onNetworkError(getNetworkErrorMessage(t))
            }
        })
    }


    override fun shareById(id: Long) {
        val response = PostsApi.retrofitService.shareById(id).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Ошибка шаринга: ${response.message()}")
        }
    }

    override fun shareByIdAsync(id: Long, callback: PostRepository.ActionCallback) {
        PostsApi.retrofitService.shareById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(RuntimeException("Ошибка шаринга: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onNetworkError(getNetworkErrorMessage(t))
            }
        })
    }

    override fun viewById(id: Long) {
        try {
            val response = PostsApi.retrofitService.viewById(id).execute()
            if (!response.isSuccessful) {
                Log.e("ViewById", "Server error: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ViewById", "Error in viewById: ${e.message}")
        }
    }

    override fun viewByIdAsync(id: Long, callback: PostRepository.ActionCallback) {
        Log.d("PostRepositoryNetworkImplNetwork", "Attempting to call viewById for id: $id")

        try {
            val call = PostsApi.retrofitService.viewById(id)
            Log.d("PostRepositoryNetworkImplNetwork", "Call created: $call")

            call.enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    Log.d("PostRepositoryNetworkImplNetwork", "Response received: ${response.code()} - ${response.message()}")
                    if (response.isSuccessful) {
                        Log.d("PostRepositoryNetworkImplNetwork", "View successful")
                        callback.onSuccess()
                    } else {
                        val errorMsg = "Ошибка просмотра: ${response.code()} - ${response.message()}"
                        Log.e("PostRepositoryNetworkImplNetwork", errorMsg)
                        callback.onError(RuntimeException(errorMsg))
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("PostRepositoryNetworkImplNetwork", "Network failure: ${t.message}", t)
                    callback.onNetworkError(getNetworkErrorMessage(t))
                }
            })
        } catch (e: Exception) {
            Log.e("PostRepositoryNetworkImplNetwork", "Exception in viewByIdAsync: ${e.message}", e)
            callback.onError(e)
        }
    }

    override fun save(post: Post): Post {
        val response = PostsApi.retrofitService.save(post).execute()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("Пустое тело ответа при сохранении")
        } else {
            throw RuntimeException("Ошибка сохранения: ${response.message()}")
        }
    }

    override fun saveAsync(post: Post, callback: PostRepository.ActionCallback) {
        PostsApi.retrofitService.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body() ?: throw RuntimeException("Пустое тело ответа при сохранении"))
                } else {
                    callback.onError(RuntimeException("Ошибка сохранения: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onNetworkError(getNetworkErrorMessage(t))
            }
        })
    }

    override fun removeById(id: Long) {
        val response = PostsApi.retrofitService.removeById(id).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Ошибка удаления: ${response.message()}")
        }
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.ActionCallback) {
        PostsApi.retrofitService.removeById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(RuntimeException("Ошибка удаления: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onNetworkError(getNetworkErrorMessage(t))
            }
        })
    }

    private fun getNetworkErrorMessage(t: Throwable): String {
        return when (t) {
            is SocketTimeoutException -> "Таймаут соединения с сервером"
            is ConnectException -> "Нет подключения к интернету"
            is UnknownHostException -> "Сервер не найден"
            else -> "Сервер не отвечает"
        }
    }
}


