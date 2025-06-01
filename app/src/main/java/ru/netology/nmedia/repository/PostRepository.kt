package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post


// Интерфейс определяет контракт (API) для работы с данными постов, скрывая конкретную реализацию


interface PostRepository {
    fun get(): LiveData<Post>
    fun like()
    fun share()
    fun view()

}