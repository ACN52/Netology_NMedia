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

        viewModel.data.observe(this){post->

            // ==================
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
                binding.textCountShare.text = formatNumberShort(post.sharesCount)
            }

            fun updateLookUI() {
                binding.textCountLook.text = formatNumberShort(post.looksCount)
            }
            post.looksCount++
            updateLookUI()
            // ==================

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

    }

}