// File: app/src/main/java/com/example/swoop/ScoreBreakdownActivity.kt
package com.example.swoop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.swoop.data.ScoreManager
import com.example.swoop.data.Player

class ScoreBreakdownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_breakdown)

        // 0) Read Intent extras
        val winnerName = intent.getStringExtra("winner") ?: ""
        val isFinal    = intent.getBooleanExtra("final", false)

        // 1) Show winner at top
        val tvWinner = findViewById<TextView>(R.id.tv_winner)
        tvWinner.text = getString(
            if (isFinal) R.string.overall_scores
            else R.string.round_summary
        ) + "\n" + winnerName

        // 2) Populate round‐by‐round scores
        val roundContainer = findViewById<LinearLayout>(R.id.ll_round_summary)
        ScoreManager.getRoundScores().forEach { (player: Player, pts: Int) ->
            val tv = TextView(this).apply {
                textSize = 16f
                text = getString(R.string.player_round_score, player.name, pts)
            }
            roundContainer.addView(tv)
        }

        // 3) Populate overall totals
        val overallContainer = findViewById<LinearLayout>(R.id.ll_overall_scores)
        ScoreManager.getAllPlayersTotalScores().forEach { (player: Player, total: Int) ->
            val tv = TextView(this).apply {
                textSize = 16f
                text = getString(R.string.player_total_score, player.name, total)
            }
            overallContainer.addView(tv)
        }

        // 4) Next/Finish button
        val btnNext = findViewById<Button>(R.id.btn_next_round)
        btnNext.text = getString(if (isFinal) R.string.finish else R.string.next_round)
        btnNext.setOnClickListener {
            if (isFinal) {
                // Exit app (or back to main menu)
                finishAffinity()
            } else {
                // Start next round
                startActivity(Intent(this, GameActivity::class.java))
            }
            finish()
        }
    }
}
