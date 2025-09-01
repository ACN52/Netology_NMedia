package ru.netology.nmedia.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetworkImpl
import ru.netology.nmedia.util.SingleLiveEvent
//import ru.netology.nmedia.repository.PostRepositoryRoomImpl

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

    private val repository: PostRepository = PostRepositoryNetworkImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    // Добавляем SingleLiveEvent для ошибок
    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String>
        get() = _errorEvent

    private val _networkErrorEvent = SingleLiveEvent<String>()
    val networkErrorEvent: LiveData<String>
        get() = _networkErrorEvent

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                // Показываем Toast об ошибке
                showToast("Ошибка загрузки: ${e.message}")
                _data.postValue(FeedModel(error = true))
            }

            // Сервер не отвечает
            // Сообщение в showToast(message) подставляется из класса class PostRepositoryNetworkImpl
            // из метода fun onFailure в зависимости от ошибки
            override fun onNetworkError(message: String) {
                _data.postValue(FeedModel(error = true))
                showToast(message)
            }
        })
    }

    fun save() {
        edited.value?.let { post ->
            repository.saveAsync(post, object : PostRepository.ActionCallback {
                override fun onSuccess(post: Post) {
                    _postCreated.postValue(Unit)
                    loadPosts()
                    showToast("Пост сохранен")
                }

                override fun onError(e: Exception) {
                    showToast("Ошибка сохранения: ${e.message}")
                }

                override fun onNetworkError(message: String) {
                    showToast(message)
                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(postId: Long) {
        // Находим пост по ID
        val post = _data.value?.posts?.find { it.id == postId }
        post?.let {
            if (it.likedByMe) {
                repository.unlikeByIdAsync(it.id, object : PostRepository.ActionCallback {
                    override fun onSuccess(post: Post) {
                        loadPosts()
                        showToast("Лайк убран")
                    }
                    override fun onError(e: Exception) {
                        showToast("Ошибка: ${e.message}")
                    }
                    override fun onNetworkError(message: String) {
                        showToast(message)
                    }
                })
            } else {
                repository.likeByIdAsync(it.id, object : PostRepository.ActionCallback {
                    override fun onSuccess(post: Post) {
                        loadPosts()
                        showToast("Лайк поставлен")
                    }
                    override fun onError(e: Exception) {
                        showToast("Ошибка: ${e.message}")
                    }
                    override fun onNetworkError(message: String) {
                        showToast(message)
                    }
                })
            }
        }
    }

//    fun likeById(id: Long) {
//        repository.likeByIdAsync(id, object : PostRepository.ActionCallback {
//            override fun onSuccess(post: Post) {
//                loadPosts() // Обновляем список после лайка
//                showToast("Лайк поставлен")
//            }
//
//            override fun onError(e: Exception) {
//                showToast("Ошибка в постановке Like: ${e.message}")
//            }
//
//            override fun onNetworkError(message: String) {
//                showToast(message)
//            }
//        })
//    }

//    fun unlikeById(id: Long) {
//        repository.unlikeByIdAsync(id, object : PostRepository.ActionCallback {
//            override fun onSuccess(post: Post) {
//                loadPosts() // Обновляем список после снятия лайка
//                showToast("Лайк убран")
//            }
//
//            override fun onError(e: Exception) {
//                showToast("Ошибка в постановке DisLike: ${e.message}")
//            }
//
//            override fun onNetworkError(message: String) {
//                showToast(message)
//            }
//        })
//    }

    fun shareById(id: Long) {
        repository.shareByIdAsync(id, object : PostRepository.ActionCallback {
            override fun onSuccess() {
                loadPosts() // Обновляем счетчик шаров
                showToast("Поделились постом")
            }

            override fun onError(e: Exception) {
                showToast("Ошибка: ${e.message}")
            }

            override fun onNetworkError(message: String) {
                showToast(message)
            }
        })
    }

    fun viewById(id: Long) {
        repository.viewByIdAsync(id, object : PostRepository.ActionCallback {
            override fun onSuccess(post: Post) {
                loadPosts() // Обновляем список после view
                showToast("Просмотрели пост")
            }

            override fun onError(e: Exception) {
                showToast("Ошибка в просмотре поста: ${e.message}")
            }

            override fun onNetworkError(message: String) {
                showToast(message)
            }
        })
    }

    fun removeById(id: Long) {
        val oldPosts = _data.value?.posts.orEmpty()

        // Оптимистичное обновление UI
        _data.postValue(_data.value?.copy(posts = oldPosts.filter { it.id != id }))

        repository.removeByIdAsync(id, object : PostRepository.ActionCallback {
            override fun onSuccess() {
                showToast("Пост удален")
            }

            override fun onError(e: Exception) {
                // Откатываем изменения при ошибке
                _data.postValue(_data.value?.copy(posts = oldPosts))
                showToast("Ошибка удаления: ${e.message}")
            }

            override fun onNetworkError(message: String) {
                // Откатываем изменения при сетевой ошибке
                _data.postValue(_data.value?.copy(posts = oldPosts))
                showToast(message)
            }
        })
    }

    // Функция безопасного отображения Toast из фонового потока в главном UI-потоке
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show()
        }
    }
}