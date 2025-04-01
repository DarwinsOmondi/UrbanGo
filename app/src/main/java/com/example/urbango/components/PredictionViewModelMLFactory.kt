package com.example.urbango.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urbango.viewModels.PredictionViewModelML

class PredictionViewModelMLFactory : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictionViewModelML::class.java)){
            return PredictionViewModelML() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}