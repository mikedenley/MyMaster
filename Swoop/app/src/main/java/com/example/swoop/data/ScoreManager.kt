// File: app/src/main/java/com/example/swoop/data/ScoreManager.kt
package com.example.swoop.data

object ScoreManager {
    private var swoopValue: Int = 50
    private val roundScores   = mutableMapOf<Player, Int>()
    private val totalScores   = mutableMapOf<Player, Int>()


    /** Call once when the game (all rounds) starts to zero everyone. */
    fun initPlayers(players: List<Player>) {
        totalScores.clear()
        players.forEach { totalScores[it] = 0 }
    }

    fun initRound(swoopValue: Int) {
        this.swoopValue = swoopValue
        roundScores.clear()
    }

    fun recordPlay(player: Player, cards: List<Card>) {
        val points = cards.sumOf { it.rank.value }
        // round score
        roundScores[player] = (roundScores[player] ?: 0) + points
        // total score
        totalScores[player] = (totalScores[player] ?: 0) + points
    }

    fun recordSwoop(player: Player, bonus: Int = 0) {
        roundScores[player] = (roundScores[player] ?: 0) + bonus
        totalScores[player] = (totalScores[player] ?: 0) + bonus
    }

    fun addRoundScore(losers: List<Player>) {
        losers.forEach { loser ->
            // grab how many points they scored this round, defaulting to 0
            val pts = roundScores[loser] ?: 0

            // add that to their cumulative total (again defaulting to 0 if missing)
            totalScores[loser] = (totalScores[loser] ?: 0) + pts
        }
    }

    fun getRoundScores(): Map<Player, Int> = roundScores.toMap()

    /** Get an immutable snapshot of everyone’s totals */
    fun getAllPlayersTotalScores(): Map<Player, Int> = totalScores.toMap()
}
