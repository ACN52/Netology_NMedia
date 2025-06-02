package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl


// пакет ViewModel отвечает за хранение данных, относящихся к UI, и за
// связывание UI с бизнес-логикой


class PostViewModel: ViewModel(){
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    fun like(id: Long) = repository.like(id)
    fun share(id: Long) = repository.share(id)
    fun view(id: Long) = repository.view(id)
}