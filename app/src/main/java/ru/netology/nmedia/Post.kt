package ru.netology.nmedia

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    var likeByMe: Boolean,
    var likesCount: Int = 0,
    var sharesCount: Int = 0,
    var looksCount: Int = 0
)
