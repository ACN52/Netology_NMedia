package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post


// Интерфейс определяет контракт (API) для работы с данными постов, скрывая конкретную реализацию


interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun like(id: Long)
    fun share(id: Long)
    fun view(id: Long)

}