package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post


// пакет Repository отвечает за хранение и управление данными


class PostRepositoryInMemoryImpl: PostRepository {

    private var index: Long = 1L
    // ----------------
    // создаем объект класса Post
    private var posts = listOf(
        Post(
            id = index++,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась" +
                    " с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну," +
                    " разработке, аналитике и управлению. Мы растём сами и помогаем расти" +
                    " студентам: от новичков до уверенных профессионалов." +
                    " Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила," +
                    " которая заставляет хотеть больше, целиться выше, бежать быстрее." +
                    " Наша миссия — помочь встать на путь роста и начать цепочку перемен" +
                    " → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            video = ""
        ),
        Post(
            id = index++,
            author = "Нетология_2.",
            content = "Привет, это новая Нетология!",
            published = "02 июня в 18:36",
            video = "https://vkvideo.ru/video-164984229_456239401"
        ),
        Post(
            id = index++,
            author = "Нетология_3.",
            content = "Привет!",
            published = "03 июня в 18:36",
            video =""
        )
    )

    private val data = MutableLiveData(posts)

    // init блок для инициализации LiveData
    init {
        data.value = posts
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
        data.value = posts
    }

    override fun share(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(sharesCount = post.sharesCount + 1)
            } else {
                post
            }
        }
        data.value = posts
    }

    override fun view(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(looksCount = post.looksCount + 1)
            } else {
                post
            }
        }
        data.value = posts
    }
    // ================

    override fun removeById(id: Long) {
        posts = posts.filter {it.id != id}
        data.value = posts
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(
                    id = index++,
                    author = "Me",
                    published = "now"
                )
            ) + posts
        } else {
            posts.map {
                if (post.id == it.id) {
                    it.copy(content = post.content)
                } else it
            }
        }
        data.value = posts
    }

}