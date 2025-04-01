package com.example.urbango.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urbango.viewModels.PermissionViewModel

class PermissionViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)){
            return  PermissionViewModel() as T
        }
        throw IllegalArgumentException("")
    }
}