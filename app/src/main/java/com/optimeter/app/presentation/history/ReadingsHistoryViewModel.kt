package com.optimeter.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optimeter.app.domain.model.MeterReading
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingsHistoryViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _selectedHomeId = MutableStateFlow<String?>(null)
    private val _selectedMeterType = MutableStateFlow<MeterType>(MeterType.WATER)
    
    val selectedMeterType: StateFlow<MeterType> = _selectedMeterType.asStateFlow()

    init {
        // Fetch first home automatically just to have a homeId
        viewModelScope.launch {
            homeRepository.getHomes().collect { homes ->
                if (homes.isNotEmpty() && _selectedHomeId.value == null) {
                    _selectedHomeId.value = homes.first().id
                }
            }
        }
    }

    fun selectMeterType(type: MeterType) {
        android.util.Log.d("HistoryDebug", "selectMeterType called with: $type")
        _selectedMeterType.value = type
    }

    fun setHomeId(homeId: String) {
        android.util.Log.d("HistoryDebug", "setHomeId called with: $homeId")
        _selectedHomeId.value = homeId
    }

    // Fully reactive query that re-runs when _selectedMeterType changes
    val readings: StateFlow<List<MeterReading>> = combine(
        _selectedHomeId.filterNotNull(),
        _selectedMeterType
    ) { homeId, meterType ->
        android.util.Log.d("HistoryDebug", "combine emitted homeId: $homeId, meterType: $meterType")
        homeId to meterType
    }.flatMapLatest { (homeId, meterType) ->
        android.util.Log.d("HistoryDebug", "flatMapLatest starting query for homeId: $homeId, meterType: $meterType")
        homeRepository.getReadings(homeId).map { allReadings ->
            android.util.Log.d("HistoryDebug", "homeRepository.getReadings returned ${allReadings.size} total readings")
            val filtered = allReadings
                .filter { it.type == meterType }
                .sortedByDescending { it.readingDate }
            android.util.Log.d("HistoryDebug", "Filtered readings size: ${filtered.size} for meterType: $meterType")
            filtered
        }.catch { e -> 
            android.util.Log.e("HistoryDebug", "Error in getReadings flow", e)
            emit(emptyList()) 
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun deleteReading(readingId: String) {
        viewModelScope.launch {
            val result = homeRepository.deleteReading(readingId)
            if (result.isSuccess) {
                // Force refresh
                val current = _selectedHomeId.value
                _selectedHomeId.value = null
                _selectedHomeId.value = current
            }
        }
    }
}
