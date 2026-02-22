package com.csd3156.game.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val scoresCollection = db.collection("scores")

    suspend fun submitScore(playerName: String, score: Int) {
        try {
            val data = hashMapOf(
                "playerName" to playerName,
                "score" to score,
                "timestamp" to System.currentTimeMillis()
            )
            scoresCollection.add(data).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to submit score", e)
        }
    }

    fun getTopScores(limit: Int = 10): Flow<List<ScoreEntity>> = callbackFlow {
        val registration = scoresCollection
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRepository", "Failed to fetch scores", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val scores = snapshot?.documents?.map { doc ->
                    ScoreEntity(
                        playerName = doc.getString("playerName") ?: "Unknown",
                        score = (doc.getLong("score") ?: 0).toInt()
                    )
                } ?: emptyList()
                trySend(scores)
            }
        awaitClose { registration.remove() }
    }
}
