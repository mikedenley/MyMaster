package com.example.swoop.ui.utils

import android.content.Context
import android.util.Log
import com.example.swoop.data.Card
import com.example.swoop.data.Rank
import com.example.swoop.data.Suit

/**
 * Maps a Card to the proper R.drawable resource.
 *  • Ranks:  ace,2–10,jack,queen,king  (joker handled specially)
 *  • Suits:  clubs,diamonds,hearts,spades
 *  • Jokers: card_red_joker, card_black_joker
 */
fun Card.toResourceId(ctx: Context): Int {
    val pkg = ctx.packageName

    // 1) Joker?
    if (rank == Rank.JOKER) {
        val resName = if (suit == Suit.HEARTS || suit == Suit.DIAMONDS)
            "card_red_joker" else "card_black_joker"
        val id = ctx.resources.getIdentifier(resName, "drawable", pkg)
        if (id != 0) return id
        Log.w("CardExtensions", "Missing $resName, falling back to card_back")
        return ctx.resources.getIdentifier("card_back", "drawable", pkg)
    }

    // 2) Build base name for non‑joker
    val rankPart = when (rank) {
        Rank.ACE   -> "ace"
        Rank.JACK  -> "jack"
        Rank.QUEEN -> "queen"
        Rank.KING  -> "king"
        else       -> rank.value.toString()   // "2" … "10"
    }

    // 3) Try plural suit
    val suitName = suit.name.lowercase()                  // "clubs", …
    var name = "card_${rankPart}_of_${suitName}"
    var id = ctx.resources.getIdentifier(name, "drawable", pkg)
    if (id != 0) return id

    // 4) Fallback to singular ("club" vs "clubs")
    val singular = suitName.removeSuffix("s")
    name = "card_${rankPart}_of_${singular}"
    id = ctx.resources.getIdentifier(name, "drawable", pkg)
    if (id != 0) return id

    // 5) Nothing matched: fallback
    Log.w("CardExtensions", "Missing $name, falling back to card_back")
    return ctx.resources.getIdentifier("card_back", "drawable", pkg)
}
