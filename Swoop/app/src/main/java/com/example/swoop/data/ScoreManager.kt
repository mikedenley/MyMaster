// File: app/src/main/java/com/example/swoop/data/ScoreManager.kt
package com.example.swoop.data

object ScoreManager {
    private var swoopValue: Int = 50
    private val roundScores = mutableMapOf<Player, Int>()
    private val totalScores = mutableMapOf<Player, Int>()

    /** Call once when the game (all rounds) starts to zero everyone. */
    fun initPlayers(players: List<Player>) {
        totalScores.clear()
        players.forEach { totalScores[it] = 0 }
    }

    fun initRound(swoopValue: Int) {
        this.swoopValue = swoopValue
        roundScores.clear()
    }

    fun recordPlay(player: Player, cards: List<Card>) { /* unchanged */ }

    @Suppress("UNUSED_PARAMETER")
    fun recordSwoop(player: Player) { /* no‐op */ }

    fun addRoundScore(losers: List<Player>) {
        losers.forEach { loser ->
            val pts = roundScores.getOrDefault(loser, 0)
            totalScores[loser] = totalScores.getOrDefault(loser, 0) + pts
        }
    }

    fun getRoundScores(): Map<Player, Int> = roundScores.toMap()
    fun getAllPlayersTotalScores(): Map<Player, Int> = totalScores.toMap()
}
