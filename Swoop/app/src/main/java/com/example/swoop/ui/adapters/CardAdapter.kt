package com.example.swoop.ui.adapters

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
import com.example.swoop.ui.utils.toResourceId

class CardAdapter(
    private val ctx: Context,
    private val cards: List<Card>
) : BaseAdapter() {

    override fun getCount()    = cards.size
    override fun getItem(pos: Int)   = cards[pos]
    override fun getItemId(pos: Int) = pos.toLong()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(ctx)
            .inflate(R.layout.item_card, parent, false)

        val iv   = v.findViewById<ImageView>(R.id.iv_card)
        val tv   = v.findViewById<TextView>(R.id.tv_rank)
        val card = getItem(pos)

        // attempt to load the PNG
        val resId = card.toResourceId(ctx)
        if (resId != 0) {
            iv.setImageResource(resId)
            tv.visibility = View.GONE
        } else {
            iv.setImageResource(R.drawable.card_back)
            tv.text = when (card.rank) {
                Rank.ACE   -> "1"
                Rank.JACK  -> "J"
                Rank.QUEEN -> "Q"
                Rank.KING  -> "K"
                Rank.JOKER -> "JK"
                else       -> card.rank.value.toString()
            }
            tv.visibility = View.VISIBLE
        }
        return v
    }

    /** If you support selection… */
    fun getSelectedCards(): List<Card> = emptyList()
}
