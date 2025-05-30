package com.example.swoop

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.swoop.ui.adapters.CardAdapter
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.swoop.data.*

import com.example.swoop.ui.utils.PreferencesHelper
import com.example.swoop.ui.utils.toResourceId
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GameActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "GameActivity"
        private const val DRAW_DELAY_MS = 2500L
    }

    // UI refs
    private lateinit var toolbar: Toolbar
    private lateinit var gridStarterDraw: GridView
    private lateinit var scrollPlayers: ScrollView
    private lateinit var gridHand: GridView
    private lateinit var llPlayers: LinearLayout

    // game state
    private lateinit var deck: Deck
    private val players = mutableListOf<Player>()
    private var drawContestants: List<Player> = emptyList()
    private val lastDraws = mutableMapOf<Player, Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // 1) Wire up views
        toolbar         = findViewById(R.id.toolbar)
        gridStarterDraw = findViewById(R.id.grid_starter_draw)
        scrollPlayers   = findViewById(R.id.scroll_players)
        gridHand        = findViewById(R.id.grid_hand)
        llPlayers       = findViewById(R.id.llPlayers)

        setSupportActionBar(toolbar)

        // 2) Prepare deck & players
        setupGame()
        supportActionBar?.title = getString(
            R.string.game_title,
            PreferencesHelper.getNumDecks(this),
            PreferencesHelper.getNumPlayers(this),
            PreferencesHelper.getNumCards(this)
        )

        // 3) Kick off starter‑draw
        renderStarterDraw()
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = if (PreferencesHelper.isLandscapeAllowed(this))
            ActivityInfo.SCREEN_ORIENTATION_SENSOR
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreateOptionsMenu(menu: Menu) =
        menuInflater.inflate(R.menu.menu_game, menu).let { true }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val myScore = ScoreManager.getAllPlayersTotalScores()[players[0]] ?: 0
        menu.findItem(R.id.action_show_scores)
            ?.title = "Current Score: $myScore"
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_scores -> {
            showScoresDialog()
            true
        }
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showScoresDialog() {
        val msg = ScoreManager.getAllPlayersTotalScores()
            .entries.joinToString("\n") { (p, pts) -> "${p.name}: $pts" }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_show_scores)
            .setMessage(msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setupGame() {
        players.clear()
        players.add(Player(getString(R.string.player_you)))
        repeat(PreferencesHelper.getNumPlayers(this) - 1) { i ->
            players.add(AIPlayer(
                "${getString(R.string.player_cpu)}${i + 1}",
                PreferencesHelper.getAIDifficulty(this)
            ))
        }
        ScoreManager.initPlayers(players)
        ScoreManager.initRound(PreferencesHelper.getSwoopValue(this))

        deck = Deck(PreferencesHelper.getNumDecks(this)).apply { shuffle() }
        val handSize = PreferencesHelper.getNumCards(this)

        for (p in players) {
            val cards = List(handSize) { deck.draw()!! }.shuffled()
            p.faceDown.addAll(cards.take(4))
            p.faceUp.addAll(cards.drop(4).take(4))
            p.hand.addAll(cards.drop(8).sortedBy { it.rank.value })
        }
        drawContestants = emptyList()
        lastDraws.clear()
    }

    private fun renderStarterDraw() {
        if (drawContestants.isEmpty()) drawContestants = players.toList()
        lastDraws.clear()

        gridStarterDraw.adapter = object : BaseAdapter() {
            override fun getCount() = drawContestants.size
            override fun getItem(pos: Int) = drawContestants[pos]
            override fun getItemId(pos: Int) = pos.toLong()
            override fun getView(pos: Int, cv: View?, parent: ViewGroup): View {
                val view = cv ?: layoutInflater.inflate(
                    R.layout.item_starter_draw, parent, false
                )
                val player = drawContestants[pos]
                val card   = deck.draw()!!
                lastDraws[player] = card

                val iv  = view.findViewById<ImageView>(R.id.iv_card)
                val tv  = view.findViewById<TextView>(R.id.tv_rank)
                val res = card.toResourceId(this@GameActivity)
                if (res != 0) {
                    iv.setImageResource(res)
                    tv.visibility = View.GONE
                } else {
                    iv.setImageResource(R.drawable.card_back)
                    tv.visibility = View.VISIBLE
                    tv.text = when (card.rank) {
                        Rank.ACE   -> "1"
                        Rank.JACK  -> "J"
                        Rank.QUEEN -> "Q"
                        Rank.KING  -> "K"
                        Rank.JOKER -> "JK"
                        else       -> card.rank.value.toString()
                    }
                }

                view.findViewById<TextView>(R.id.tv_name).text = player.name
                return view
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val max = lastDraws.values.maxOf { it.rank.value }
            val winners = lastDraws.filter { it.value.rank.value == max }.keys.toList()
            if (winners.size > 1) {
                Toast.makeText(
                    this,
                    "Tie between ${winners.joinToString { it.name }}… redraw",
                    Toast.LENGTH_SHORT
                ).show()
                drawContestants = winners
                renderStarterDraw()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_draw_title)
                    .setMessage(R.string.dialog_draw_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.continue_button) { _, _ ->
                        showMainGameUI()
                    }
                    .show()
            }
        }, DRAW_DELAY_MS)
    }

    private fun showMainGameUI() {
        gridStarterDraw.visibility = View.GONE
        scrollPlayers.visibility   = View.VISIBLE

        // 1) Show human hand:
        gridHand.visibility = View.VISIBLE
        gridHand.adapter = CardAdapter(this, players[0].hand)

        // 2) Render 4‑pile row for you + each AI:
        llPlayers.removeAllViews()
        players.forEachIndexed { idx, player ->
            val row = layoutInflater.inflate(
                R.layout.item_player_row, llPlayers, false
            ).apply {
                findViewById<TextView>(R.id.tv_player_name).text =
                    if (idx == 0) getString(R.string.player_you) else player.name
            }

            val pileRow = row.findViewById<LinearLayout>(R.id.ll_face_piles)
            pileRow.removeAllViews()

            repeat(4) { slot ->
                val pv = layoutInflater.inflate(
                    R.layout.item_ai_pile, pileRow, false
                )
                val ivDown = pv.findViewById<ImageView>(R.id.iv_down)
                val ivUp   = pv.findViewById<ImageView>(R.id.iv_up)

                ivDown.setImageResource(R.drawable.card_back)
                if (slot < player.faceUp.size) {
                    ivUp.setImageResource(player.faceUp[slot].toResourceId(this))
                    ivUp.visibility = View.VISIBLE
                    ivDown.post {
                        ivUp.translationY = ivDown.height * 0.25f
                    }
                }
                pileRow.addView(pv)
            }
            llPlayers.addView(row)
        }
    }
}
