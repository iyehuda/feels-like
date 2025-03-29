package com.iyehuda.feelslike.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.iyehuda.feelslike.data.local.dao.PostDao
import com.iyehuda.feelslike.data.local.entity.PostEntity
import com.iyehuda.feelslike.data.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val firestore: FirebaseFirestore
) {
    fun getAllPosts(): Flow<List<Post>> {
        return postDao.getAllPosts().map { entities ->
            entities.map { it.toPost() }
        }
    }

    fun getPostsByUser(userId: String): Flow<List<Post>> {
        return postDao.getPostsByUser(userId).map { entities ->
            entities.map { it.toPost() }
        }
    }

    suspend fun savePost(post: Post) {
        // Save to local database
        postDao.insertPost(PostEntity.fromPost(post, isSync = false))
        
        try {
            // Save to Firebase
            firestore.collection("posts")
                .document(post.id)
                .set(post)
                .await()
            
            // Mark as synced in local database
            postDao.markPostAsSynced(post.id)
        } catch (e: Exception) {
            // Post will remain marked as unsynced
            throw e
        }
    }

    suspend fun syncUnsyncedPosts() {
        val unsyncedPosts = postDao.getUnsyncedPosts()
        unsyncedPosts.forEach { postEntity ->
            try {
                firestore.collection("posts")
                    .document(postEntity.id)
                    .set(postEntity.toPost())
                    .await()
                postDao.markPostAsSynced(postEntity.id)
            } catch (e: Exception) {
                // Handle error or continue with next post
            }
        }
    }
} 