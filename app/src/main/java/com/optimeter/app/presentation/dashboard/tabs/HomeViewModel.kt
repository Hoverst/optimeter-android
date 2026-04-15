package com.optimeter.app.presentation.dashboard.tabs

import androidx.lifecycle.SavedStateHandle
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
import javax.inject.Inject


data class HomeUiState(
    val isLoading: Boolean = false,
    val homes: List<Home> = emptyList(),
    val latestReadings: Map<MeterType, MeterReading> = emptyMap(),
    val allReadings: List<MeterReading> = emptyList(),
    val selectedHomeId: String? = null,
    val readingDifference: Double? = null,
    val error: String? = null,
    val selectedAnalyticsMeterType: MeterType = MeterType.ELECTRICITY
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Restore selectedHomeId from saved state
        val savedHomeId = savedStateHandle.get<String>("selectedHomeId")
        val savedAnalyticsMeterTypeStr = savedStateHandle.get<String>("selectedAnalyticsMeterType")
        val savedAnalyticsMeterType = savedAnalyticsMeterTypeStr?.let { 
            try { MeterType.valueOf(it) } catch (e: Exception) { null }
        }
        
        _uiState.update { 
            it.copy(
                selectedHomeId = savedHomeId,
                selectedAnalyticsMeterType = savedAnalyticsMeterType ?: MeterType.ELECTRICITY
            ) 
        }
        
        viewModelScope.launch {
            homeRepository.refreshTrigger.collect {
                loadHomes()
            }
        }
    }

    fun loadHomes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                homeRepository.getHomes().collect { homes ->
                    val currentSelectedId = _uiState.value.selectedHomeId
                    val updatedState = _uiState.value.copy(
                        isLoading = false, 
                        homes = homes, 
                        error = null
                    )
                    _uiState.update { updatedState }
                    
                    // Determine which home to load readings for
                    val homeIdToLoad = when {
                        currentSelectedId != null && homes.any { it.id == currentSelectedId } -> currentSelectedId
                        homes.isNotEmpty() -> homes.first().id
                        else -> null
                    }
                    
                    // Update selectedHomeId if it's not set yet and we have homes
                    if (updatedState.selectedHomeId == null && homes.isNotEmpty()) {
                        _uiState.update { it.copy(selectedHomeId = homes.first().id) }
                    }
                    
                    // Load readings for the appropriate home
                    homeIdToLoad?.let { loadLatestReadingsForHome(it) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadLatestReadingsForHome(homeId: String) {
        viewModelScope.launch {
            try {
                homeRepository.getLatestReadings(homeId).collect { readings ->
                    val readingMap = readings.associateBy { it.type }
                    _uiState.update { state ->
                        state.copy(latestReadings = readingMap)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading latest readings", e)
                // Don't clear existing readings on error - keep them visible
            }
        }
        
        // Also load all readings for analytics
        fetchAllReadings(homeId)
    }

    fun fetchAllReadings(homeId: String) {
        if (homeId.isBlank()) return
        viewModelScope.launch {
            try {
                homeRepository.getReadings(homeId).collect { readings ->
                    android.util.Log.d("Analytics", "Data received: ${readings.size} items")
                    _uiState.update { state ->
                        state.copy(allReadings = readings)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Analytics", "Error loading all readings", e)
            }
        }
    }

    fun addHome(name: String, address: String) {
        viewModelScope.launch {
            val newHome = Home(
                id = "",
                userId = "",
                name = name,
                address = address
            )
            val result = homeRepository.addHome(newHome)
            result
                .onSuccess {
                    loadHomes()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun removeHome(homeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = homeRepository.removeHome(homeId)
            result
                .onSuccess {
                    loadHomes()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun deleteReading(homeId: String, readingId: String) {
        viewModelScope.launch {
            val result = homeRepository.deleteReading(readingId)
            result.onFailure { e ->
                android.util.Log.e("HomeViewModel", "Error deleting reading", e)
            }
        }
    }

    fun saveReading(
        homeId: String,
        meterType: MeterType,
        value: Double,
        readingDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val reading = MeterReading(
                id = "",
                homeId = homeId,
                userId = "",
                type = meterType,
                value = value,
                readingDate = readingDate,
                imageUrl = null
            )
            val result = homeRepository.saveReading(reading)
            result.onFailure { e ->
                android.util.Log.e("HomeViewModel", "Error saving reading", e)
            }
        }
    }

    fun selectHome(homeId: String) {
        _uiState.update { it.copy(selectedHomeId = homeId) }
        savedStateHandle["selectedHomeId"] = homeId
        loadLatestReadingsForHome(homeId)
    }

    fun selectAnalyticsMeterType(type: MeterType) {
        _uiState.update { it.copy(selectedAnalyticsMeterType = type) }
        savedStateHandle["selectedAnalyticsMeterType"] = type.name
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}