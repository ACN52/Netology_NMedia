package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts_room ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    // Проверка на пустоту БД
    @Query("SELECT COUNT(*) = 0 FROM posts_room")
    fun isEmpty(): LiveData<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE posts_room SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query("""
        UPDATE posts_room SET
        likesCount = likesCount + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    suspend fun likeById(id: Long)

    @Query("UPDATE posts_room SET sharesCount = sharesCount + 1 WHERE id = :id")
    suspend fun shareById(id: Long)

    @Query("UPDATE posts_room SET looksCount = looksCount + 1 WHERE id = :id")
    suspend fun viewById(id: Long)

    @Query("DELETE FROM posts_room WHERE id = :id")
    suspend fun removeById(id: Long)
}