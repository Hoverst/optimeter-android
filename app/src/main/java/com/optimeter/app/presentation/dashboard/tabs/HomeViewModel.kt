package com.optimeter.app.presentation.dashboard.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optimeter.app.domain.model.Home
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
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomes()
    }

    fun loadHomes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                homeRepository.getHomes().collect { homes ->
                    _uiState.update { state ->
                        state.copy(isLoading = false, homes = homes, error = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
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
                    // Reload homes so UI reflects the newly added home.
                    loadHomes()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun removeHome(homeId: String) {
        viewModelScope.launch {
            val result = homeRepository.removeHome(homeId)
            result.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

