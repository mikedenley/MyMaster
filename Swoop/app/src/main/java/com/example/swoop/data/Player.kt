package com.example.swoop.data

/** Must be open so AIPlayer can inherit from it */
open class Player(val name: String) {
    val faceDown = mutableListOf<Card>()
    val faceUp   = mutableListOf<Card>()
    val hand     = mutableListOf<Card>()

    open fun play(cards: List<Card>) {
        cards.forEach { c ->
            when {
                hand.remove(c)     -> {}
                faceUp.remove(c)   -> {}
                faceDown.remove(c) -> {}
            }
        }
    }
}
