package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    var _user = MutableLiveData<User>()
    val user : LiveData<User> get() = _user

    fun update(name: String) {
        _user.value = User(name);
    }
}