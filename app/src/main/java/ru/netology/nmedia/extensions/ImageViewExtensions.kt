package ru.netology.nmedia.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R

fun ImageView.loadAvatar(avatarPath: String) {
    if (avatarPath.isNullOrEmpty()) {
        println("Проверьте путь к аватаркам")
        setImageResource(R.drawable.ic_error_24)
        return
    }

    val avatarUrl = "http://192.168.1.7:9999/avatars/$avatarPath"
    println("Загрузка аватара из: $avatarUrl")

    Glide.with(context)
        .load(avatarUrl)
        .placeholder(R.drawable.ic_manufacturing_24)
        .error(R.drawable.ic_error_24)
        .timeout(10_000)
        .circleCrop() // Создаем круглые аватарки
        //.transform(RoundedCorners(16)) // круглые аватарки радиус 16px
        .circleCrop()
        .into(this)
}