package com.example.urbango.model

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}