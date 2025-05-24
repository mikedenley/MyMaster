package com.example.swoop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.swoop.R
import com.example.swoop.data.ScoreManager

class ScoreBreakdownActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_breakdown)

        val winner = intent.getStringExtra("winner") ?: ""
        findViewById<TextView>(R.id.tv_winner).apply {
            text = getString(R.string.title_score_breakdown)
        }

        val roundContainer = findViewById<LinearLayout>(R.id.ll_round_summary)
        ScoreManager.getAllPlayers()
            .filter { it.name != winner }
            .forEach { player ->
                val score = ScoreManager.breakdown(player).sumOf { it.second }
                // Header per player
                roundContainer.addView(TextView(this).apply {
                    text = getString(R.string.player_round_score, player.name, score)
                    textSize = 18f
                    setPadding(0, 16, 0, 8)
                })
                // Breakdown details
                ScoreManager.breakdown(player).forEach { (card, pts) ->
                    roundContainer.addView(TextView(this).apply {
                        text = getString(
                            R.string.player_card_score,
                            card.rank.name,
                            card.suit,
                            pts
                        )
                        setPadding(32, 4, 0, 4)
                    })
                }
            }

        val overallContainer = findViewById<LinearLayout>(R.id.ll_overall_scores)
        ScoreManager.getAllPlayers().forEach { player ->
            overallContainer.addView(TextView(this).apply {
                text = getString(R.string.player_total_score, player.name, ScoreManager.getTotal(player))
                textSize = 16f
                setPadding(0, 8, 0, 8)
            })
        }

        findViewById<Button>(R.id.btn_next_round).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }
    }
}
