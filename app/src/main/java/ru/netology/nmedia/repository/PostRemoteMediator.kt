package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity


@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostsApiService,
    private val dao: PostDao,
    private val refreshTrigger: Flow<Unit>,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
): RemoteMediator<Int, PostEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    fun getRefreshKey(state: PagingState<Int, PostEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        return try {
            when (loadType) {
                LoadType.REFRESH -> handleRefresh(state)
                LoadType.PREPEND -> handlePrepend() // Отключаем автоматический PREPEND
                LoadType.APPEND -> handleAppend(state)
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun handleRefresh(state: PagingState<Int, PostEntity>): MediatorResult {
        // Обработка триггера обновления
        var shouldRefresh = false
        refreshTrigger
            .take(1)
            .collect {
                shouldRefresh = true
            }

        if (!shouldRefresh) {
            // Если нет сигнала обновления, используем кешированные данные
            return MediatorResult.Success(endOfPaginationReached = false)
        }

        return appDb.withTransaction {
            // Получаем ID самого нового поста для инкрементального обновления
            val latestPostId = dao.getLatestPostId()

            val response = if (latestPostId != null) {
                // Инкрементальное обновление - получаем только новые посты
                apiService.getAfter(latestPostId, state.config.pageSize)
            } else {
                // Первая загрузка - получаем последние посты
                apiService.getLatest(state.config.initialLoadSize)
            }

            if (!response.isSuccessful) {
                return@withTransaction MediatorResult.Error(Exception("HTTP error: ${response.code()}"))
            }

            val body = response.body() ?: emptyList()

            if (body.isEmpty()) {
                return@withTransaction MediatorResult.Success(endOfPaginationReached = true)
            }

            // Сохраняем новые посты в базу (не очищаем старые)
            dao.insertAll(body.map { PostEntity.fromDto(it).copy(isVisible = true) })

            // Обновляем ключи только если это первая загрузка
            if (latestPostId == null) {
                postRemoteKeyDao.clear()
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        type = PostRemoteKeyEntity.KeyType.BEFORE,
                        key = body.last().id,
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = false)
        }
    }

    private suspend fun handlePrepend(): MediatorResult {
        // Отключаем автоматическую подгрузку при скролле к верху
        return MediatorResult.Success(endOfPaginationReached = true)
    }

    private suspend fun handleAppend(state: PagingState<Int, PostEntity>): MediatorResult {
        return appDb.withTransaction {
            val oldestKey = postRemoteKeyDao.min() ?: return@withTransaction MediatorResult.Success(
                endOfPaginationReached = true
            )

            val response = apiService.getBefore(oldestKey, state.config.pageSize)

            if (!response.isSuccessful) {
                return@withTransaction MediatorResult.Error(Exception("HTTP error: ${response.code()}"))
            }

            val body = response.body() ?: emptyList()

            if (body.isEmpty()) {
                return@withTransaction MediatorResult.Success(endOfPaginationReached = true)
            }

            // Сохраняем посты в базу
            dao.insertAll(body.map { PostEntity.fromDto(it).copy(isVisible = true) })

            // Обновляем ключ для следующей загрузки
            postRemoteKeyDao.insert(
                PostRemoteKeyEntity(
                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                    key = body.last().id,
                )
            )

            MediatorResult.Success(endOfPaginationReached = false)
        }
    }
}
