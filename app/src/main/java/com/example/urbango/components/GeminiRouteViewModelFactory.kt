package com.example.urbango.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urbango.viewModels.GeminiRouteViewModel

class GeminiRouteViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeminiRouteViewModel::class.java)) {
            return GeminiRouteViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
