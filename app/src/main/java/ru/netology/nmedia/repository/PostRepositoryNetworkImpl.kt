package ru.netology.nmedia.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import ru.netology.nmedia.activity.PostPagingSource
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.entity.PostEntity
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.collections.map
import kotlin.random.Random

class PostRepositoryNetworkImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostsApiService
) : PostRepository {

    private val factory = InvalidatingPagingSourceFactory {
        PostPagingSource(apiService, dao)
    }

    private val pager = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = factory,
    )

    override val data = pager.flow
        .map { pagingData -> pagingData.map { it.toDto() } }
        .map { pagingData -> pagingData.map { it as FeedItem } }

    // LiveData для передачи ошибок в UI
    private val _errorMessage = MutableLiveData<String?>()
    override val errorMessage: LiveData<String?> = _errorMessage

    // Метод для принудительного обновления
    override suspend fun refresh() {
        factory.invalidate()
    }

    override suspend fun getAllAsync() {
        // Для Paging 3 это не нужно, так как данные загружаются автоматически
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            try {
                val response = apiService.getNewer(id)
                if (response.isSuccessful) {
                    val newPosts = response.body() ?: emptyList()
                    if (newPosts.isNotEmpty()) {
                        Log.d("Network", "Найдено ${newPosts.size} новых постов")
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
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            handleNetworkError(e, "likeById")
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            val updatedPost = apiService.unlikeById(id)
            dao.insert(PostEntity.fromDto(updatedPost))
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            handleNetworkError(e, "unlikeById")
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            apiService.removeById(id)
            dao.removeById(id)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            handleNetworkError(e, "removeById")
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

    override fun isEmpty() = dao.isEmpty()

    override fun clearError() {
        _errorMessage.postValue(null)
    }

    override suspend fun makeAllPostsVisible() {
        dao.makeAllVisible()
    }

    private fun handleNetworkError(e: Exception, context: String) {
        val message = getNetworkErrorMessage(e)
        Log.e("PostRepositoryNetworkImpl", "Network error in $context: ${e.message}")
        _errorMessage.postValue(message)
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


