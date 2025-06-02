package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.CardPostBinding
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

        val adapter = PostAdapter(
            onLikeListener = { post -> viewModel.like(post.id) },
            onShareListener = { post -> viewModel.share(post.id) },
            onViewListener = { post -> viewModel.view(post.id) }
        )
        binding.recyclerId.adapter = adapter

        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }
    }
}