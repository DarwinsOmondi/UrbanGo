package com.example.urbango.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urbango.viewModels.DelayReportViewModel

class DelayReportViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DelayReportViewModel::class.java)) {
            return DelayReportViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}