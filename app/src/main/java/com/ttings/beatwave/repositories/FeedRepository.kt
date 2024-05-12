package com.ttings.beatwave.repositories

import TrackPagingSource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {
    fun getTracks(): Flow<PagingData<Track>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { TrackPagingSource(database) }
        ).flow
    }

    suspend fun getAuthorById(userId: String): User? {
        return try {
            val snapshot = database.getReference("users").child(userId).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            Timber.tag("UserRepository").e(e, "Error getting user by id")
            null
        }
    }

    // Понравившийся трек записывается следующим образом:
    // likes -> trackId -> userIds
    // user -> userId -> likedTracks
    suspend fun addTrackToLibrary(trackId: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Добавление трека в список понравившихся треков пользователя
            val userLikesRef = database.getReference("users").child(currentUser.uid).child("likedTracks")
            val userLikesSnapshot = userLikesRef.get().await()
            val userLikedTrackIds = userLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (trackId !in userLikedTrackIds) {
                userLikedTrackIds.add(trackId)
                userLikesRef.setValue(userLikedTrackIds).await()
            }
            Timber.tag("TR: addTrackToLibrary").d("User liked tracks: $userLikedTrackIds")

            // Добавление пользователя в список пользователей, которые лайкнули трек
            val trackLikesRef = database.getReference("likes").child(trackId)
            val trackLikesSnapshot = trackLikesRef.get().await()
            val trackLikedUserIds = trackLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (currentUser.uid !in trackLikedUserIds) {
                trackLikedUserIds.add(currentUser.uid)
                trackLikesRef.setValue(trackLikedUserIds).await()
            }
            Timber.tag("TR: addTrackToLibrary").d("Track liked users: $trackLikedUserIds")
        }
    }

    suspend fun deleteTrackFromLibrary(trackId: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Удаление трека из списка понравившихся треков пользователя
            val userLikesRef = database.getReference("users").child(currentUser.uid).child("likedTracks")
            val userLikesSnapshot = userLikesRef.get().await()
            val userLikedTrackIds = userLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (trackId in userLikedTrackIds) {
                userLikedTrackIds.remove(trackId)
                userLikesRef.setValue(userLikedTrackIds).await()
            }

            // Удаление пользователя из списка пользователей, которые лайкнули трек
            val trackLikesRef = database.getReference("likes").child(trackId)
            val trackLikesSnapshot = trackLikesRef.get().await()
            val trackLikedUserIds = trackLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (currentUser.uid in trackLikedUserIds) {
                trackLikedUserIds.remove(currentUser.uid)
                trackLikesRef.setValue(trackLikedUserIds).await()
            }
        }
    }

    suspend fun followUser(userId: String) {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val followMap = mapOf("userId" to currentUser.uid, "followedUserId" to userId)
                database.getReference("subscriptions").push().setValue(followMap).await()
            }
        } catch (e: Exception) {
            Timber.tag("UserRepository").e(e, "Error following user")
        }
    }

    suspend fun unfollowUser(userId: String) {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val subscriptionsRef = database.getReference("subscriptions")
                val snapshot = subscriptionsRef.orderByChild("userId").equalTo(currentUser.uid).get().await()
                for (data in snapshot.children) {
                    if (data.child("followedUserId").value == userId) {
                        subscriptionsRef.child(data.key!!).removeValue().await()
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag("UserRepository").e(e, "Error unfollowing user")
        }
    }

    suspend fun isFollowing(userId: String): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val subscriptionsRef = database.getReference("subscriptions")
                val snapshot = subscriptionsRef.orderByChild("userId").equalTo(currentUser.uid).get().await()
                for (data in snapshot.children) {
                    if (data.child("followedUserId").value == userId) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            Timber.tag("UserRepository").e(e, "Error checking if following user")
            false
        }
    }

    suspend fun isLiked(trackId: String): Boolean {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val trackLikesRef = database.getReference("likes").child(trackId)
            val snapshot = trackLikesRef.get().await()
            val userIds = snapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (currentUser.uid in userIds) {
                return true
            }
        }
        return false
    }
}