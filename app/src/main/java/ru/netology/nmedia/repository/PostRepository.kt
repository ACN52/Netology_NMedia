package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post


// Интерфейс определяет контракт (API) для работы с данными постов, скрывая конкретную реализацию


interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long): Post
    fun unlikeById(id: Long): Post
    fun shareById(id: Long)
    fun viewById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)

    fun getAllAsync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }

}