package com.example.lightningtracker.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lightningtracker.domain.model.LightningStrike
import com.example.lightningtracker.domain.usecase.GetHistoricalStrikesUseCase
import com.example.lightningtracker.domain.usecase.GetLiveStrikesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class MapState(
    val strikes: List<LightningStrike> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getHistoricalStrikesUseCase: GetHistoricalStrikesUseCase,
    private val getLiveStrikesUseCase: GetLiveStrikesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state = _state.asStateFlow()

    init {
        getHistoricalStrikes()
        observeLiveStrikes()
    }

    private fun getHistoricalStrikes() {
        getHistoricalStrikesUseCase().onEach { strikes ->
            _state.value = _state.value.copy(
                strikes = strikes
            )
        }.launchIn(viewModelScope)
    }

    private fun observeLiveStrikes() {
        getLiveStrikesUseCase().onEach { newStrike ->
            _state.value = _state.value.copy(
                strikes = _state.value.strikes + newStrike
            )
        }.launchIn(viewModelScope)
    }
} 