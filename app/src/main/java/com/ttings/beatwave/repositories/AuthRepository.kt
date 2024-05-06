package com.ttings.beatwave.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ttings.beatwave.data.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    suspend fun signUpWithEmailAndPassword(email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).await()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun createUserDocument(user: User) {
        database.getReference("users").child(user.userId).setValue(user)
    }

    fun getUserDocument(userId: String, callback: (User?) -> Unit) {
        database.getReference("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }
}