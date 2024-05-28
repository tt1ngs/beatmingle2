package com.ttings.beatwave.repositories

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.storage.storage
import com.ttings.beatwave.data.Playlist
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FirebaseAlbumsRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val userRepository: UserRepository
) {

    suspend fun removeAlbumFromLibrary(userId: String, playlistId: String) {
        try {
            // Удаление трека из списка понравившихся плейлистов пользователя
            val userLikesRef = firebaseDatabase.getReference("users").child(userId).child("likedAlbums")
            val userLikesSnapshot = userLikesRef.get().await()
            val userLikedPlaylistsIds = userLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (playlistId in userLikedPlaylistsIds) {
                userLikedPlaylistsIds.remove(playlistId)
                userLikesRef.setValue(userLikedPlaylistsIds).await()
            }

            // Удаление пользователя из списка пользователей, которые лайкнули плейлист
            val trackLikesRef = firebaseDatabase.getReference("likes").child(playlistId)
            val trackLikesSnapshot = trackLikesRef.get().await()
            val trackLikedUserIds = trackLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (userId in trackLikedUserIds) {
                trackLikedUserIds.remove(userId)
                trackLikesRef.setValue(trackLikedUserIds).await()
            }
        } catch (e: Exception) {
            Timber.tag("FirebasePlaylistRepository").e(e, "Error removing playlist from library")
        }
    }

    suspend fun isAlbumLiked(userId: String, playlistId: String): Boolean {
        val reference = firebaseDatabase.getReference("users").child(userId).child("likedAlbums")
        val snapshot = reference.get().await()
        return snapshot.exists()
    }

    suspend fun getAlbum(playlistId: String): Playlist? {
        val snapshot = firebaseDatabase.getReference("albums").child(playlistId).get().await()
        return snapshot.getValue(Playlist::class.java)
    }

    suspend fun createAlbum(playlist: Playlist, imageUri: Uri) {
        try {
            if (isActive) { // Check if the coroutine is still active
                val imageUrl = uploadImage(imageUri)
                val playlistWithImage = playlist.copy(image = imageUrl)
                firebaseDatabase.getReference("albums").child(playlist.playlistId).setValue(playlistWithImage)

                addAlbumToUploads(playlist.userId, playlist.playlistId)
            }
        } catch (e: Exception) {
            Timber.tag("FAR").e(e, "Error creating album")
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("album/${UUID.randomUUID()}")
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun addAlbumToUploads(userId: String, playlistId: String) {
        val currentUser = userRepository.getCurrentUser() ?: return
        // Write to users -> userId -> uploads -> playlistIds
        val userUploadsRef = firebaseDatabase.getReference("users").child(currentUser.userId).child("uploads").child("albums")
        val snapshot = userUploadsRef.get().await()
        val playlistIds = snapshot.getValue<MutableList<String>>() ?: mutableListOf()
        if (playlistId !in playlistIds) {
            playlistIds.add(playlistId)
            userUploadsRef.setValue(playlistIds).await()
        }
    }

    // Понравившийся плейлист записывается в базу данных следующим образом:
    // likes -> playlistId -> userIds
    // users -> userId -> likes -> playlistIds
    suspend fun addAlbumToLibrary(userId: String, albumId: String) {
        val currentUser = userRepository.getCurrentUser() ?: return
        // Write to users -> userId -> likes -> playlistIds
        val userLikesRef = firebaseDatabase.getReference("users").child(currentUser.userId).child("likedAlbums")
        val snapshot = userLikesRef.get().await()
        val albumIds = snapshot.getValue<MutableList<String>>() ?: mutableListOf()
        if (albumId !in albumIds) {
            albumIds.add(albumId)
            userLikesRef.setValue(albumIds).await()
        }

        // Write to likes -> playlistId -> userIds
        val playlistLikesRef2 = firebaseDatabase.getReference("likes").child(albumId)
        val userLikesSnapshot = playlistLikesRef2.get().await()
        val userIds = userLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
        if (userId !in userIds) {
            userIds.add(userId)
            playlistLikesRef2.setValue(userIds).await()
        }
    }

    suspend fun getLikedAlbums(userId: String): List<Pair<Playlist, String>> {
        return try {
            val snapshot = firebaseDatabase.getReference("users").child(userId).child("likedAlbums").get().await()
            val albumIds = snapshot.children.map { it.getValue(String::class.java) ?: "" }.filter { it.isNotBlank() }
            val album = albumIds.mapNotNull { albumId ->
                val albumSnapshot = firebaseDatabase.getReference("albums").child(albumId).get().await()
                val album = albumSnapshot.getValue(Playlist::class.java)
                album?.let { Pair(it, userRepository.getCurrentUser()?.username ?: "Unknown") }
            }
            album
        } catch (e: Exception) {
            Timber.tag("FPR: getLikedAlbums").e(e, "Error getting liked albums")
            emptyList()
        }
    }

    suspend fun getUploadedAlbums(userId: String): List<Pair<Playlist, String>> {
        return try {
            val snapshot = firebaseDatabase.getReference("users").child(userId).child("uploads").child("albums").get().await()
            val albumIds = snapshot.children.map { it.getValue(String::class.java) ?: "" }.filter { it.isNotBlank() }
            val album = albumIds.mapNotNull { albumId ->
                val albumSnapshot = firebaseDatabase.getReference("albums").child(albumId).get().await()
                val album = albumSnapshot.getValue(Playlist::class.java)
                album?.let { Pair(it, userRepository.getCurrentUser()?.username ?: "Unknown") }
            }
            album
        } catch (e: Exception) {
            Timber.tag("FPR: getUploadedAlbums").e(e, "Error getting uploaded albums")
            emptyList()
        }
    }

}
