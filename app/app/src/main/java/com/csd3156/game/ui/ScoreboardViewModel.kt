package com.csd3156.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.game.data.AppDatabase
import com.csd3156.game.data.FirestoreRepository
import com.csd3156.game.data.ScoreEntity
import com.csd3156.game.data.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScoreboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScoreRepository
    private val firestoreRepository = FirestoreRepository()

    val topScores: StateFlow<List<ScoreEntity>>

    val globalScores: StateFlow<List<ScoreEntity>> = firestoreRepository.getTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isShowingGlobal = MutableStateFlow(false)
    val isShowingGlobal: StateFlow<Boolean> = _isShowingGlobal.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).scoreDao()
        repository = ScoreRepository(dao)
        topScores = repository.getTopScores()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addScore(playerName: String, score: Int) {
        viewModelScope.launch {
            repository.insertScore(playerName, score)
            firestoreRepository.submitScore(playerName, score)
        }
    }

    fun toggleScoreboard() {
        _isShowingGlobal.value = !_isShowingGlobal.value
    }

    //fun clearScores() {
    //    viewModelScope.launch {
    //        repository.clearScores()
    //    }
    //}
}
