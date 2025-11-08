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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractorListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var binding: FragmentFeedBinding
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)

        setupAdapter()
        setupObservers()
        setupSwipeRefresh()
        setupFab()
        setupNewPostsNotification()

        return binding.root
    }

    private fun setupAdapter() {
        adapter = PostAdapter(object : OnInteractorListener {
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
                if (post.likedByMe) {
                    viewModel.unlikeById(post.id)
                } else {
                    viewModel.likeById(post.id)
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
                val shareIntent = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onView(post: Post) {
                // TODO
            }
        })

        // Настраиваем адаптер с заголовком и футером для PREPEND и APPEND
        binding.recyclerId.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() }, // ДЛЯ PREPEND
            footer = PostLoadingStateAdapter { adapter.retry() }    // ДЛЯ APPEND
        )
    }

    private fun setupObservers() {
        // Наблюдаем за данными
        lifecycleScope.launch {
            viewModel.data
                .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
                .collectLatest {
                    adapter.submitData(it)
                }
        }

        // Наблюдаем за состояниями загрузки
        lifecycleScope.launch {
            adapter.loadStateFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { loadState ->
                    handleLoadState(loadState)
                }
        }

        // Наблюдаем за ошибками
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun handleLoadState(loadState: CombinedLoadStates) {
        // REFRESH состояние - показываем в SwipeRefreshLayout
        binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading

        // Показываем/скрываем кнопку повтора при ошибке
        binding.retryButton.isVisible = loadState.refresh is LoadState.Error

        // Обработка ошибок
        when (val refreshState = loadState.refresh) {
            is LoadState.Error -> {
                binding.retryButton.setOnClickListener { adapter.retry() }
            }
            else -> {
                // Другие состояния
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh() // Запускаем REFRESH через адаптер
        }
    }

    private fun setupFab() {
        binding.buttonFab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }
    }

    private fun setupNewPostsNotification() {
        viewModel.showNewPostsNotification.observe(viewLifecycleOwner) { showNotification ->
            binding.newPostsNotification.isVisible = showNotification
        }

        binding.newPostsNotification.setOnClickListener {
            viewModel.loadNewPosts()
            binding.recyclerId.smoothScrollToPosition(0)
        }
    }

    companion object {
        var Bundle.textArgs by StringArg
    }
}