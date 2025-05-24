package com.example.swoop.data

class AIPlayer(name: String, val difficulty: Difficulty) : Player(name) {
    enum class Difficulty { NEWBIE, CASUAL, PRO, SHARK }

    fun chooseMove(pileTop: Rank?): List<Card> {
        fun pick(src: List<Card>): List<Card> {
            val playable = src.filter { c ->
                c.rank.isSwoop || pileTop == null || c.rank.value <= pileTop.value
            }
            if (playable.isEmpty()) return emptyList()
            return when (difficulty) {
                Difficulty.NEWBIE  -> listOf(playable.random())
                Difficulty.CASUAL  -> {
                    val minV = playable.minOf { it.rank.value }
                    playable.filter { it.rank.value == minV }
                }
                Difficulty.PRO     -> {
                    val byRank = playable.groupBy { it.rank }
                    val swoop = byRank.keys.firstOrNull { it.isSwoop || byRank[it]!!.size >= 4 }
                    swoop?.let { byRank[it]!! } ?: byRank.maxByOrNull { it.key.value }!!.value
                }
                Difficulty.SHARK   -> {
                    val wild = playable.filter { it.rank.isSwoop }
                    if (wild.isNotEmpty()) return wild
                    val byRank = playable.groupBy { it.rank }
                    val group4 = byRank.keys.firstOrNull { byRank[it]!!.size >= 4 }
                    group4?.let { byRank[it]!! } ?: listOf(playable.maxByOrNull { it.rank.value }!!)
                }
            }
        }
        pick(hand).takeIf { it.isNotEmpty() }?.let { return it }
        pick(faceUp).takeIf { it.isNotEmpty() }?.let { return it }
        faceDown.firstOrNull()
            ?.takeIf { c -> c.rank.isSwoop || pileTop == null || c.rank.value <= pileTop.value }
            ?.let { return listOf(it) }
        return emptyList()
    }
}
