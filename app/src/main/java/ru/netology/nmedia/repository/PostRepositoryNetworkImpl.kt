package ru.netology.nmedia.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class PostRepositoryNetworkImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostsApiService
) : PostRepository {

    // Используем только видимые посты для основного списка
    override val data = dao.getAllVisible().map { it.map { it.toDto() } }

    // override val data = dao.getAll().map {it.map {it.toDto() }}

    // LiveData для передачи ошибок в UI
    private val _errorMessage = MutableLiveData<String?>()
    override val errorMessage: LiveData<String?> = _errorMessage

    private fun handleNetworkError(e: Exception, context: String) {
        val message = getNetworkErrorMessage(e)
        Log.e("PostRepositoryNetworkImpl", "Network error in $context: ${e.message}")
        // Передаем сообщение для Toast
        _errorMessage.postValue(message)
    }

    override suspend fun getAllAsync() {
        try {
            Log.d("Network", "Выполнение вызова API для получения сообщений")
            val posts = apiService.getAll() // вызов API
            Log.d("Network", "Получено ${posts.size} сообщений с сервера")
            // Сохраняем посты как видимые при первоначальной загрузке
            dao.insert(posts.map { post ->
                PostEntity.fromDto(post).copy(isVisible = true)
            })
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            Log.e("Network", "Ошибка при получении постов: ${e.message}")
            handleNetworkError(e, "getAllAsync")
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            try {
                // ИСПОЛЬЗУЕМ ОПТИМИЗИРОВАННЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ ТОЛЬКО НОВЫХ ПОСТОВ
                val response = apiService.getNewer(id)

                if (response.isSuccessful) {
                    val newPosts = response.body() ?: emptyList()

                    if (newPosts.isNotEmpty()) {
                        Log.d("Network", "Найдено ${newPosts.size} новых постов")
                        // Сохраняем только новые посты как невидимые
                        val newEntities = newPosts.map { post ->
                            PostEntity.fromDto(post).copy(isVisible = false)
                        }
                        dao.insert(newEntities)
                        emit(newPosts.size)
                    } else {
                        emit(0)
                    }
                } else {
                    Log.e("Network", "Ошибка HTTP при получении новых постов: ${response.code()}")
                    emit(0)
                }

                _errorMessage.postValue(null)
            } catch (e: Exception) {
                handleNetworkError(e, "getNewerCount")
                emit(0)
            }
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val updatedPost = apiService.likeById(id)
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
            val updatedPost = apiService.unlikeById(id)
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
            apiService.removeById(id)
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
            val postFromServer = apiService.save(post)
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

    override suspend fun makeAllPostsVisible() {
        dao.makeAllVisible()
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


