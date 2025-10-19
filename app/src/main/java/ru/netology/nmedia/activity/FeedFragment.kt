package ru.netology.nmedia.activity


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractorListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostAdapter(object : OnInteractorListener {

            override fun onEdit(post: Post) {
                // TODO
            }

            override fun onUpdate(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onContentClicked(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onLike(post: Post) {
                // ВЫЗЫВАЕМ СООТВЕТСТВУЮЩИЙ МЕТОД В ЗАВИСИМОСТИ ОТ ТЕКУЩЕГО СОСТОЯНИЯ
                if (post.likedByMe) {
                    viewModel.unlikeById(post.id)   // Если уже лайкнуто - убираем лайк
                } else {
                    viewModel.likeById(post.id)     // Если не лайкнуто - ставим лайк
                }
            }

            override fun onRemove(post: Post) {
                // TODO
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onView(post: Post) {
                // TODO
            }
        })

        binding.recyclerId.adapter = adapter

        // Наблюдаем за данными и обновляем UI
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.buttonFab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            println(state)
        }

        // Наблюдаем за уведомлением о новых постах
        // ----------------------------------------
        viewModel.showNewPostsNotification.observe(viewLifecycleOwner) { showNotification ->
            binding.newPostsNotification.isVisible = showNotification
        }

        // Обработка нажатия на плашку
        binding.newPostsNotification.setOnClickListener {
            viewModel.loadNewPosts()
            // Плавный скролл к верху
            binding.recyclerId.smoothScrollToPosition(0)
        }
        // ----------------------------------------

        // Swipe экрана
        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        // ДОБАВЛЯЕМ НАБЛЮДЕНИЕ ЗА ОШИБКАМИ ДЛЯ TOAST
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let { message ->
                // ПОКАЗЫВАЕМ TOAST ПРИ ОШИБКАХ СЕТИ
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                // Очищаем ошибку после показа
                viewModel.clearError()
            }
        }

        return binding.root
    }

    companion object {
        var Bundle.textArgs by StringArg

    }
}