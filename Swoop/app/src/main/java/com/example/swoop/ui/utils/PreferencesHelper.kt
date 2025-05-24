package com.example.swoop.ui.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.swoop.data.AIPlayer

object PreferencesHelper{
    private fun prefs(ctx:Context)=PreferenceManager.getDefaultSharedPreferences(ctx)
    fun getNumPlayers(ctx:Context)=prefs(ctx).getString("pref_num_players","2")!!.toInt()
    fun getNumDecks(ctx:Context)=prefs(ctx).getString("pref_deck_count",null)?.toInt()?:(3+((getNumPlayers(ctx)-4+1)/2).coerceAtLeast(0))
    fun getNumCards(ctx:Context)=prefs(ctx).getString("pref_hand_size","19")!!.toInt()
    fun getSwoopValue(ctx:Context)=prefs(ctx).getString("pref_swoop_value","50")!!.toInt()
    fun getAIDifficulty(ctx:Context)=AIPlayer.Difficulty.valueOf(prefs(ctx).getString("pref_ai_difficulty","NEWBIE")!!)
    fun isLandscapeAllowed(ctx:Context)=prefs(ctx).getBoolean("pref_allow_landscape",true)
    fun isDeveloperMode(ctx:Context)=prefs(ctx).getBoolean("pref_dev_mode",false)
}