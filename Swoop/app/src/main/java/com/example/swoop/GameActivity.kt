package com.example.swoop

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.swoop.data.*
import com.example.swoop.ui.adapters.CardAdapter
import com.example.swoop.ui.utils.PreferencesHelper
import com.example.swoop.ui.utils.TooltipHelper

class GameActivity : AppCompatActivity() {

    private lateinit var gvFaceDown: GridView
    private lateinit var gvFaceUp:   GridView
    private lateinit var gridView:   GridView
    private lateinit var tvPileTop:  TextView
    private lateinit var btnPlay:    Button

    private lateinit var deck: Deck
    private val players = mutableListOf<Player>()
    private val pile    = mutableListOf<Card>()
    private var currentPlayerIndex = 0

    override fun onResume() {
        super.onResume()
        requestedOrientation = if (PreferencesHelper.isLandscapeAllowed(this))
            ActivityInfo.SCREEN_ORIENTATION_SENSOR
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Bind the three grids and controls
        gvFaceDown = findViewById(R.id.grid_face_down)
        gvFaceUp   = findViewById(R.id.grid_face_up)
        gridView   = findViewById(R.id.grid_cards)
        tvPileTop  = findViewById(R.id.tv_pile_top)
        btnPlay    = findViewById(R.id.btn_play)

        // Deal & render initial state
        setupGame()
        renderFacePiles()
        renderPlayerHand()
        updatePileUI()

        // Tooltips
        tvPileTop.setOnLongClickListener {
            TooltipHelper.show(it, getString(R.string.tooltip_pile_top))
            true
        }
        gridView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, view, pos, _ ->
                val card = (gridView.adapter as CardAdapter).getItem(pos)
                TooltipHelper.show(
                    view,
                    "${card.rank.value} of ${card.suit.name.lowercase().replaceFirstChar { it.titlecase() }}"
                )
                true
            }
        btnPlay.setOnLongClickListener {
            TooltipHelper.show(it, getString(R.string.tooltip_play_button))
            true
        }

        btnPlay.setOnClickListener { onPlayClicked() }
    }

    private fun setupGame() {
        val numDecks = PreferencesHelper.getNumDecks(this)
        deck = Deck(numDecks).apply { shuffle() }
        ScoreManager.initRound(PreferencesHelper.getSwoopValue(this))

        players.clear()
        players.add(Player(getString(R.string.player_you)))
        repeat(PreferencesHelper.getNumPlayers(this) - 1) { i ->
            players.add(
                AIPlayer(
                    "${getString(R.string.player_cpu)}${i + 1}",
                    PreferencesHelper.getAIDifficulty(this)
                )
            )
        }

        val handSize = PreferencesHelper.getNumCards(this)
        players.forEach { p ->
            val cards = List(handSize) { deck.draw()!! }.shuffled()
            p.faceDown.addAll(cards.take(4))
            p.faceUp.addAll(cards.drop(4).take(4))
            p.hand.addAll(cards.drop(8).sortedBy { it.rank.value })
        }
        currentPlayerIndex = 0
    }

    private fun renderFacePiles() {
        // 4 facedown as backs
        gvFaceDown.adapter = object : BaseAdapter() {
            override fun getCount()    = players[currentPlayerIndex].faceDown.size
            override fun getItem(pos: Int) = players[currentPlayerIndex].faceDown[pos]
            override fun getItemId(pos: Int) = pos.toLong()
            override fun getView(pos: Int, cv: View?, parent: ViewGroup): View {
                val v = cv ?: LayoutInflater.from(this@GameActivity)
                    .inflate(R.layout.item_card, parent, false)
                v.findViewById<ImageView>(R.id.iv_card)
                    .setImageResource(R.drawable.card_back)
                v.findViewById<TextView>(R.id.tv_rank)
                    .visibility = View.INVISIBLE
                return v
            }
        }
        // 4 face‑up with real cards
        gvFaceUp.adapter = CardAdapter(
            this,
            players[currentPlayerIndex].faceUp
        )
    }

    private fun renderPlayerHand() {
        gridView.adapter = CardAdapter(
            this,
            players[currentPlayerIndex].hand
        )
    }

    private fun updatePileUI() {
        tvPileTop.text =
            pile.lastOrNull()?.rank?.value?.toString() ?: getString(R.string.pile_empty)
    }

    private fun onPlayClicked() {
        try {
            val player = players[currentPlayerIndex]
            val move = if (player is AIPlayer) {
                player.chooseMove(pile.lastOrNull()?.rank)
            } else {
                (gridView.adapter as CardAdapter).getSelectedCards()
            }

            if (move.isEmpty() && player !is AIPlayer) {
                player.hand.addAll(pile)
                pile.clear()
                advanceTurn()
            } else {
                playCards(player, move)
                checkSwoopOrAdvance(player, move)
                if (player.hand.isEmpty()) {
                    endRound(player)
                    return
                }
            }

            renderFacePiles()
            renderPlayerHand()
            updatePileUI()

        } catch (e: IllegalMoveException) {
            TooltipHelper.show(this, e.message ?: getString(R.string.error_invalid_move))
        } catch (t: Throwable) {
            if (PreferencesHelper.isDeveloperMode(this)) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error_dev_title))
                    .setMessage(android.util.Log.getStackTraceString(t))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            } else {
                TooltipHelper.show(this, getString(R.string.error_generic))
            }
        }
    }

    private fun playCards(player: Player, cards: List<Card>) {
        require(cards.isNotEmpty()) { getString(R.string.error_select_cards) }
        val rank = cards.first().rank
        require(cards.all { it.rank == rank }) {
            getString(R.string.error_mismatch_rank)
        }
        val top = pile.lastOrNull()?.rank
        if (top != null && !rank.isSwoop && rank.value > top.value) {
            throw IllegalMoveException(getString(R.string.error_too_high))
        }
        player.play(cards)
        pile.addAll(cards)
        ScoreManager.recordPlay(player, cards)
    }

    private fun checkSwoopOrAdvance(player: Player, cards: List<Card>) {
        val count = pile.count { it.rank == cards.first().rank }
        if (cards.first().rank.isSwoop || count >= 4) {
            pile.clear()
            ScoreManager.recordSwoop(player)
        } else {
            advanceTurn()
        }
    }

    private fun advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }

    private fun endRound(winner: Player) {
        ScoreManager.addRoundScore(players.filter { it != winner })
        startActivity(
            Intent(this, ScoreBreakdownActivity::class.java).apply {
                putExtra("winner", winner.name)
            }
        )
        finish()
    }
}
