package com.example.urbango.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urbango.viewModels.UserPointsViewModel

class UserPointsViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserPointsViewModel::class.java)) {
            return UserPointsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}