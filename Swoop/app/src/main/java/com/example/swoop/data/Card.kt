package com.example.swoop.data

/**
 * A single playing card: must have a Rank and (for non‑Joker) a Suit.
 */
data class Card(
    val rank: Rank,
    val suit: Suit  // for Jokers you can still give a “suit” if you need to pick red vs black
)
