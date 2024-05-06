package com.ttings.beatwave.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ttings.beatwave.data.User
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    suspend fun uploadImage(uri: Uri): String {
        val ref = storage.reference.child("profileImage/${UUID.randomUUID()}")
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await()
        return downloadUrl.toString()
    }
    suspend fun uploadBackground(uri: Uri): String {
        val ref = storage.reference.child("profileImage/background/${UUID.randomUUID()}")
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await()
        return downloadUrl.toString()
    }

    fun updateUserImage(url: String) {
        val user = auth.currentUser
        if (user != null) {
            database.getReference("users").child(user.uid).updateChildren(mapOf("avatar" to url))
        }
    }
    fun updateUserBackground(url: String) {
        val user = auth.currentUser
        if (user != null) {
            database.getReference("users").child(user.uid).updateChildren(mapOf("background" to url))
        }
    }

    fun updateUserName(name: String) {
        val user = auth.currentUser
        if (user != null) {
            database.getReference("users").child(user.uid)
                .updateChildren(mapOf("username" to name, "profileSetupComplete" to true))
        }
    }

    suspend fun getFollowersCount(): Int = suspendCoroutine { continuation ->
        val user = auth.currentUser
        if (user != null) {
            val reference = database.getReference("subscriptions")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var count = 0
                    for (snapshot in dataSnapshot.children) {
                        val followedUserId = snapshot.child("followedUserId").getValue(String::class.java)
                        if (followedUserId == user.uid) {
                            count++
                        }
                    }
                    continuation.resume(count)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.tag("Firebase error").e(databaseError.message)
                    continuation.resume(0)
                }
            })
        } else {
            continuation.resume(0)
        }
    }

    suspend fun getFollowingCount(): Int = suspendCoroutine { continuation ->
        val user = auth.currentUser
        if (user != null) {
            val reference = database.getReference("subscriptions")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var count = 0
                    for (snapshot in dataSnapshot.children) {
                        val userId = snapshot.child("userId").getValue(String::class.java)
                        if (userId == user.uid) {
                            count++
                        }
                    }
                    continuation.resume(count)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.tag("Firebase error").e(databaseError.message)
                    continuation.resume(0)
                }
            })
        } else {
            continuation.resume(0)
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

    suspend fun getAuthorById(id: String): User? {
        val snapshot = database.getReference("users").child(id).get().await()
        return snapshot.getValue(User::class.java)
    }

    suspend fun getCurrentUser(): User? = suspendCoroutine { continuation ->
        val user = auth.currentUser
        if (user != null) {
            val reference = database.getReference("users").child(user.uid)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    continuation.resume(user)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.tag("Firebase error").e(databaseError.message)
                    continuation.resume(null)
                }
            })
        } else {
            continuation.resume(null)
        }
    }
}
