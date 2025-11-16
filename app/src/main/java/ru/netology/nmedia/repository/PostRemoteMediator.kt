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
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
): RemoteMediator<Int, PostEntity>() {

//    override suspend fun initialize(): InitializeAction {
//        return InitializeAction.SKIP_INITIAL_REFRESH
//    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        return try {
            when (loadType) {
                LoadType.REFRESH -> handleRefresh(state)
                LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true) // PREPEND отключен
                LoadType.APPEND -> handleAppend(state)
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun handleRefresh(state: PagingState<Int, PostEntity>): MediatorResult {
        return appDb.withTransaction {
            // При refresh всегда получаем самые свежие данные
            val response = apiService.getLatest(state.config.initialLoadSize)

            if (!response.isSuccessful) {
                return@withTransaction MediatorResult.Error(Exception("HTTP error: ${response.code()}"))
            }

            val body = response.body() ?: emptyList()

            if (body.isNotEmpty()) {
                // Вставляем/обновляем посты (conflict strategy заменяет при конфликте)
                dao.insertAll(body.map { PostEntity.fromDto(it).copy(isVisible = true) })

                // Обновляем ключи для APPEND
                postRemoteKeyDao.clear()
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        type = PostRemoteKeyEntity.KeyType.BEFORE,
                        key = body.last().id,
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        }
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

            if (body.isNotEmpty()) {
                dao.insertAll(body.map { PostEntity.fromDto(it).copy(isVisible = true) })
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        type = PostRemoteKeyEntity.KeyType.BEFORE,
                        key = body.last().id,
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        }
    }
}