package com.manishk9.bingo.auth

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.manishk9.bingo.R
import com.manishk9.bingo.ResultWrapper
import com.manishk9.bingo.model.User
import com.manishk9.bingo.repo.LoginRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: LoginRepo) : ViewModel() {

    val response = MutableLiveData<ResultWrapper<User>>()

    fun doLogin(token: AccessToken) {
        viewModelScope.launch {
            response.postValue(ResultWrapper.Loading)
            repo.doLogin(token).flatMapLatest {
                repo.saveUser(it)
            }.flowOn(Dispatchers.IO)
                .catch { e ->
                    if (e is FirebaseAuthUserCollisionException)
                        response.postValue(ResultWrapper.Error("An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address."))
                    else
                        response.postValue(ResultWrapper.Error("Something Went Wrong"))
                }
                .collect {
                    response.postValue(it)
                }
        }
    }

    fun doGoogleLogin(token: String) {
        viewModelScope.launch {
            response.postValue(ResultWrapper.Loading)
            repo.doGoogleLogin(token).flatMapLatest {
                repo.saveUser(it)
            }.flowOn(Dispatchers.IO)
                .catch { e ->
                    if (e is FirebaseAuthUserCollisionException)
                        response.postValue(ResultWrapper.Error("An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address."))
                    else
                        response.postValue(ResultWrapper.Error("Something Went Wrong"))
                }
                .collect {
                    response.postValue(it)
                }
        }
    }


}