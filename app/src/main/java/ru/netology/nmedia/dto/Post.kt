package ru.netology.nmedia.dto

data class Post(
    var id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val looksCount: Int = 0,
    val likedByMe: Boolean = false
    //val video: String
)
