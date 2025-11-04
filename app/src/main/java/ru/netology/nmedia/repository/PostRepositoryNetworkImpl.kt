package ru.netology.nmedia.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.collections.map

class PostRepositoryNetworkImpl @Inject constructor(
    private val apiService: PostsApiService,
    private val dao: PostDao,
    //private val refreshTrigger: Flow<Unit>,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : PostRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
            prefetchDistance = 2   // Загружаем следующую страницу за 2 элемента до конца
        ),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(apiService, dao, refreshTrigger, postRemoteKeyDao, appDb)
    ).flow
        .map { it.map(PostEntity::toDto) }

    // Метод для принудительного обновления
    override suspend fun refresh() {
        refreshTrigger.emit(Unit)
    }

    // Используем только видимые посты для основного списка
    //override val data = dao.getAllVisible().map { it.map { it.toDto() } }

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
            val response = apiService.getAll() // получаем Response

            if (response.isSuccessful) {
                val posts = response.body() ?: emptyList()
                Log.d("Network", "Получено ${posts.size} сообщений с сервера")

                // Сохраняем посты как видимые при первоначальной загрузке
                dao.insertAll(posts.map { post ->
                    PostEntity.fromDto(post).copy(isVisible = true)
                })
                _errorMessage.postValue(null)
            } else {
                // Обработка HTTP ошибок
                val errorMessage = "HTTP error: ${response.code()} - ${response.message()}"
                Log.e("Network", errorMessage)
                _errorMessage.postValue(errorMessage)
            }
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
                        dao.insertAll(newEntities)
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


