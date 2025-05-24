package com.example.swoop.data

/**
 * Manages scoring for the Swoop game.
 */
object ScoreManager {
    private val scores = mutableMapOf<Player, Int>()
    private val lastHands = mutableMapOf<Player, List<Card>>()
    private var swoopVal = 50

    /**
     * Call at the start of each round with the current wild-card point value.
     */
    fun initRound(value: Int) {
        swoopVal = value
        lastHands.clear()
    }

    /**
     * Record a play — no-op for now.
     * Suppress unused-parameter warnings.
     */
    @Suppress("UNUSED_PARAMETER")
    fun recordPlay(player: Player, cards: List<Card>) {
        // Intentionally no-op
    }

    /**
     * Record a swoop event — no-op for now.
     * Suppress unused-parameter warnings.
     */
    @Suppress("UNUSED_PARAMETER")
    fun recordSwoop(player: Player) {
        // Intentionally no-op
    }

    /**
     * After someone goes out, tally each remaining player's hand and update their total.
     * @param losers List of players who did not win this round
     */
    fun addRoundScore(losers: List<Player>) {
        losers.forEach { player ->
            val hand = player.hand.toList()
            lastHands[player] = hand
            val pts = hand.sumOf { card ->
                if (card.rank.isSwoop) swoopVal else card.rank.value
            }
            scores[player] = (scores[player] ?: 0) + pts
        }
    }

    /**
     * Detailed breakdown for the last round's hand of a given player.
     */
    fun breakdown(player: Player): List<Pair<Card, Int>> =
        lastHands[player]?.map { card ->
            card to if (card.rank.isSwoop) swoopVal else card.rank.value
        } ?: emptyList()

    /**
     * Total cumulative score for a player.
     */
    fun getTotal(player: Player): Int = scores[player] ?: 0

    /**
     * All players who have recorded scores so far.
     */
    fun getAllPlayers(): List<Player> = scores.keys.toList()
}
