package com.example.steamaccount

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_games.*
import kotlinx.android.synthetic.main.activity_main.bottomNav
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GamesActivity : AppCompatActivity() {

    lateinit var prefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    var appId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)

        title = "Game Information"

        prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        editor = prefs.edit()

        bottomNav.selectedItemId = R.id.navigation_games
        btnRecent.isEnabled = false
        btnShare.isEnabled = false
        btnStorePage.isEnabled = false

        val steamID = prefs.getLong("steamid", 0)
        val ownedGamesResult = prefs.getInt("ownedgames", 0)
        val hoursResult = prefs.getInt("hours", 0)
        val unplayedResult = prefs.getInt("unplayed", 0)
        val dateFetched = prefs.getString("datefetchedgames", null)
        val visibility = prefs.getInt("visibility", 3)
        val dataLoaded = prefs.getBoolean("dataLoaded", false)

        if (savedInstanceState != null) {
            txtRandomResult.text = savedInstanceState.getString("gameName")
            txtPlaytime.text = savedInstanceState.getString("playTime")
            txtRecentPlaytime.text = savedInstanceState.getString("playTimeRecent")
            btnStorePage.isEnabled = savedInstanceState.getBoolean("storeBtn")
            appId = savedInstanceState.getInt("appID")
            if (appId != 0) {
                Picasso.get().load("https://cdn.akamai.steamstatic.com/steam/apps/${appId}/header.jpg").placeholder(R.drawable.default_gamebanner).error(R.drawable.default_gamebanner).fit().into(imgGame)
            }
            btnStorePage.setOnClickListener {
                val goStorePage = Intent(Intent.ACTION_VIEW, Uri.parse("http://store.steampowered.com/app/${appId}"))
                startActivity(goStorePage)
            }
        }

        if (steamID != 0L && !dataLoaded) {
            loadData()
        } else {
            if (visibility != 3 && dataLoaded) {
                txtOwnedGamesResult.text = "Private"
                txtHoursResult.text = "Private"
                txtUnplayedResult.text = "Private"
                txtDateFetched3.text = "Last Updated: ${dateFetched}"
                btnRecent.isEnabled = false
                btnRecent.isEnabled = false
            } else if (visibility == 3 && dataLoaded) {
                txtOwnedGamesResult.text = ownedGamesResult.toString()
                txtHoursResult.text = hoursResult.toString()
                txtUnplayedResult.text = unplayedResult.toString()
                txtDateFetched3.text = "Last Updated: ${dateFetched}"
                btnRecent.isEnabled = true
                btnShare.isEnabled = true
            }
        }

        btnShare.setOnClickListener{ share() }
        btnRecent.setOnClickListener{
            startActivity(Intent(this, RecentlyPlayedActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnRandomise.setOnClickListener{ randomise() }

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

    override fun onSaveInstanceState(ActivityInstanceState : Bundle) {
        super.onSaveInstanceState(ActivityInstanceState)
        ActivityInstanceState.putString("gameName", txtRandomResult.text.toString())
        ActivityInstanceState.putString("playTime", txtPlaytime.text.toString())
        ActivityInstanceState.putString("playTimeRecent", txtRecentPlaytime.text.toString())
        ActivityInstanceState.putBoolean("storeBtn", btnStorePage.isEnabled)
        ActivityInstanceState.putInt("appID", appId)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    fun loadData() {
        val steamID = prefs.getLong("steamid", 0).toString().toLong()

        val service = ServiceBuilder.buildService(Services::class.java)
        val requestCall = service.getOwnedGames(steamID)
        requestCall.enqueue(object : Callback<OwnedGames> {
            override fun onResponse(call: Call<OwnedGames>,
                                    response: Response<OwnedGames>
            ) {
                if (response.isSuccessful) {
                    btnRecent.isEnabled = true

                    val ownedGames = response.body()!!
                    var hours = 0
                    var unplayed = 0
                    btnShare.isEnabled = true

                    if (ownedGames.response.gameCount != 0) {
                        for (Game in ownedGames.response.games) {
                            if (Game.playtimeForever == 0) {
                                unplayed++
                            } else {
                                hours += (Game.playtimeForever / 60)
                            }
                        }

                        val ownedGamesResult = ownedGames.response.gameCount

                        editor.putInt("ownedgames", ownedGamesResult)
                        editor.putInt("hours", hours)
                        editor.putInt("unplayed", unplayed)
                        editor.apply()

                        txtOwnedGamesResult.text = ownedGamesResult.toString()
                        txtHoursResult.text = hours.toString()
                        txtUnplayedResult.text = unplayed.toString()

                    } else {
                        if (prefs.getInt("visibility", 0) != 3) {
                            txtOwnedGamesResult.text = "Private"
                            txtHoursResult.text = "Private"
                            txtUnplayedResult.text = "Private"
                            btnShare.isEnabled = false
                            btnRecent.isEnabled = false
                        } else {
                            txtOwnedGamesResult.text = ownedGames.response.gameCount.toString()
                            txtHoursResult.text = hours.toString()
                            txtUnplayedResult.text = unplayed.toString()
                        }
                    }
                    editor.putString("datefetchedgames", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toString())
                    editor.putString("ownedGamesJson", Gson().toJson(ownedGames))
                    editor.putBoolean("dataLoaded", true)
                    editor.apply()
                    txtDateFetched3.text = "Last Updated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toString()}"

                } else {
                    AlertDialog.Builder(this@GamesActivity)
                        .setTitle("API error")
                        .setMessage("There was a response, but this error occurred: ${response.message()}")
                        .setPositiveButton(android.R.string.ok) {_, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }

            override fun onFailure(call: Call<OwnedGames>, t: Throwable) {
                AlertDialog.Builder(this@GamesActivity)
                    .setTitle("API error")
                    .setMessage("Could not retrieve data. Please check your internet connection!")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        })
    }

    fun randomise() {
        val returnedGamesJson = prefs.getString("ownedGamesJson", null)
        if (returnedGamesJson != null) {
            val ownedGames: OwnedGames = Gson().fromJson(returnedGamesJson, OwnedGames::class.java)
            if (ownedGames.response.gameCount != 0) {
                appId = ownedGames.response.games.random().appid
                btnStorePage.isEnabled = true

                for (Game in ownedGames.response.games) {
                    if (Game.appid == appId) {
                        txtPlaytime.text = "Play Time: ${(Game.playtimeForever / 60).toString()} hours"
                        txtRecentPlaytime.text = "Played last 2 weeks: ${(Game.playtime2weeks / 60).toString()} hours"
                        txtRandomResult.text = Game.name
                        btnStorePage.setOnClickListener {
                            val goStorePage = Intent(Intent.ACTION_VIEW, Uri.parse("http://store.steampowered.com/app/${appId}"))
                            startActivity(goStorePage)
                        }
                        Picasso.get().load("https://cdn.akamai.steamstatic.com/steam/apps/${appId}/header.jpg").placeholder(R.drawable.default_gamebanner).error(R.drawable.default_gamebanner).fit().into(imgGame)
                    }
                }
            } else {
                Toast.makeText(this@GamesActivity, "No games to randomise!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this@GamesActivity, "No games to randomise!", Toast.LENGTH_LONG).show()
        }
    }


    fun share() {
        val intent: Intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share your failure")
        intent.putExtra(Intent.EXTRA_TEXT, "I've wasted ${prefs.getInt("hours", 0)} hours playing games...")
        startActivity(Intent.createChooser(intent, "Share failure"))
    }
}