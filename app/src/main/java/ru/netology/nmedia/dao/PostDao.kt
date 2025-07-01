package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts_room ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE posts_room SET content = :content WHERE id = :id")
    fun updateContentById(id: Long, content: String)

    fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query("""
        UPDATE posts_room SET
        likesCount = likesCount + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    fun likeById(id: Long)

    @Query("UPDATE posts_room SET sharesCount = sharesCount + 1 WHERE id = :id")
    fun shareById(id: Long)

    @Query("UPDATE posts_room SET looksCount = looksCount + 1 WHERE id = :id")
    fun viewById(id: Long)

    @Query("DELETE FROM posts_room WHERE id = :id")
    fun removeById(id: Long)
}