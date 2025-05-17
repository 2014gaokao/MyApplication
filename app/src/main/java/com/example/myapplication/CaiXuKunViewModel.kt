package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CaiXuKunViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    // 使用 MutableLiveData 来保存数据
    private var _user = MutableLiveData<User>()

    // 公共的 LiveData 用于暴露数据
    val user : LiveData<User> get() = _user

    // 更新数据的方法
    fun updateUser(name: String) {
        _user.postValue(User(name))
    }
}