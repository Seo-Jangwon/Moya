package com.ssafy.main.explorestart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.skele.moya.background.di.LocationManager
import com.skele.moya.background.di.StepManager
import com.ssafy.network.ApiResponse
import com.ssafy.network.repository.ExplorationRepository
import com.ssafy.network.request.ExplorationEndRequestBody
import com.ssafy.ui.explorestart.ExploreMarkerState
import com.ssafy.ui.explorestart.ExploreStartDialogState
import com.ssafy.ui.explorestart.ExploreStartScreenState
import com.ssafy.ui.explorestart.ExploreStartUserIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreStartScreenViewModel
    @Inject
    constructor(
        private val explorationRepository: ExplorationRepository,
        private val locationManager: LocationManager,
        private val stepManager: StepManager,
    ) : ViewModel() {
        private val _state = MutableStateFlow<ExploreStartScreenState>(ExploreStartScreenState.Loading)
        val state: StateFlow<ExploreStartScreenState> = _state

        private val _dialogState =
            MutableStateFlow<ExploreStartDialogState>(ExploreStartDialogState.Closed)
        val dialogState: StateFlow<ExploreStartDialogState> = _dialogState

        fun loadData(parkId: Long) {
            when (val currentState = _state.value) {
                is ExploreStartScreenState.Loaded ->
                    updateData(
                        explorationId = currentState.explorationId,
                        parkId = parkId,
                    )

                else -> loadInitialData(parkId)
            }
        }

        fun onIntent(intent: ExploreStartUserIntent) {
            when (intent) {
                is ExploreStartUserIntent.OnDialogDismissed -> {
                    _dialogState.value = ExploreStartDialogState.Closed
                }

                is ExploreStartUserIntent.OnExitClicked -> {
                    _dialogState.value = ExploreStartDialogState.Exit
                }

                is ExploreStartUserIntent.OnExitExplorationConfirmed -> {
                    _dialogState.value = ExploreStartDialogState.Closed
                    endExploration()
                }

                is ExploreStartUserIntent.OnOpenChallengeList -> {
                    _dialogState.value = ExploreStartDialogState.Challenge
                }
            }
        }

        fun enableSensor(context: Context) {
            if (state.value is ExploreStartScreenState.Loaded) return
            locationManager.initialize(context)
            locationManager.startTracking(context)

            stepManager.initializeStepSensor(context)
            stepManager.startCounting()
        }

        private fun disableSensor() {
            locationManager.stopTracking()
            stepManager.disposeStepSensor()
        }

        private fun loadInitialData(parkId: Long) {
            viewModelScope.launch {
                explorationRepository.startExploration(parkId).collectLatest { response ->
                    _state.value =
                        when (response) {
                            is ApiResponse.Success -> {
                                response.body?.let { body ->
                                    ExploreStartScreenState.Loaded(
                                        explorationId = body.id,
                                        npcPositions =
                                            body.npcs
                                                .flatMap { it.positions }
                                                .map { LatLng(it.latitude, it.longitude) },
                                        discoveredPositions =
                                            body.myDiscoveredSpecies
                                                .map {
                                                    ExploreMarkerState(
                                                        it.name,
                                                        it.imageUrl,
                                                        it.positions,
                                                    )
                                                },
                                        speciesPositions =
                                            body.species
                                                .flatMap { it.positions }
                                                .map { LatLng(it.latitude, it.longitude) },
                                    )
                                } ?: ExploreStartScreenState.Error("Failed to load initial data")
                            }

                            is ApiResponse.Error -> {
                                ExploreStartScreenState.Error(response.errorMessage ?: "Unknown error")
                            }
                        }
                }
            }
        }

        private fun updateData(
            explorationId: Long,
            parkId: Long,
        ) {
            viewModelScope.launch {
                explorationRepository
                    .getExplorationData(parkId = parkId, explorationId = explorationId)
                    .collectLatest { response ->
                        _state.value =
                            when (response) {
                                is ApiResponse.Success -> {
                                    response.body?.let { body ->
                                        ExploreStartScreenState.Loaded(
                                            explorationId = body.id,
                                            npcPositions =
                                                body.npcs
                                                    .flatMap { it.positions }
                                                    .map { LatLng(it.latitude, it.longitude) },
                                            discoveredPositions =
                                                body.myDiscoveredSpecies
                                                    .map {
                                                        ExploreMarkerState(
                                                            it.name,
                                                            it.imageUrl,
                                                            it.positions,
                                                        )
                                                    },
                                            speciesPositions =
                                                body.species
                                                    .flatMap { it.positions }
                                                    .map { LatLng(it.latitude, it.longitude) },
                                        )
                                    } ?: ExploreStartScreenState.Error("Failed to load initial data")
                                }

                                is ApiResponse.Error -> {
                                    ExploreStartScreenState.Error(
                                        response.errorMessage ?: "Unknown error",
                                    )
                                }
                            }
                    }
            }
        }

        private fun endExploration() {
            viewModelScope.launch {
                if (state.value is ExploreStartScreenState.Loaded) {
                    val uiState = state.value as ExploreStartScreenState.Loaded

                    explorationRepository
                        .endExploration(
                            uiState.explorationId,
                            body =
                                ExplorationEndRequestBody(
                                    route = locationManager.getLocationList(), // TODO : 이동경로 저장
                                    steps = stepManager.stepCount,
                                ),
                        ).collectLatest { response ->
                            when (response) {
                                is ApiResponse.Success -> {
                                    disableSensor()
                                    _state.value = ExploreStartScreenState.Exit
                                }

                                is ApiResponse.Error -> {
                                    // _state.value = ExploreStartScreenState.Exit
                                }
                            }
                        }
                }
            }
        }
    }
