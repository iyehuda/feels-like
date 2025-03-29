package com.iyehuda.feelslike.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.iyehuda.feelslike.data.local.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT * FROM posts WHERE isSync = 0")
    suspend fun getUnsyncedPosts(): List<PostEntity>

    @Query("UPDATE posts SET isSync = 1 WHERE id = :postId")
    suspend fun markPostAsSynced(postId: String)
} 