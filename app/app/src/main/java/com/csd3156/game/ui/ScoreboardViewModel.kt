package com.csd3156.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.game.data.AppDatabase
import com.csd3156.game.data.ScoreEntity
import com.csd3156.game.data.ScoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScoreboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScoreRepository

    val topScores: StateFlow<List<ScoreEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).scoreDao()
        repository = ScoreRepository(dao)
        topScores = repository.getTopScores()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addScore(playerName: String, score: Int) {
        viewModelScope.launch {
            repository.insertScore(playerName, score)
        }
    }

    fun clearScores() {
        viewModelScope.launch {
            repository.clearScores()
        }
    }
}
