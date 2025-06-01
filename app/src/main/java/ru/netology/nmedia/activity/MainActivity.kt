package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.formatNumberShort
import ru.netology.nmedia.viewmodel.PostViewModel


// пакет Activity только управляет UI и взаимодействием с пользователем


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()

        // Наблюдаем за изменениями данных
        // ========================
        viewModel.data.observe(this) { post ->
            with(binding) {
                textAuthor.text = post.author
                textContent.text = post.content
                textPublished.text = post.published

                // Обновление UI на основе текущего состояния
                imageHeart.setImageResource(
                    if (!post.likeByMe) R.drawable.baseline_favorite_24
                    else R.drawable.baseline_thumb_up_24
                )
                textCountLikes.text = formatNumberShort(post.likesCount)
                textCountShare.text = formatNumberShort(post.sharesCount)
                textCountLook.text = formatNumberShort(post.looksCount)
            }
        }

        // Обработчики кликов
        with(binding) {
            imageHeart.setOnClickListener {
                viewModel.like()
            }

            imageShare.setOnClickListener {
                viewModel.share()
            }
        }

        // Увеличиваем счетчик просмотров при отображении
        viewModel.view()
        // ========================

    }

}

