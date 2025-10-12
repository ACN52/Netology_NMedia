package ru.netology.nmedia.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class PostRepositoryNetworkImpl(private val dao: PostDao) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map(PostEntity::toDto)
    }

    // LiveData для передачи ошибок в UI
    private val _errorMessage = MutableLiveData<String?>()
    override val errorMessage: LiveData<String?> = _errorMessage

    override suspend fun getAllAsync() {
        try {
            val posts = PostsApi.retrofitService.getAll()
            dao.insert(posts.map(PostEntity::fromDto))
            _errorMessage.postValue(null) // Очищаем ошибку при успехе
        } catch (e: Exception) {
            // Создаем локальную переменную с другим именем
            val message = getNetworkErrorMessage(e)
            // Просто логируем ошибки
            Log.e("PostRepositoryNetworkImpl", "Network error in getAllAsync: ${e.message}")
            // Данные остаются из БД - приложение работает
            // Передаем сообщение для Toast
            _errorMessage.postValue(message)
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val updatedPost = PostsApi.retrofitService.likeById(id)
            dao.insert(PostEntity.fromDto(updatedPost))
            _errorMessage.postValue(null) // Очищаем ошибку при успехе
        } catch (e: Exception) {
            val message = getNetworkErrorMessage(e)
            Log.e("PostRepositoryNetworkImpl", "Network error in likeById: ${e.message}")
            // Данные остаются из БД - приложение работает
            _errorMessage.postValue(message)
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            val updatedPost = PostsApi.retrofitService.unlikeById(id)
            dao.insert(PostEntity.fromDto(updatedPost))
            _errorMessage.postValue(null) // Очищаем ошибку при успехе
        } catch (e: Exception) {
            val message = getNetworkErrorMessage(e)
            Log.e("PostRepositoryNetworkImpl", "Network error in unlikeById: ${e.message}")
            // Данные остаются из БД - приложение работает
            _errorMessage.postValue(message)
        }
    }


    override suspend fun removeById(id: Long) {
        try {
            PostsApi.retrofitService.removeById(id)
            dao.removeById(id)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            val message = getNetworkErrorMessage(e)
            Log.e("PostRepositoryNetworkImpl", "Network error in removeById: ${e.message}")
            _errorMessage.postValue(message)
        }
    }

    override suspend fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun viewById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun save(post: Post): Post {
        try {
            val postFromServer = PostsApi.retrofitService.save(post)

            dao.insert(PostEntity.fromDto(postFromServer))

            return postFromServer

        } catch (e: Exception) {
            throw UnknownError(getNetworkErrorMessage(e))
        }
    }

    override fun isEmpty()= dao.isEmpty()

    override fun clearError() {
        _errorMessage.postValue(null)
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


