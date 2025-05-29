// File: app/src/main/java/com/example/swoop/GameActivity.kt
package com.example.swoop

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.swoop.data.*
import com.example.swoop.ui.adapters.CardAdapter
import com.example.swoop.ui.utils.PreferencesHelper
import com.example.swoop.ui.utils.TooltipHelper
import android.view.Menu
import android.view.MenuItem
import com.example.swoop.data.ScoreManager
import com.example.swoop.ui.utils.toResourceId
//import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GameActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "GameActivity"
        private const val DRAW_DELAY_MS = 2500L
    }

    // UI
    private lateinit var toolbar: Toolbar
    private lateinit var gvStarterDraw: GridView
    private lateinit var gvFaceDown: GridView
    private lateinit var gvFaceUp: GridView
    private lateinit var gvHand: GridView
    private lateinit var tvPileTop: TextView
    private lateinit var btnPlay: Button
    private lateinit var llAiPlayers: LinearLayout
    private lateinit var scrollAiPlayers: HorizontalScrollView

    // Game state
    private lateinit var deck: Deck
    private val players = mutableListOf<Player>()
    private val pile = mutableListOf<Card>()
    private var currentPlayerIndex = 0

    // Starter‐draw tie handling
    private var drawContestants: List<Player> = emptyList()
    private val lastDraws = mutableMapOf<Player, Card>()

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
        scrollAiPlayers = findViewById(R.id.scroll_ai_players)
        llAiPlayers      = findViewById(R.id.ll_ai_players)

        // 0) Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 1) Bind views
        gvStarterDraw = findViewById(R.id.grid_starter_draw)
        gvFaceDown    = findViewById(R.id.grid_face_down)
        gvFaceUp      = findViewById(R.id.grid_face_up)
        gvHand        = findViewById(R.id.grid_cards)
        tvPileTop     = findViewById(R.id.tv_pile_top)
        btnPlay       = findViewById(R.id.btn_play)

        scrollAiPlayers.isHorizontalScrollBarEnabled = true
        scrollAiPlayers.isVerticalScrollBarEnabled   = false
        scrollAiPlayers.isScrollbarFadingEnabled     = false
        scrollAiPlayers.overScrollMode               = View.OVER_SCROLL_ALWAYS
        scrollAiPlayers.scrollBarStyle               = View.SCROLLBARS_OUTSIDE_INSET
        scrollAiPlayers.scrollBarSize                = resources.getDimensionPixelSize(R.dimen.ai_scrollbar_size)

        // 2) Deal & initialize
        setupGame()
        supportActionBar?.title = getString(
            R.string.game_title,
            PreferencesHelper.getNumDecks(this),
            PreferencesHelper.getNumPlayers(this),
            PreferencesHelper.getNumCards(this)
        )

        // 3) Hide main‐game UI
        listOf(gvFaceDown, gvFaceUp, gvHand, tvPileTop, btnPlay).forEach {
            it.visibility = View.GONE
        }

        // 4) Begin starter‐draw phase
        renderStarterDraw()

        // 5) Play button only after draw
        btnPlay.setOnClickListener { onPlayClicked() }
        btnPlay.setOnLongClickListener {
            TooltipHelper.show(it, getString(R.string.tooltip_play_button))
            true
        }
    }

    // -- Menu: Show current scores during play --
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d("GameActivity","onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_game, menu)
        menu.findItem(R.id.action_show_scores)?.title = "Current Score"
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_show_scores -> {
            showScores()
            true
        }
        R.id.action_help -> {
            showHelpDialog()
            true
        }
        R.id.action_settings -> {
            // existing settings logic
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /** Pops up a dialog showing each player’s cumulative total */
    private fun showScores() {
        val msg = ScoreManager
            .getAllPlayersTotalScores()
            .entries
            .joinToString("\n") { (p, pts) -> "${p.name}: $pts" }

        AlertDialog.Builder(this)
            .setTitle(R.string.action_show_scores)
            .setMessage(msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setupGame() {
        // build players list
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
        // init scoring
        ScoreManager.initPlayers(players)
        ScoreManager.initRound(PreferencesHelper.getSwoopValue(this))
        // build & shuffle deck
        deck = Deck(PreferencesHelper.getNumDecks(this)).apply { shuffle() }

        // deal each player's cards
        val handSize = PreferencesHelper.getNumCards(this)
        for (p in players) {
            val cards = List(handSize) { deck.draw()!! }.shuffled()
            p.faceDown.addAll(cards.take(4))
            p.faceUp  .addAll(cards.drop(4).take(4))
            p.hand    .addAll(cards.drop(8).sortedBy { it.rank.value })
        }
        currentPlayerIndex = 0
        drawContestants = emptyList()
        lastDraws.clear()
    }

    private fun renderStarterDraw() {
        if (drawContestants.isEmpty()) {
            drawContestants = players.toList()
        }
        lastDraws.clear()

        gvStarterDraw.adapter = object : BaseAdapter() {
            override fun getCount()    = drawContestants.size
            override fun getItem(pos: Int) = drawContestants[pos]
            override fun getItemId(pos: Int) = pos.toLong()
            override fun getView(pos: Int, cv: View?, parent: ViewGroup): View {
                val v = cv ?: layoutInflater.inflate(
                    R.layout.item_starter_draw, parent, false
                )
                val player = drawContestants[pos]
                val card = deck.draw()!!
                lastDraws[player] = card

                val iv     = v.findViewById<ImageView>(R.id.iv_card)
                val resId  = card.toResourceId(this@GameActivity)
                iv.setImageResource(if (resId != 0) resId else R.drawable.card_back)

                val tvRank = v.findViewById<TextView>(R.id.tv_rank)
                if (resId != 0) {
                    tvRank.visibility = View.GONE
                } else {
                    tvRank.text = when (card.rank) {
                        Rank.ACE   -> "1"
                        Rank.JACK  -> "J"
                        Rank.QUEEN -> "Q"
                        Rank.KING  -> "K"
                        Rank.JOKER -> "JK"
                        else       -> card.rank.value.toString()
                    }
                    tvRank.visibility = View.VISIBLE
                }

                v.findViewById<TextView>(R.id.tv_name).text = player.name
                return v
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // tie‐breaker logic
            val max = lastDraws.values.maxOf { it.rank.value }
            val winners = lastDraws.filter { it.value.rank.value == max }.keys.toList()
            if (winners.size > 1) {
                val names = winners.joinToString(", ") { it.name }
                Toast.makeText(this, "Tie between $names — redrawing…", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Tie among: $names")
                drawContestants = winners
                renderStarterDraw()
            } else {
                // clear dimming
                val dlg = AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_draw_title)
                    .setMessage(R.string.dialog_draw_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.continue_button) { d, _ ->
                        d.dismiss()
                        showMainGameUI()
                    }
                    .create()
                dlg.show()
                dlg.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }, DRAW_DELAY_MS)
    }

    private fun showMainGameUI() {
        gvStarterDraw.visibility = View.GONE
        listOf(gvFaceDown, gvFaceUp, gvHand, tvPileTop, btnPlay)
            .forEach { it.visibility = View.VISIBLE }
        scrollAiPlayers.visibility = View.VISIBLE
        renderAIPiles()
        renderFacePiles()
        renderPlayerHand()
        updatePileUI()
    }

    private fun renderAIPiles() {
        llAiPlayers.removeAllViews()
        for (i in 1 until players.size) {
            val ai = players[i]

// 1) inflate the AI strip
            val aiView = layoutInflater.inflate(R.layout.item_ai_piles, llAiPlayers, false)
            aiView.findViewById<TextView>(R.id.tv_ai_name).text = ai.name

// 2) fill its 4 piles
            val pileContainer = aiView.findViewById<LinearLayout>(R.id.ll_ai_piles)
            pileContainer.removeAllViews()

            for (slot in 0 until 4) {
                val pileView = layoutInflater.inflate(
                    R.layout.item_ai_pile, pileContainer, false
                )
                val ivDown = pileView.findViewById<ImageView>(R.id.iv_down)
                val ivUp   = pileView.findViewById<ImageView>(R.id.iv_up)

                // always show the back
                ivDown.setImageResource(R.drawable.card_back)

                // if this slot has a face‑up card, show & position it
                if (slot < ai.faceUp.size) {
                    val card = ai.faceUp[slot]
                    ivUp.setImageResource(card.toResourceId(this))
                    ivUp.visibility = View.VISIBLE

                    // after layout, nudge the face‑up down by half the back‑card height
                    ivDown.post {
                        ivUp.translationY = (ivDown.height / 3f)
                    }
                }

                pileContainer.addView(pileView)
            }

            llAiPlayers.addView(aiView)
        }

        // Log widths for scrollbar debugging
        scrollAiPlayers.post {
            val cW = scrollAiPlayers.width
            val cntW = llAiPlayers.width
            Log.d(TAG,"AI strip: container width=$cW, content width=$cntW")
        }
    Log.d(TAG, "renderAIPiles: added ${llAiPlayers.childCount} AI strips")
    }

    private fun renderFacePiles() {
        gvFaceDown.adapter = object : BaseAdapter() {
            override fun getCount()    = players[currentPlayerIndex].faceDown.size
            override fun getItem(pos: Int) = players[currentPlayerIndex].faceDown[pos]
            override fun getItemId(pos: Int) = pos.toLong()
            override fun getView(pos: Int, cv: View?, parent: ViewGroup): View {
                val v = cv ?: layoutInflater.inflate(R.layout.item_card, parent, false)
                v.findViewById<ImageView>(R.id.iv_card).setImageResource(R.drawable.card_back)
                v.findViewById<TextView>(R.id.tv_rank).visibility = View.INVISIBLE
                return v
            }
        }
        gvFaceUp.adapter = CardAdapter(this, players[currentPlayerIndex].faceUp)
    }

    private fun renderPlayerHand() {
        gvHand.adapter = CardAdapter(this, players[currentPlayerIndex].hand)
    }

    private fun updatePileUI() {
        tvPileTop.text = pile.lastOrNull()?.rank?.value?.toString()
            ?: getString(R.string.pile_empty)
        tvPileTop.setOnLongClickListener {
            TooltipHelper.show(it, getString(R.string.tooltip_pile_top))
            true
        }
    }
    //  -- add this if you want to update the title each time the menu opens
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        Log.d("GameActivity","onPrepareOptionsMenu")
        super.onPrepareOptionsMenu(menu)
        // e.g. show your own running score from ScoreManager
        val myScore = ScoreManager
        .getAllPlayersTotalScores()[players[0]]
        ?: 0
        menu.findItem(R.id.action_show_scores)
        ?.title = "Current Score: $myScore"
        return true
    }
    private fun onPlayClicked() {
        try {
            val player = players[currentPlayerIndex]
            val move = if (player is AIPlayer)
                player.chooseMove(pile.lastOrNull()?.rank)
            else
                (gvHand.adapter as CardAdapter).getSelectedCards()

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
            // force Android to re‑run onPrepareOptionsMenu()
            invalidateOptionsMenu()
        } catch (e: IllegalMoveException) {
            TooltipHelper.show(this, e.message ?: getString(R.string.error_invalid_move))
        } catch (t: Throwable) {
            if (PreferencesHelper.isDeveloperMode(this)) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error_dev_title))
                    .setMessage(Log.getStackTraceString(t))
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

        val endTotal = PreferencesHelper.getGameEndTotal(this)
        val someoneOver = ScoreManager
            .getAllPlayersTotalScores()
            .values.any { it >= endTotal }

        Intent(this, ScoreBreakdownActivity::class.java).also { intent ->
            intent.putExtra("winner", winner.name)
            if (someoneOver) intent.putExtra("final", true)
            startActivity(intent)
        }
        finish()
    }
    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("How to Play")
            .setMessage("On each turn, select cards in your hand that match or beat the pile's top card. Tap PLAY to make your move. Tap ⋮ → Current Score to see your running total.")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
