package com.dthurman.moviesaver.ui.features.feature_watchlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(

): ViewModel() {



}

sealed interface WatchlistUiState {



}