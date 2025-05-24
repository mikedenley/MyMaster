package com.example.swoop.data

enum class Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

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
    TEN(10, true),
    JACK(10),
    QUEEN(10),
    KING(10),
    JOKER(0, true)
}

data class Card(val rank: Rank, val suit: Suit)
