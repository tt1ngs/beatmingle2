package com.ttings.beatwave.repositories

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okio.IOException
import timber.log.Timber
import javax.inject.Inject

class TrackRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {

    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(false)
    val playbackState: StateFlow<Boolean> = _playbackState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: Flow<Float> = _progress

    interface TrackChangeCallback {
        fun onTrackChange(track: Track, queue: List<Track>)
    }

    var trackChangeCallback: TrackChangeCallback? = null

    suspend fun getGenres(): List<String> {
        val genres = mutableListOf<String>()
        val dataSnapshot = database.getReference("genres").get().await()
        for (snapshot in dataSnapshot.children) {
            val genre = snapshot.getValue(String::class.java)
            if (genre != null) {
                genres.add(genre)
            }
        }
        return genres
    }

    suspend fun uploadTrack(
        userId: String,
        trackId: String,
        title: String,
        genre: String,
        artistIds: List<String>,
        albumId: String?,
        duration: Int,
        imageUri: String,
        trackUri: String,
        isPrivate: Boolean
    ) {
        // Загрузка изображения
        val imageRef = storage.reference.child("trackFolder/trackImage/${trackId}")
        imageRef.putFile(imageUri.toUri()).await()
        // Получение URL изображения
        val imageUrl = imageRef.downloadUrl.await().toString()
        // Загрузка трека
        val trackRef = storage.reference.child("trackFolder/trackFile/${trackId}")
        trackRef.putFile(trackUri.toUri()).await()
        // Получение URL трека
        val trackUrl = trackRef.downloadUrl.await().toString()
        val track = Track(
            trackId = trackId,
            title = title,
            genre = genre,
            image = imageUrl,
            file = trackUrl,
            artistIds = artistIds,
            albumId = null,
            duration = duration,
            isPrivate = isPrivate
        )
        database.getReference("track").child(trackId).setValue(track).await()
        database.getReference("users").child(userId).child("uploads").push().setValue(trackId).await()
    }

    suspend fun getLikedTracksByUser(userId: String?): List<Track> {
        val likedTracks = mutableListOf<Track>()
        if (userId == null) {
            Timber.tag("TR: getLikedTracksByUser").e("User ID is null")
            return likedTracks
        }
        try {
            val dataSnapshot = database.getReference("users").child(userId).child("likedTracks").get().await()
            val trackIds = dataSnapshot.getValue<MutableList<String>>() ?: mutableListOf()
            for (trackId in trackIds) {
                if (trackId == null) {
                    Timber.tag("TR: getLikedTracksByUser").e("Track ID is null")
                    continue
                }
                val trackSnapshot = database.getReference("track").child(trackId).get().await()
                val track = trackSnapshot.getValue(Track::class.java)
                if (track != null) {
                    likedTracks.add(track)
                }
            }
        } catch (e: Exception) {
            Timber.tag("TR: getLikedTracksByUser").e(e, "Error getting liked tracks by user")
        }
        return likedTracks
    }

    suspend fun getUploadedTracksByUser(userId: String, artistName: String): List<Pair<Track, String>> {
        val tracks = mutableListOf<Pair<Track, String>>()
        try {
            val dataSnapshot = database.getReference("track").get().await()
            for (snapshot in dataSnapshot.children) {
                val track = snapshot.getValue(Track::class.java)
                if (track != null && userId in track.artistIds) {
                    Timber.tag("TrackRepository").d("Artist name for user $userId: $artistName")
                    tracks.add(Pair(track, artistName))
                }
            }
        } catch (e: Exception) {
            Timber.tag("TrackRepository").e(e, "Error getting uploaded tracks by user")
        }
        return tracks
    }

    private suspend fun getTrackById(trackId: String): Track? {
        val trackRef = database.getReference("track").child(trackId)
        val snapshot = trackRef.get().await()
        return snapshot.getValue(Track::class.java).also {
            if (it == null) Timber.e("No track found for ID: $trackId")
        }
    }

    private suspend fun getPlaylistById(id: String, type: String): Playlist? {
        Timber.d("Attempting to fetch $type with ID: $id")
        val ref = database.getReference(type).child(id)
        val snapshot = ref.get().await()
        return snapshot.getValue(Playlist::class.java).also {
            if (it == null) Timber.e("No $type found for ID: $id")
        }
    }

    fun getTracksByAlbum(playlistId: String): Flow<List<Track>> = flow {
        val path = "albums/$playlistId/tracks"
        val dataSnapshot = database.getReference(path).get().await()
        val trackIds = dataSnapshot.children.mapNotNull { snapshot ->
            val trackId = snapshot.getValue(String::class.java)
            trackId?.let { getTrackById(it) }
        }
        emit(trackIds)
    }

    fun getTracksByPlaylist(playlistId: String): Flow<List<Track>> = flow {
        val path = "playlists/$playlistId/tracks"
        val dataSnapshot = database.getReference(path).get().await()
        val trackIds = dataSnapshot.children.mapNotNull { snapshot ->
            val trackId = snapshot.getValue(String::class.java)
            trackId?.let { getTrackById(it) }
        }
        emit(trackIds)
    }

    fun getTracksByLikedTracks(userId: String, limit: Int = 20): Flow<List<Track>> = flow {
        val path = "users/$userId/likedTracks"
        val dataSnapshot = database.getReference(path).limitToFirst(limit).get().await()
        val trackIds = dataSnapshot.children.mapNotNull { snapshot ->
            val trackId = snapshot.getValue(String::class.java)
            trackId?.let { getTrackById(it) }
        }
        emit(trackIds)
    }

    fun getTracksByUserUploads(userId: String, limit: Int = 20): Flow<List<Track>> = flow {
        val uploadsPath = "users/$userId/uploads"
        val dataSnapshot = database.getReference(uploadsPath).orderByKey().limitToFirst(limit).get().await()
        val trackIds = dataSnapshot.children.mapNotNull { snapshot ->
            val trackId = snapshot.getValue<Any>()?.toString() // Convert each individual item to a String
            trackId?.let {
                if (it.contains(Regex("[.#$\\[\\]]"))) {
                    null // Skip this track ID if it contains invalid characters
                } else {
                    getTrackById(it)
                }
            }
        }
        emit(trackIds)
    }

    fun getPlaylistsByUser(userId: String, type: String, onlyPublic: Boolean = false): Flow<List<Playlist>> = flow {
        val path = "users/$userId/uploads/$type"
        val dataSnapshot = database.getReference(path).get().await()
        val itemIds = dataSnapshot.children.mapNotNull { snapshot ->
            val itemId = snapshot.getValue(String::class.java)
            itemId?.let { getPlaylistById(it, type) }
        }.filter { playlist ->
            if (onlyPublic) {
                !playlist.isPrivate
            } else {
                true
            }
        }
        emit(itemIds)
    }

    fun setupMediaPlayer(track: Track, queue: List<Track>) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build())
            setOnPreparedListener {
                Timber.d("MediaPlayer: onPrepared, starting playback")
                start()
                _playbackState.value = true
                CoroutineScope(Dispatchers.Default).launch {
                    startProgressUpdater()
                }
            }
            setOnCompletionListener {
                Timber.d("MediaPlayer: onCompletion")
                if (queue.isNotEmpty()) {
                    trackChangeCallback?.onTrackChange(queue.first(), queue.drop(1))
                }
                _playbackState.value = false
            }
            setOnErrorListener { mp, what, extra ->
                Timber.e("MediaPlayer error on track ${track.title}: what $what, extra $extra")
                _playbackState.value = false
                true  // Indicate that the error was handled
            }
            try {
                setDataSource(track.file)
                prepareAsync()  // Prepare media asynchronously
            } catch (e: IOException) {
                Timber.e(e, "Error setting data source for track ${track.title}")
            }
        }
    }

    private suspend fun startProgressUpdater() {
//        Timber.d("startProgressUpdater: Starting progress updater.")
        while (mediaPlayer?.isPlaying == true) {
            val current = mediaPlayer?.currentPosition ?: 0
            val max = mediaPlayer?.duration ?: 1
            _progress.value = current.toFloat() / max.toFloat()
//            Timber.d("Progress updated: ${_progress.value}, Current: $current, Max: $max")
            delay(1000)
        }
//        Timber.d("startProgressUpdater: Exiting progress updater.")
    }


    fun togglePlayPause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            _playbackState.value = false
            // Останавливаем обновление прогресса, если трек был поставлен на паузу
            _progress.value = (mediaPlayer?.currentPosition?.toFloat() ?: 0f) / (mediaPlayer?.duration?.toFloat() ?: 1f)
        } else {
            mediaPlayer?.start()
            _playbackState.value = true
            // Перезапускаем корутину обновления прогресса
            CoroutineScope(Dispatchers.Default).launch {
                startProgressUpdater()
            }
        }
    }

    fun cleanUp() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Понравившийся трек записывается следующим образом:
    // likes -> trackId -> userIds
    // user -> userId -> likedTracks
    suspend fun addTrackToLibrary(trackId: String, currentUser: User) {
        if (currentUser != null) {
            // Добавление трека в список понравившихся треков пользователя
            val userLikesRef = database.getReference("users").child(currentUser.userId).child("likedTracks")
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
            if (currentUser.userId !in trackLikedUserIds) {
                trackLikedUserIds.add(currentUser.userId)
                trackLikesRef.setValue(trackLikedUserIds).await()
            }
            Timber.tag("TR: addTrackToLibrary").d("Track liked users: $trackLikedUserIds")
        }
    }

    suspend fun isTrackLikedByUser(trackId: String, userId: String): Boolean {
        val userLikesRef = database.getReference("users").child(userId).child("likedTracks")
        val snapshot = userLikesRef.get().await()
        val likedTrackIds = snapshot.getValue<MutableList<String>>() ?: mutableListOf()
        return trackId in likedTrackIds
    }
    suspend fun removeTrackFromLibrary(trackId: String, currentUser: User) {
        // Удаление трека из списка понравившихся треков пользователя
        val userLikesRef = database.getReference("users").child(currentUser.userId).child("likedTracks")
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
        if (currentUser.userId in trackLikedUserIds) {
            trackLikedUserIds.remove(currentUser.userId)
            trackLikesRef.setValue(trackLikedUserIds).await()
        }
    }

    suspend fun getPublicTracks(): List<Track> {
        val tracks = mutableListOf<Track>()
        val dataSnapshot = database.getReference("track").get().await()
        for (snapshot in dataSnapshot.children) {
            val track = snapshot.getValue(Track::class.java)
            if (track != null && !track.isPrivate) {
                tracks.add(track)
            }
        }
        return tracks
    }
}