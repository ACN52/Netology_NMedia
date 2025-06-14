package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AcManagementBinding
import ru.netology.nmedia.databinding.AcNewPostBinding
import ru.netology.nmedia.databinding.ActivityMainBinding

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = AcManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем текст для редактирования (если он есть)
        val existingText = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (!existingText.isNullOrBlank()) {
            binding.editTextCreateUpdate.setText(existingText)
            binding.editTextCreateUpdate.setSelection(existingText.length) // Курсор в конец
        }

        // Отрабатываем кнопку Создать (Create) пост
        // --------------------
        binding.buttonCreate.setOnClickListener {
            val intent = Intent()

            if (binding.editTextCreateUpdate.text.isNullOrBlank()) {
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                val content = binding.editTextCreateUpdate.text.toString()

                intent.putExtra(Intent.EXTRA_TEXT, content)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
        // --------------------

        // Отрабатываем кнопку Редактировать (Update) пост
        // --------------------
        binding.buttonUpdate.setOnClickListener {
            val content = binding.editTextCreateUpdate.text.toString()
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(Intent.EXTRA_TEXT, content)
            })
            finish()
        }
        // --------------------


    }
}