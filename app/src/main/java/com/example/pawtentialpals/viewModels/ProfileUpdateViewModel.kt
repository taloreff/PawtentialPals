package com.example.pawtentialpals.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileUpdateViewModel : ViewModel() {
    private val _profileUpdated = MutableLiveData<Boolean>()
    val profileUpdated: LiveData<Boolean> get() = _profileUpdated

    fun setProfileUpdated(updated: Boolean) {
        _profileUpdated.value = updated
    }
}
