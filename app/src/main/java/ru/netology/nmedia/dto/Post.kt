package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    var id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: String,
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val looksCount: Int = 0,
    val likedByMe: Boolean = false,
    val authorAvatar: Boolean,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
    //val video: String
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)
