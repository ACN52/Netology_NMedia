package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryFileImpl


// пакет ViewModel отвечает за хранение данных, относящихся к UI, и за
// связывание UI с бизнес-логикой

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    video = ""
    )

class PostViewModel(application: Application): AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryFileImpl(application)
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun like(id: Long) = repository.like(id)
    fun share(id: Long) = repository.share(id)
    fun view(id: Long) = repository.view(id)
    fun removeById(id: Long) = repository.removeById(id)

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

    // метод для сброса редактирования поста
    fun cancelEdit() {
        edited.value = empty // Сбрасываем на "пустой" пост
    }
}