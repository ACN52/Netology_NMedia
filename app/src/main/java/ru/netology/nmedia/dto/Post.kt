package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName
import ru.netology.nmedia.enumeration.AttachmentType

sealed interface FeedItem{
    val id: Long
}

data class Post(
    override  var id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: String,
    @SerializedName("likes")
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val looksCount: Int = 0,
    val likedByMe: Boolean = false,
    val authorAvatar: Boolean,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
    //val video: String
): FeedItem

data class Ad(
    override  val id: Long,
    val image: String
): FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType,
)
