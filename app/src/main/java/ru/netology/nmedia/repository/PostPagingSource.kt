package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.IOException

class PostPagingSource(
    private val apiService: PostsApiService,
    private val dao: PostDao,
    private val refreshTrigger: Flow<Unit>
): PagingSource<Long, Post>() {

    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        return try {
            // Следим за триггером обновления
            refreshTrigger.collect {
                // При получении сигнала инвалидируем источник
                if (params is LoadParams.Refresh) {
                    invalidate()
                }
            }

            val page = params.key ?: 0
            val response = apiService.getAfter(0, params.loadSize)

            if (response.isSuccessful) {
                val posts = response.body() ?: emptyList()

                // Сохраняем в базу
                dao.insert(posts.map { PostEntity.fromDto(it).copy(isVisible = true) })

                LoadResult.Page(
                    data = posts,
                    prevKey = if (page.toInt() == 0) null else page - 1,
                    nextKey = if (posts.isEmpty()) null else page + 1
                )
            } else {
                LoadResult.Error(Exception("HTTP error: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

//    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
//        try {
//            val result = when (params) {
//                is LoadParams.Refresh -> {
//                    apiService.getLatest(params.loadSize)
//                }
//                is LoadParams.Append -> {
//                    apiService.getBefore(id = params.key, count = params.loadSize)
//                }
//                is LoadParams.Prepend -> return LoadResult.Page(
//                    data = emptyList(), nextKey = null, prevKey = params.key
//                )
//            }
//            if (!result.isSuccessful) {
//                throw HttpException(result)
//            }
//            val data = result.body().orEmpty()
//            return LoadResult.Page(
//                data = data,
//                prevKey = params.key,
//                nextKey = data.lastOrNull()?.id
//            )
//        } catch (e: IOException) {
//            return LoadResult.Error(e)
//        }
//
//    }
}
