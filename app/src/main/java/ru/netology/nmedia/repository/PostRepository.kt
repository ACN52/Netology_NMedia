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
    fun save(post: Post): Post

    fun getAllAsync(callback: GetAllCallback)
    fun likeByIdAsync(id: Long, callback: ActionCallback)
    fun unlikeByIdAsync(id: Long, callback: ActionCallback)
    fun shareByIdAsync(id: Long, callback: ActionCallback)
    fun viewByIdAsync(id: Long, callback: ActionCallback)
    fun removeByIdAsync(id: Long, callback: ActionCallback)
    fun saveAsync(post: Post, callback: ActionCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
        fun onNetworkError(message: String) {}
    }

    interface ActionCallback {
        fun onSuccess() {}
        fun onSuccess(post: Post) {}
        fun onError(e: Exception) {}
        fun onNetworkError(message: String) {}
    }

}