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

        // Наблюдаем изменения в списке постов
//        viewModel.data.observe(viewLifecycleOwner) { feedModel ->
//            // Достаем список постов из объекта FeedModel
//            val posts = feedModel.posts ?: emptyList()
//
//            // Используем find(), чтобы найти пост по указанному ID
//            val post = posts.find { it.id == postId }
//
//            if (post != null) {
//                with(binding) {
//                    textAuthor.text = post.author
//                    textContent.text = post.content
//                    textPublished.text = post.published
//
//                    imageHeart.apply {
//                        isChecked = post.likedByMe
//                        text = formatNumberShort(post.likesCount)
//                    }
//                    imageShare.text = formatNumberShort(post.sharesCount)
//                    imageLook.text = formatNumberShort(post.looksCount)
//                }
//            }
//        }
        return binding.root
    }
}