package com.manishk9.bingo.repo

import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.manishk9.bingo.ResultWrapper
import com.manishk9.bingo.model.User
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

open class LoginRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {


    fun doLogin(token: AccessToken) = flow {
        val credentials = FacebookAuthProvider.getCredential(token.token)
        val result = auth.signInWithCredential(credentials).await()
        result.user?.let {
            emit(it)
        }
    }

    fun doGoogleLogin(token:String) = flow {
        val credentials = GoogleAuthProvider.getCredential(token,null)
        val result = auth.signInWithCredential(credentials).await()
        result.user?.let {
            emit(it)
        }
    }

    fun saveUser(userDetails: FirebaseUser) = flow {
        val uid = userDetails.uid
        val user = User(
            uid,
            userDetails.displayName,
            userDetails.email,
            userDetails.photoUrl?.toString()
        )
        try {
            db.collection("users").document(uid).set(user).await()
            emit(ResultWrapper.Success(user))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e.message ?: "Some thing went wrong"))
        }

    }
}