package com.example.swoop.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.swoop.R
import com.example.swoop.data.Card
import com.example.swoop.data.Rank
import com.example.swoop.data.Suit

class CardAdapter(
    private val context: Context,
    private val cards: List<Card>
) : BaseAdapter() {

    private val selectedPositions = mutableSetOf<Int>()

    override fun getCount() = cards.size
    override fun getItem(pos: Int) = cards[pos]
    override fun getItemId(pos: Int) = pos.toLong()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_card, parent, false)

        val ivCard = view.findViewById<ImageView>(R.id.iv_card)
        val tvRank = view.findViewById<TextView>(R.id.tv_rank)
        val card   = getItem(pos)

        // 1) Build the correct drawable name
        val suitPart = card.suit.name.lowercase() // hearts, diamonds, clubs, spades
        val resName = when (card.rank) {
            Rank.JOKER -> if (card.suit == Suit.HEARTS) "card_red_joker" else "card_black_joker"
            Rank.ACE   -> "card_ace_of_$suitPart"             // spelled‐out for Aces
            else       -> "card_${card.rank.value}_of_$suitPart"  // numeric for 2–10
        }

        @SuppressLint("DiscouragedApi")
        val drawableId = context.resources.getIdentifier(
            resName, "drawable", context.packageName
        ).takeIf { it != 0 } ?: R.drawable.card_back

        ivCard.setImageResource(drawableId)

        // 2) Overlay the numeric/text rank
        tvRank.text = when (card.rank) {
            Rank.JOKER -> "JK"
            else       -> card.rank.value.toString() // Ace==1, Face==10
        }
        tvRank.visibility = View.VISIBLE

        // 3) Highlight selection
        view.isSelected = selectedPositions.contains(pos)
        view.setOnClickListener {
            if (!selectedPositions.remove(pos)) selectedPositions.add(pos)
            notifyDataSetChanged()
        }

        return view
    }

    /** Returns whichever cards the user tapped */
    fun getSelectedCards(): List<Card> =
        selectedPositions.map { cards[it] }
}
