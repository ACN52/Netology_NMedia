package ru.netology.nmedia.activity

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity

class PostPagingSource(
    private val apiService: PostsApiService,
    private val dao: PostDao
) : PagingSource<Int, PostEntity>() {

    override fun getRefreshKey(state: PagingState<Int, PostEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostEntity> {
        return try {
            val page = params.key ?: 0
            val response = apiService.getLatest(params.loadSize)

            if (!response.isSuccessful) {
                return LoadResult.Error(Exception("HTTP error: ${response.code()}"))
            }

            val posts = response.body() ?: emptyList()

            if (posts.isNotEmpty()) {
                // Сохраняем посты в базу данных
                dao.insertAll(posts.map { PostEntity.fromDto(it) })
            }

            LoadResult.Page(
                data = posts.map { PostEntity.fromDto(it) },
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (posts.isNotEmpty()) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}