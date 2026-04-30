package com.optimeter.app.presentation.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.domain.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val readingRepository: ReadingRepository
) : ViewModel() {

    private val homeId = savedStateHandle.get<String>("homeId")
    private val meterType = MeterType.valueOf(savedStateHandle.get<String>("meterType") ?: "GAS")

    private val _uiState = MutableStateFlow(ScannerState())
    val uiState: StateFlow<ScannerState> = _uiState.asStateFlow()

    private var timeoutJob: Job? = null
    private var isCheckingRealism = false

    init {
        startTimeoutTimer()
    }

    private fun startTimeoutTimer() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(20_000)
            _uiState.update { it.copy(showTimeoutDialog = true) }
        }
    }

    fun onDigitsDetected(digits: String) {
        // Ігноруємо нові зчитування, якщо вже стабільно, перевіряється реалістичність або відкриті діалоги
        if (_uiState.value.isStable || isCheckingRealism ||
            _uiState.value.showTimeoutDialog || _uiState.value.showUnrealisticDialog) {
            return
        }

        // Перший успішний результат скасовує таймаут повної невдачі
        timeoutJob?.cancel()
        timeoutJob = null

        val currentState = _uiState.value

        if (digits == currentState.detectedDigits) {
            val newCount = currentState.stableCount + 1
            if (newCount >= 3) {
                // Призупиняємо сканер і йдемо перевіряти історію
                isCheckingRealism = true
                _uiState.update { it.copy(stableCount = newCount) }
                checkRealismAndProceed(digits)
            } else {
                _uiState.update { it.copy(stableCount = newCount) }
            }
        } else {
            _uiState.update {
                currentState.copy(
                    detectedDigits = digits,
                    stableCount = 1,
                    isStable = false
                )
            }
        }
    }

    private fun checkRealismAndProceed(digits: String) {
        viewModelScope.launch {
            if (homeId == null) {
                // Якщо немає homeId, довіряємо OCR
                finalizeStabilization()
                return@launch
            }

            val history = readingRepository.getReadingsByType(homeId, meterType).firstOrNull()
                ?.sortedByDescending { it.readingDate }

            if (history.isNullOrEmpty()) {
                // Немає історії - перше зчитування
                finalizeStabilization()
                return@launch
            }

            val newValue = digits.toDoubleOrNull() ?: 0.0
            val lastReading = history.first()
            val lastValue = lastReading.value

            var isUnrealistic = false

            // Правило 1 — Значення не може зменшитись
            if (newValue < lastValue) {
                isUnrealistic = true
            } else {
                // Правило 2 — Аномальна дельта
                if (history.size > 1) {
                    val pairsCount = minOf(5, history.size - 1)
                    var totalDelta = 0.0
                    for (i in 0 until pairsCount) {
                        val current = history[i].value
                        val prev = history[i + 1].value
                        totalDelta += (current - prev).coerceAtLeast(0.0)
                    }
                    val avgDelta = totalDelta / pairsCount
                    val newDelta = newValue - lastValue

                    // Перевіряємо тільки якщо середня дельта більше нуля, щоб уникнути ділення/множення на нуль
                    if (avgDelta > 0 && newDelta > 5 * avgDelta) {
                        isUnrealistic = true
                    }
                }
            }

            if (isUnrealistic) {
                _uiState.update {
                    it.copy(
                        showUnrealisticDialog = true,
                        lastKnownValue = lastValue
                    )
                }
            } else {
                finalizeStabilization()
            }

            isCheckingRealism = false
        }
    }

    fun acceptUnrealisticValue() {
        _uiState.update { it.copy(showUnrealisticDialog = false) }
        finalizeStabilization()
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                showTimeoutDialog = false,
                showUnrealisticDialog = false
            )
        }
    }

    private fun finalizeStabilization() {
        _uiState.update { it.copy(isStable = true) }
    }

    fun onPhotoCaptured(path: String) {
        _uiState.update {
            it.copy(
                capturedPhotoPath = path,
                showValidation = true
            )
        }
    }

    fun resetScanner() {
        _uiState.value = ScannerState()
        isCheckingRealism = false
        startTimeoutTimer()
    }
}

data class ScannerState(
    val detectedDigits: String = "",
    val stableCount: Int = 0,
    val isStable: Boolean = false,
    val capturedPhotoPath: String? = null,
    val showValidation: Boolean = false,
    val showTimeoutDialog: Boolean = false,
    val showUnrealisticDialog: Boolean = false,
    val lastKnownValue: Double? = null
)