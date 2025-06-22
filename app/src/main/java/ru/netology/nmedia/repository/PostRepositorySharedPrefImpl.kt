package ru.netology.nmedia.repository

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostRepositorySharedPrefImpl(context: Context) : PostRepository {

    private val pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    private var nextId = 1L

    val currentTime = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy   HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(currentTime))
    // ----------------

    // создаем объект класса Post
    private var posts = emptyList<Post>()
        set(value) {   // Переопределяем метод set() в переменной posts
            field = value
            data.value = posts
            sync()
        }

    private val data = MutableLiveData(posts)

    // Реализация через SharedPreferens
    init {
        pref.getString(POSTS_KEY, null)?.let { json ->
            posts = gson.fromJson(json, type)
            nextId = (posts.maxOfOrNull {post -> post.id } ?: 0) +1
        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    // ================
    override fun like(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                val newLikeState = !post.likeByMe
                post.copy(
                    likeByMe = newLikeState,
                    likesCount = if (newLikeState) post.likesCount + 1 else post.likesCount - 1
                )
            } else {
                post
            }
        }
        //data.value = posts
        //sync()
    }

    override fun share(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(sharesCount = post.sharesCount + 1)
            } else {
                post
            }
        }
        //data.value = posts
        //sync()
    }

    override fun view(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(looksCount = post.looksCount + 1)
            } else {
                post
            }
        }
        //data.value = posts
        //sync()
    }
    // ================

    override fun removeById(id: Long) {
        posts = posts.filter {it.id != id}
        //data.value = posts
        //sync()
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    published = formattedDate  // Получаем дату в формате "22.06.2024 19:50"
                )
            ) + posts
        } else {
            posts.map {
                if (post.id == it.id) {
                    it.copy(content = post.content)
                } else it
            }
        }
        //data.value = posts
        //sync()
    }

    // Реализация через SharedPreferens
    // Сохранение данных в SharedPreferens
    private fun sync() {
        pref.edit {
            putString(POSTS_KEY, gson.toJson(posts, type))
        }
    }

    // Создаем константы
    companion object {
        private const val SHARED_PREF_NAME = "repo"
        private const val POSTS_KEY = "posts"
        private val gson = Gson()
        //private val type = TypeToken.getParameterized(List::class.java, Post::class.java)
        private val type = object : TypeToken<List<Post>>() {}.type
    }

}