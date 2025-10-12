package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post


// Интерфейс определяет контракт (API) для работы с данными постов, скрывая конкретную реализацию

interface PostRepository {
    val errorMessage: LiveData<String?>
    val data: Flow<List<Post>>
    fun getNewerCount(id: Long): Flow<Int>

    suspend fun getAllAsync()
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun viewById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post
    fun isEmpty(): LiveData<Boolean>
    // Добавляем метод для очистки ошибок
    fun clearError()

    suspend fun makeAllPostsVisible()

}

