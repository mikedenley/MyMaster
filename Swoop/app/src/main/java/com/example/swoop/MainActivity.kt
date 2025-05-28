package com.example.swoop

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast                     // ← Make sure this import is present
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Wire up the toolbar so the overflow menu shows
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Start Game button with error‑guard
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            try {
                startActivity(Intent(this, GameActivity::class.java))
                finish()  // optional: close MainActivity so focus moves cleanly
            } catch (ex: Exception) {
                // Log and toast the error so you can see what's going wrong
                android.util.Log.e("MainActivity", "Failed to launch GameActivity", ex)
                Toast.makeText(
                    this,
                    "Error launching game: ${ex.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
