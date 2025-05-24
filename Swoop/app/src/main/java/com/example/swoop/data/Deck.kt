package com.example.swoop.data

class Deck(deckCount: Int = 3) {
    // Internal list of cards
    private val cards = mutableListOf<Card>()

    init {
        repeat(deckCount) {
            // Add the 52 standard cards
            Suit.values().forEach { suit ->
                Rank.values()
                    .filterNot { it == Rank.JOKER }
                    .forEach { rank ->
                        cards += Card(rank, suit)
                    }
            }
            // Plus two jokers per deck (use HEARTS as “red”, SPADES as “black”)
            cards += Card(Rank.JOKER, Suit.HEARTS)
            cards += Card(Rank.JOKER, Suit.SPADES)
        }
    }

    /** Shuffle all remaining cards. */
    fun shuffle() {
        cards.shuffle()
    }

    /** Draw the top card, or null if empty. */
    fun draw(): Card? =
        if (cards.isNotEmpty()) cards.removeAt(0) else null

    /**
     * How many cards are left in the deck.
     * Useful for logging or game‑state checks.
     */
    fun remainingCount(): Int = cards.size
}
