package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//        // ----------------
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//        // ----------------

        // ----------------
        //  View Binding для безопасного доступа к элементам макета (layout) без findViewById()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ----------------

        // ----------------
        // создаем объект класса Post
        val post = Post(
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
            published = "21 мая в 18:36",
            likeByMe = false
        )
        // ----------------

        // Функция для форматирования чисел в краткую запись
        // ----------------
        fun formatNumberShort(count: Int): String {
            return when {
                count < 1_000 -> count.toString()
                count < 10_000 -> {
                    val hundreds = (count % 1_000) / 100
                    if (hundreds == 0) "${count / 1_000}K"
                    else "${count / 1_000}.${hundreds}K"
                }
                count < 1_000_000 -> "${count / 1_000}K"
                else -> {
                    val hundredThousands = (count % 1_000_000) / 100_000
                    if (hundredThousands == 0) "${count / 1_000_000}M"
                    else "${count / 1_000_000}.${hundredThousands}M"
                }
            }
        }
        // ----------------

        // ----------------
        // Функции для обновления UI
        fun updateLikeUI() {
            with(binding) {
                imageHeart.setImageResource(
                    if (!post.likeByMe) R.drawable.baseline_favorite_24
                    else R.drawable.baseline_thumb_up_24
                )
                textCountLikes.text = formatNumberShort(post.likesCount)
            }
        }

        fun updateShareUI() {
            with(binding) {
                textCountShare.text = formatNumberShort(post.sharesCount)
            }
        }

        fun updateLookUI() {
            binding.textCountLook.text = formatNumberShort(post.looksCount)
        }
        post.looksCount++
        updateLookUI()
        // ----------------

        // Обработчики кликов
        // ==================
        with(binding) {
            textAuthor.text = post.author
            textContent.text = post.content
            textPublished.text = post.published

            // Инициализация состояний UI
            updateLikeUI()
            updateShareUI()

            imageHeart.setOnClickListener {
                post.likeByMe = !post.likeByMe
                if (post.likeByMe) {
                    post.likesCount++
                } else {
                    post.likesCount--
                }
                updateLikeUI()
            }

            imageShare.setOnClickListener {
                post.sharesCount++
                updateShareUI()
            }
        }
        // ==================
    }
      // onStart(): Вызывается когда Activity становится видимым пользователю
//    override fun onStart() {
//        super.onStart()
//        post.looksCount++
//        updateLookUI()
//    }

}