package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post


// пакет Repository отвечает за хранение и управление данными


class PostRepositoryInMemoryImpl: PostRepository {

    private val data = MutableLiveData<Post>()

    override fun get(): LiveData<Post> = data

    // ----------------
    // создаем объект класса Post
    private var post = Post(
        id = 1,
        author = "Нетология. Университет интернет-профессий будущего",
        content = "Привет, это новая Нетология! Когда-то Нетология начиналась" +
                " с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну," +
                " разработке, аналитике и управлению. Мы растём сами и помогаем расти" +
                " студентам: от новичков до уверенных профессионалов." +
                " Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила," +
                " которая заставляет хотеть больше, целиться выше, бежать быстрее." +
                " Наша миссия — помочь встать на путь роста и начать цепочку перемен" +
                " → http://netolo.gy/fyb",
        published = "21 мая в 18:36"
    )

        // сеттер для автоматического обновления LiveData при изменении post
        set(value) {
            field = value   // сохраняем новое значение в поле
            data.value = value // обновляем LiveData
        }
    // ----------------

    // ----------------
    override fun like() {
        post = post.copy(
            likeByMe = !post.likeByMe,
            likesCount = if (!post.likeByMe) post.likesCount + 1 else post.likesCount - 1
        )
    }

    override fun share() {
        post = post.copy(sharesCount = post.sharesCount + 1)
    }

    override fun view() {
        post = post.copy(looksCount = post.looksCount + 1)
    }

}