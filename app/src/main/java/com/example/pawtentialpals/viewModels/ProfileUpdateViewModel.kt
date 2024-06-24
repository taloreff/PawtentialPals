package com.example.pawtentialpals.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileUpdateViewModel : ViewModel() {
    private val _profileUpdated = MutableLiveData<Boolean>()

    fun setProfileUpdated(updated: Boolean) {
        _profileUpdated.value = updated
    }
}
