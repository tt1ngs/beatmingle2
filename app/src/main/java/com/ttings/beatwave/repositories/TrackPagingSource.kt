import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.database.FirebaseDatabase
import com.ttings.beatwave.data.Track
import kotlinx.coroutines.tasks.await
import kotlin.math.min

class TrackPagingSource(
    private val database: FirebaseDatabase
) : PagingSource<Int, Track>() {

    private var allTracks = mutableListOf<Track>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
        return try {
            val currentPage = params.key ?: 0
            if (allTracks.isEmpty()) {
                val dataSnapshot = database.getReference("track").get().await()
                for (snapshot in dataSnapshot.children) {
                    val track = snapshot.getValue(Track::class.java)
                    if (track != null) {
                        allTracks.add(track)
                    }
                }
                allTracks.shuffle()
            }

            val endIndex = min(currentPage + params.loadSize, allTracks.size)
            val pageData = allTracks.subList(currentPage, endIndex)

            LoadResult.Page(
                data = pageData,
                prevKey = if (currentPage == 0) null else currentPage - params.loadSize,
                nextKey = if (endIndex == allTracks.size) null else endIndex
            )
        } catch (e: Exception) {
            println("Error loading tracks: $e")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Track>): Int? {
        return state.anchorPosition
    }
}
