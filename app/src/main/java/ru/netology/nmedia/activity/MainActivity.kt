package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractorListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.formatNumberShort
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel


// пакет Activity только управляет UI и взаимодействием с пользователем


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()

        // Регистрируем лаунчер на Редактирование поста
        val editLauncher = registerForActivityResult(EditPostResultContract()) { editedText ->
            editedText?.let {
                viewModel.changeContent(editedText)
                viewModel.save()
            }
        }

        val adapter = PostAdapter(
            object : OnInteractorListener {

                override fun onLike(post: Post) {
                    viewModel.like(post.id)
                }

                override fun onShare(post: Post) {
                    //viewModel.share(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val intent2 = Intent.createChooser(intent, getString(R.string.chooser_share_post))

                    startActivity(intent2)
                }

                override fun onView(post: Post) {
                    viewModel.view(post.id)
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    editLauncher.launch(post.content)
                }
            }
        )


        binding.recyclerId.adapter = adapter

        // Обновление списка постов в RecyclerView при изменении данных в LiveData
        viewModel.data.observe(this) { posts ->
            val isNew = posts.size != adapter.itemCount
            adapter.submitList(posts) {
                if (isNew) {
                    binding.recyclerId.smoothScrollToPosition(0)
                }
            }
        }

        // Регистрируем лаунчер на создание поста
        val newPostLauncher = registerForActivityResult(NewPostResultContract()) { content ->
            content ?: return@registerForActivityResult
            viewModel.changeContent(content)
            viewModel.save()
        }

        binding.buttonFab.setOnClickListener {
            newPostLauncher.launch(Unit)
        }


//        // Регистрируем лаунчер на Редактирование поста
//        val editLauncher = registerForActivityResult(EditPostResultContract()) { editedText ->
//            editedText?.let {
//                viewModel.changeContent(editedText)
//                viewModel.save()
//            }
//        }


        //  Выбираем пост для редактирования
//        viewModel.edited.observe(this) { post ->
//            val isEditing = post.id != 0L
//            with(binding) {
//                textInput.apply {
//                    if (isEditing) {
//                        requestFocus()
//                        setText(post.content)
//                    }
//                }
//                buttonAbort.visibility = if (isEditing) View.VISIBLE else View.GONE
//            }
//        }
//
//        with(binding) {
//            buttonSave.setOnClickListener {
//                if (textInput.text.isNullOrBlank()) {
//                    Toast.makeText(this@MainActivity, R.string.error_empty_content,
//                        Toast.LENGTH_LONG).show()
//                    return@setOnClickListener
//                }
//                viewModel.changeContent(textInput.text.toString())
//                viewModel.save()
//                textInput.setText("")
//                textInput.clearFocus()
//                AndroidUtils.hideKeyboard(it)
//            }
//
//            // обработчик кнопки "отмена редактирования поста"
//            buttonAbort.setOnClickListener {
//                viewModel.cancelEdit() // Сбрасываем редактирование
//                with(binding) {
//                    textInput.setText("")
//                    textInput.clearFocus()
//                    AndroidUtils.hideKeyboard(it)
//                }
//            }
//
//        }

    }
}