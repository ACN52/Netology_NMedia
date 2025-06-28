package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractorListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel


// пакет Activity только управляет UI и взаимодействием с пользователем

class FeedFragment : Fragment() {

    val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = PostAdapter(
            object : OnInteractorListener {

                override fun onLike(post: Post) {
                    viewModel.likeById(post.id)
                }

                override fun onShare(post: Post) {
                    viewModel.shareById(post.id)   // Увеличиваем счетчик в репозитории
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val intent2 = Intent.createChooser(intent, getString(R.string.chooser_share_post))

                    startActivity(intent2)
                }

                override fun onView(post: Post) {
                    viewModel.viewById(post.id)
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onEdit(post: Post) {
                    viewModel.edit(post)  // сохраняем пост в ViewModel
                    findNavController().navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply {
                            putString(textArgs, post.content)
                        }
                    )
                }

                override fun onUpdate(post: Post) {
                    viewModel.edit(post) // Обновляем пост в ViewModel
                    findNavController().navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply {
                            putString(textArgs, post.content) // Передаем текст поста
                        }
                    )
                }

                // Обрабатываем функцию клика на текст поста
                override fun onContentClicked(post: Post) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_postCardFragment,
                        Bundle().apply { putLong("postId", post.id) }
                    )
                }


            }
        )


        binding.recyclerId.adapter = adapter

        // Обновление списка постов в RecyclerView при изменении данных в LiveData
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val isNew = posts.size != adapter.itemCount
            adapter.submitList(posts) {
                if (isNew) {
                    binding.recyclerId.smoothScrollToPosition(0)
                }
            }
        }

        binding.buttonFab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }


        return binding.root
    }

    companion object {
        var Bundle.textArgs by StringArg

    }
}