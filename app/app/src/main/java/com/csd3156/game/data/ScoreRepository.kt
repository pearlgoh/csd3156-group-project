package com.csd3156.game.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    fun getTopScores(limit: Int = 10): Flow<List<ScoreEntity>> = scoreDao.getTopScores(limit)

    suspend fun insertScore(playerName: String, score: Int) {
        scoreDao.insertScore(ScoreEntity(playerName = playerName, score = score))
    }

    suspend fun clearScores() {
        scoreDao.deleteAllScores()
    }
}
