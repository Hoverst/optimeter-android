package com.optimeter.app.presentation.scan

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerState())
    val uiState: StateFlow<ScannerState> = _uiState.asStateFlow()

    fun onDigitsDetected(digits: String) {
        val currentState = _uiState.value
        
        if (digits == currentState.detectedDigits) {
            val newCount = currentState.stableCount + 1
            _uiState.value = currentState.copy(
                stableCount = newCount,
                isStable = newCount >= 3
            )
        } else {
            _uiState.value = currentState.copy(
                detectedDigits = digits,
                stableCount = 1,
                isStable = false
            )
        }
    }
    
    fun onPhotoCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            capturedPhotoPath = path,
            showValidation = true
        )
    }

    fun resetScanner() {
        _uiState.value = ScannerState()
    }
}

data class ScannerState(
    val detectedDigits: String = "",
    val stableCount: Int = 0,
    val isStable: Boolean = false,
    val capturedPhotoPath: String? = null,
    val showValidation: Boolean = false
)
