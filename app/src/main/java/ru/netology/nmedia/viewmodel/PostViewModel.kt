package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositorySQLiteImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likesCount = 0,
    sharesCount = 0,
    looksCount = 0,
    likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositorySQLiteImpl(
        AppDb.getInstance(application).postDao
    )
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun removeById(id: Long) = repository.removeById(id)
    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun viewById(id: Long) = repository.viewById(id)

    fun changeContent(content: String) {
        val text = content.trim()
        edited.value.let {
            if (it != null) {
                if(text == it.content) {
                    return@let
                }
                edited.value = it.copy(content = text)
            }
        }
    }

        fun save() {
        edited.value.let {
            if (it != null) {
                repository.save(it)
            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

//    // метод для сброса редактирования поста
//    fun cancelEdit() {
//        edited.value = empty // Сбрасываем на "пустой" пост
//    }
}