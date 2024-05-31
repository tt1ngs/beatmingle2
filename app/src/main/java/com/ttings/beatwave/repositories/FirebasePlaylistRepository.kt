package com.ttings.beatwave.repositories

import android.annotation.SuppressLint
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.database.*
import com.google.firebase.storage.storage
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class FirebasePlaylistRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val userRepository: UserRepository
) {

    suspend fun getUserTracks(userId: String): List<Track> {
        return try {
            val snapshot = firebaseDatabase.getReference("users").child(userId).child("uploads").child("tracks").get().await()
            val trackIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
            val tracks = trackIds.mapNotNull { trackId ->
                val trackSnapshot = firebaseDatabase.getReference("track").child(trackId).get().await()
                trackSnapshot.getValue(Track::class.java)
            }
            Timber.tag("FPR: getUserTracks").d("Tracks: $tracks")
            tracks
        } catch (e: Exception) {
            Timber.tag("FPR: getUserTracks").e(e, "Error getting user tracks")
            emptyList()
        }
    }

    suspend fun createPlaylist(playlist: Playlist, imageUri: Uri) {
        try {
            if (isActive) { // Check if the coroutine is still active
                val imageUrl = uploadImage(imageUri)
                val playlistWithImage = playlist.copy(image = imageUrl)
                firebaseDatabase.getReference("playlists").child(playlist.playlistId).setValue(playlistWithImage)

                addPlaylistToUploads(playlist.userId, playlist.playlistId)
            }
        } catch (e: Exception) {
            Timber.tag("FirebasePlaylistRepository").e(e, "Error creating playlist")
        }
    }

    suspend fun updatePlaylistCover(playlistId: String, coverUri: Uri) {
        val imageUrl = uploadImage(coverUri)
        firebaseDatabase.getReference("playlists").child(playlistId).updateChildren(mapOf(
            "image" to imageUrl
        ))
    }

    suspend fun updatePlaylistName(playlistId: String, name: String) {
        firebaseDatabase.getReference("playlists").child(playlistId).updateChildren(mapOf(
            "title" to name
        ))
    }

    suspend fun updatePlaylistPrivacy(playlistId: String, isPrivate: Boolean) {
        firebaseDatabase.getReference("playlists").child(playlistId).updateChildren(mapOf(
            "private" to isPrivate
        ))
    }

    // Понравившийся плейлист записывается в базу данных следующим образом:
    // likes -> playlistId -> userIds
    // users -> userId -> likes -> playlistIds
    suspend fun addPlaylistToLibrary(userId: String, playlistId: String) {
        val currentUser = userRepository.getCurrentUser() ?: return
        // Write to users -> userId -> likes -> playlistIds
        val userLikesRef = firebaseDatabase.getReference("users").child(currentUser.userId).child("likedPlaylists")
        val snapshot = userLikesRef.get().await()
        val playlistIds = snapshot.getValue<MutableList<String>>() ?: mutableListOf()
        if (playlistId !in playlistIds) {
            playlistIds.add(playlistId)
            userLikesRef.setValue(playlistIds).await()
        }

        // Write to likes -> playlistId -> userIds
        val playlistLikesRef2 = firebaseDatabase.getReference("likes").child(playlistId)
        val userLikesSnapshot = playlistLikesRef2.get().await()
        val userIds = userLikesSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
        if (userId !in userIds) {
            userIds.add(userId)
            playlistLikesRef2.setValue(userIds).await()
        }
    }

    suspend fun addPlaylistToUploads(userId: String, playlistId: String) {
        val currentUser = userRepository.getCurrentUser() ?: return
        // Write to users -> userId -> uploads -> playlistIds
        val userUploadsRef = firebaseDatabase.getReference("users").child(currentUser.userId).child("uploads").child("playlists")
        val snapshot = userUploadsRef.get().await()
        val playlistIds = snapshot.getValue<MutableList<String>>() ?: mutableListOf()
        if (playlistId !in playlistIds) {
            playlistIds.add(playlistId)
            userUploadsRef.setValue(playlistIds).await()
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("playlist/${UUID.randomUUID()}")
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun getLikedPlaylists(userId: String): List<Pair<Playlist, String>> {
        return try {
            val snapshot = firebaseDatabase.getReference("users").child(userId).child("likedPlaylists").get().await()
            val playlistIds = snapshot.children.map { it.getValue(String::class.java) ?: "" }.filter { it.isNotBlank() }
            val playlists = playlistIds.mapNotNull { playlistId ->
                val playlistSnapshot = firebaseDatabase.getReference("playlists").child(playlistId).get().await()
                val playlist = playlistSnapshot.getValue(Playlist::class.java)
                playlist?.let { Pair(it, userRepository.getCurrentUser()?.username ?: "Unknown") }
            }
            playlists
        } catch (e: Exception) {
            Timber.tag("FPR: getLikedPlaylists").e(e, "Error getting liked playlists")
            emptyList()
        }
    }

    suspend fun getUploadedPlaylists(userId: String): List<Pair<Playlist, String>> {
        return try {
            val snapshot = firebaseDatabase.getReference("users").child(userId).child("uploads").child("playlists").get().await()
            val playlistIds = snapshot.children.map { it.getValue(String::class.java) ?: "" }.filter { it.isNotBlank() }
            val playlists = playlistIds.mapNotNull { playlistId ->
                val playlistSnapshot = firebaseDatabase.getReference("playlists").child(playlistId).get().await()
                val playlist = playlistSnapshot.getValue(Playlist::class.java)
                playlist?.let { Pair(it, userRepository.getCurrentUser()?.username ?: "Unknown") }
            }
            playlists
        } catch (e: Exception) {
            Timber.tag("FPR: getUploadedPlaylists").e(e, "Error getting uploaded playlists")
            emptyList()
        }
    }

    suspend fun getUserPlaylists(userId: String): List<Playlist> {
        return try {
            val snapshot = firebaseDatabase.getReference("users").child(userId).child("uploads").child("playlists").get().await()
            val playlistIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
            val playlists = playlistIds.mapNotNull { playlistId ->
                val playlistSnapshot = firebaseDatabase.getReference("playlists").child(playlistId).get().await()
                playlistSnapshot.getValue(Playlist::class.java)
            }
            Timber.tag("FPR: getUserPlaylists").d("Playlists: $playlists")
            playlists
        } catch (e: Exception) {
            Timber.tag("FPR: getUserPlaylists").e(e, "Error getting user playlists")
            emptyList()
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        try {
            // Удаление плейлиста из основного хранилища плейлистов
            firebaseDatabase.getReference("playlists").child(playlistId).removeValue().await()

            // Удаление плейлиста из списка загруженных плейлистов пользователя
            val currentUser = userRepository.getCurrentUser() ?: return
            val userUploadsRef = firebaseDatabase.getReference("users").child(currentUser.userId).child("uploads").child("playlists")
            val snapshot = userUploadsRef.get().await()
            val playlistIds = snapshot.getValue<MutableList<String>>() ?: mutableListOf()
            if (playlistId in playlistIds) {
                playlistIds.remove(playlistId)
                userUploadsRef.setValue(playlistIds).await()
            }

            // Удаление плейлиста из списка понравившихся плейлистов всех пользователей
            val likesRef = firebaseDatabase.getReference("likes").child(playlistId)
            likesRef.removeValue().await()
        } catch (e: Exception) {
            Timber.tag("FirebasePlaylistRepository").e(e, "Error deleting playlist")
        }
    }

    suspend fun getPlaylistTracks(playlistId: String): List<Track> {
        return try {
            val snapshot = firebaseDatabase.getReference("playlists").child(playlistId).child("tracks").get().await()
            val trackIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
            val tracks = trackIds.mapNotNull { trackId ->
                val trackSnapshot = firebaseDatabase.getReference("track").child(trackId).get().await()
                trackSnapshot.getValue(Track::class.java)
            }
            Timber.tag("FPR: getPlaylistTracks").d("Tracks: $tracks")
            tracks
        } catch (e: Exception) {
            Timber.tag("FPR: getPlaylistTracks").e(e, "Error getting playlist tracks")
            emptyList()
        }
    }

    suspend fun getPlaylist(playlistId: String): Playlist? {
        val snapshot = firebaseDatabase.getReference("playlists").child(playlistId).get().await()
        return snapshot.getValue(Playlist::class.java)
    }

    suspend fun getTracks(): List<Track> = suspendCancellableCoroutine { continuation ->
        try {
            val tracks = mutableListOf<Track>()
            val totalTracksRef = firebaseDatabase.getReference("track")
            totalTracksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach { snapshot ->
                        val track = snapshot.getValue(Track::class.java)
                        if (track != null) { // Check if the track is not in the playlist
                            tracks.add(track)
                        }
                    }
                    continuation.resume(tracks)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.tag("FPR: getTracks").e(databaseError.toException(), "Error getting tracks")
                    continuation.resumeWithException(databaseError.toException())
                }
            })
        } catch (e: Exception) {
            Timber.tag("FirebasePlaylistRepository").e(e, "Error getting tracks")
            continuation.resumeWithException(e)
        }
    }

    fun generateRandomIndices(listSize: Int, numIndices: Int): List<Int> {
        val randomIndices = mutableListOf<Int>()
        val random = Random.Default

        if (listSize > 0) {
            val actualNumIndices = minOf(listSize, numIndices)
            for (i in 0 until actualNumIndices) {
                randomIndices.add(random.nextInt(listSize))
            }
        }

        Timber.tag("FPR: generateRandomIndices").d("Random indices: $randomIndices")
        return randomIndices
    }

    suspend fun addTrackToPlaylist(trackId: String, playlistId: String, onlyUserTracks: Boolean = false) {
        if (onlyUserTracks) {
            val playlistRef = firebaseDatabase.getReference("albums").child(playlistId).child("tracks")
            val snapshot = playlistRef.get().await()

            // Получение данных как HashMap и преобразование их в List
            val trackMap = snapshot.getValue(object : GenericTypeIndicator<HashMap<String, String>>() {})
            val trackIds = trackMap?.values?.toList() ?: emptyList()

            if (trackId !in trackIds) {
                playlistRef.push().setValue(trackId)
            } else {
                Timber.tag("FirebasePlaylistRepository").d("Track already in playlist")
            }
        }
        else {
            val playlistRef = firebaseDatabase.getReference("playlists").child(playlistId).child("tracks")
            val snapshot = playlistRef.get().await()

            // Получение данных как HashMap и преобразование их в List
            val trackMap = snapshot.getValue(object : GenericTypeIndicator<HashMap<String, String>>() {})
            val trackIds = trackMap?.values?.toList() ?: emptyList()

            if (trackId !in trackIds) {
                playlistRef.push().setValue(trackId)
            } else {
                Timber.tag("FirebasePlaylistRepository").d("Track already in playlist")
            }
        }
    }

    @SuppressLint("RestrictedApi")
    suspend fun removeTrackFromPlaylist(trackId: String, playlistId: String, onlyUserTracks: Boolean = false) {
        val playlistRef = if (onlyUserTracks) {
            firebaseDatabase.getReference("albums").child(playlistId).child("tracks")
        } else {
            firebaseDatabase.getReference("playlists").child(playlistId).child("tracks")
        }

        // Логгирование пути к данным для отладки
        Timber.tag("FirebasePlaylistRepository").d("Playlist reference path: ${playlistRef.path}")

        val snapshot = playlistRef.get().await()

        // Логгирование наличия данных в снимке
        Timber.tag("FirebasePlaylistRepository").d("Snapshot exists: ${snapshot.exists()}")

        if (snapshot.exists()) {
            // Получение данных как Map<String, String>
            val trackMap = snapshot.getValue(object : GenericTypeIndicator<HashMap<String, String>>() {}) ?: hashMapOf()

            // Логгирование содержимого trackMap для отладки
            Timber.tag("FirebasePlaylistRepository").d("Track map: $trackMap")

            // Проверка, есть ли трек в плейлисте
            if (trackId in trackMap.values) {
                // Если трек есть в плейлисте, удалить его
                val trackToRemove = trackMap.filterValues { it == trackId }.keys.first()
                playlistRef.child(trackToRemove).removeValue().await()
                Timber.tag("FirebasePlaylistRepository").d("Track $trackId removed from playlist $playlistId")
            } else {
                Timber.tag("FirebasePlaylistRepository").e("Track $trackId is not in playlist $playlistId")
            }
        } else {
            Timber.tag("FirebasePlaylistRepository").e("Snapshot is empty for path: ${playlistRef.path}")
        }
    }



    suspend fun isPlaylistLiked(userId: String, playlistId: String): Boolean {
        val reference = firebaseDatabase.getReference("users").child(userId).child("likedPlaylists")
        val snapshot = reference.get().await()
        return snapshot.exists()
    }

    suspend fun removePlaylistFromLibrary(userId: String, playlistId: String) {
        try {
            Timber.tag("FirebasePlaylistRepository").d("Removing playlist from library")
            Timber.tag("FirebasePlaylistRepository").d("$userId, $playlistId")
            // Удаление трека из списка понравившихся плейлистов пользователя
            val userLikesRef = firebaseDatabase.getReference("users").child(userId).child("likedPlaylists")
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

    suspend fun getPublicPlaylists(): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        val dataSnapshot = firebaseDatabase.getReference("playlists").get().await()
        Timber.tag("FirebasePlaylistRepository").e("Playlists: ${dataSnapshot.toString()}")
        for (snapshot in dataSnapshot.children) {
            val playlist = snapshot.getValue(Playlist::class.java)
            if (playlist != null && !playlist.isPrivate) {
                playlists.add(playlist)
            }
        }
        return playlists
    }
}