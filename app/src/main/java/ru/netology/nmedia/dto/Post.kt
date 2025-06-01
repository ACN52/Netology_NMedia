package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likeByMe: Boolean = false,
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val looksCount: Int = 0
)
