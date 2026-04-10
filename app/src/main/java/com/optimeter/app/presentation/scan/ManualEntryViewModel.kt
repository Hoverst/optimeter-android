package com.optimeter.app.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optimeter.app.domain.model.Home
import com.optimeter.app.domain.model.MeterReading
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class ManualEntryUiState(
    val isLoading: Boolean = false,
    val savedReading: MeterReading? = null,
    val isSaveSuccessful: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

    fun saveReading(
        homeId: String,
        meterType: MeterType,
        value: Double,
        readingDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
_uiState.update { it.copy(isLoading = true, error = null, isSaveSuccessful = false) }
            val reading = MeterReading(
                id = "",
                homeId = homeId,
                userId = "", // Will be populated from auth context if needed
                type = meterType,
                value = value,
                readingDate = readingDate,
                imageUrl = null
            )
            val result = homeRepository.saveReading(reading)
            result
.onSuccess { savedReading ->
    _uiState.update { it.copy(isLoading = false, savedReading = savedReading, isSaveSuccessful = true) }
}
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearState() {
        _uiState.update { ManualEntryUiState() }
    }
}