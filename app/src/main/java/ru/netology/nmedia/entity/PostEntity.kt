package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity(tableName = "posts_room")
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val looksCount: Int = 0,
    val likedByMe: Boolean
) {
    fun toDto() = Post(id, author, content, published, likesCount, sharesCount, looksCount, likedByMe)

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            dto.id,
            dto.author,
            dto.content,
            dto.published,
            dto.likesCount,
            dto.sharesCount,
            dto.looksCount,
            dto.likedByMe)
    }
}
