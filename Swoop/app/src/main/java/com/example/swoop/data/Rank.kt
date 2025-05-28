package com.example.swoop.data

/**
 * Card ranks with their numeric values.
 *  • value: used for draw‑to‑lead comparisons
 *  • isSwoop: true for 10s & Jokers
 */
enum class Rank(val value: Int, val isSwoop: Boolean = false) {
    ACE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10, isSwoop = true),
    JACK(11),
    QUEEN(12),
    KING(13),
    JOKER(14, isSwoop = true);
}
