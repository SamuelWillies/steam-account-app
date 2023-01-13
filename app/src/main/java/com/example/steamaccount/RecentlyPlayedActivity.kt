package com.example.steamaccount

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.bottomNav
import kotlinx.android.synthetic.main.activity_recently_played.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecentlyPlayedActivity : AppCompatActivity() {

    lateinit var prefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recently_played)

        title = "Recently Played"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        editor = prefs.edit()
        bottomNav.selectedItemId = R.id.navigation_games

        val recentGames = prefs.getString("recentGames", null)

        if (recentGames == null) {
            loadData()
        } else {
            localData()
        }

        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.navigation_account -> {
                    startActivity(Intent(this, AccountActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.navigation_games -> {
                    startActivity(Intent(this, GamesActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, GamesActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    fun loadData() {
        val steamID = prefs.getLong("steamid", 0)
        val service = ServiceBuilder.buildService(Services::class.java)
        val requestCall = service.getRecentlyPlayed(steamID)

        requestCall.enqueue(object : Callback<RecentlyPlayed> {
            override fun onResponse(call: Call<RecentlyPlayed>,
                                    response: Response<RecentlyPlayed>
            ) {
                val span = resources.configuration.orientation
                if (response.isSuccessful) {
                    if (response.body()!!.response.totalCount != 0) {

                        val recentGames = response.body()!!

                        editor.putString("recentGames", Gson().toJson(recentGames)).apply()

                        recyclerView.layoutManager = GridLayoutManager(this@RecentlyPlayedActivity, span)
                        recyclerView.adapter = RecentGamesAdapter(response.body()!!)
                    } else {
                        return Toast.makeText(this@RecentlyPlayedActivity, "No recent games!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    AlertDialog.Builder(this@RecentlyPlayedActivity)
                        .setTitle("API Error")
                        .setMessage("Response but error ${response.message()}")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }
            override fun onFailure(call: Call<RecentlyPlayed>, t: Throwable) {
                AlertDialog.Builder(this@RecentlyPlayedActivity)
                    .setTitle("API Error")
                    .setMessage("Could not retrieve data. Please check your internet connection!")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        })
    }

    fun localData() {
        val returnedGamesJson = prefs.getString("recentGames", null)
        val span = resources.configuration.orientation
        val recentGames: RecentlyPlayed = Gson().fromJson(returnedGamesJson, RecentlyPlayed::class.java)

        recyclerView.layoutManager = GridLayoutManager(this@RecentlyPlayedActivity, span)
        recyclerView.adapter = RecentGamesAdapter(recentGames)
    }
}