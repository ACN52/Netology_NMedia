package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.textArgs
import ru.netology.nmedia.databinding.AcManagementBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = AcManagementBinding.inflate(
            inflater,
            container,
            false
        )

        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Устанавливаем текст из edited.value (если есть) или из аргументов
        viewModel.edited.value?.let { post ->
            if (post.id != 0L) {
                binding.editTextCreateUpdate.setText(post.content)
                binding.editTextCreateUpdate.setSelection(post.content.length)
            }
        } ?: arguments?.textArgs?.let { text ->
            binding.editTextCreateUpdate.setText(text)
            binding.editTextCreateUpdate.setSelection(text.length)
        }

        // Отрабатываем кнопку Создать (Create) пост
        // --------------------
        binding.buttonCreate.setOnClickListener {
            if (binding.editTextCreateUpdate.text.isNotBlank()) {
                val content = binding.editTextCreateUpdate.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
            }
            findNavController().navigateUp()
        }
        // --------------------

        // Отрабатываем кнопку Редактировать (Update) пост
        // --------------------
        binding.buttonUpdate.setOnClickListener {
            val content = binding.editTextCreateUpdate.text.toString()
            if (content.isNotBlank()) {
                viewModel.changeContent(content)
                viewModel.save()
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, R.string.error_empty_content, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        findNavController().navigateUp()
                    }.show()
            }
        }
        // --------------------


        return binding.root

    }
}