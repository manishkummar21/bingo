package com.manishk9.bingo.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.manishk9.bingo.ResultWrapper
import com.manishk9.bingo.model.User
import com.manishk9.bingo.repo.LoginRepo
import dagger.hilt.android.lifecycle.HiltViewModel
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
                .catch {
                    response.postValue(ResultWrapper.Error("Some thing went wrong"))
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
                .catch {
                    response.postValue(ResultWrapper.Error("Some thing went wrong"))
                }
                .collect {
                    response.postValue(it)
                }
        }
    }


}