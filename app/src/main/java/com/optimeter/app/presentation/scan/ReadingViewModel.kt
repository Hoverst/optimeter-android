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

data class ReadingUiState(
    val isLoading: Boolean = false,
    val savedReading: MeterReading? = null,
    val error: String? = null
)

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()

    fun saveReading(
        homeId: String,
        meterType: MeterType,
        value: Double,
        readingDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
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
                    _uiState.update { it.copy(isLoading = false, savedReading = savedReading) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearState() {
        _uiState.update { ReadingUiState() }
    }
}