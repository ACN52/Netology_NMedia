package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.formatNumberShort
import ru.netology.nmedia.viewmodel.PostViewModel

class PostCardFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CardPostBinding.inflate(   // Получаем данные с разметки card_post.xml
            inflater,
            container,
            false
        )

        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Получаем переданный ID поста
        val postId = arguments?.getLong("postId") ?: 0L

        // Загружаем данные поста из ViewModel
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            posts.find { it.id == postId }?.let { post ->
                with(binding) {
                    // Текстовые поля
                    textAuthor.text = post.author
                    textContent.text = post.content
                    textPublished.text = post.published
                    //textVideo.text = post.video

                    imageHeart.apply {
                        isChecked = post.likedByMe
                        text = formatNumberShort(post.likesCount)
                    }
                    imageShare.text = formatNumberShort(post.sharesCount)
                    imageLook.text = formatNumberShort(post.looksCount)
                }
            }
        }
        return binding.root
    }
}