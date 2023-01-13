package com.example.steamaccount

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.activity_main.bottomNav
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AccountActivity : AppCompatActivity() {

    lateinit var prefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        title = "Account Information"
        prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        editor = prefs.edit()

        val steamID = prefs.getLong("steamid", 0)
        val econstatus = prefs.getString("econstatus", null)
        val datefetched = prefs.getString("datefetchedaccount", null)
        val visibility = prefs.getInt("visibility", 0)

        Log.d("ECON STATUS", econstatus.toString())

        if (steamID != 0L && econstatus == null) {
            loadData()
        } else if (econstatus != null) {
            txtCommStatusResult.text = prefs.getString("commstatus", null)
            txtVACStatusResult.text = prefs.getString("vacstatus", null)
            txtEconStatusResult.text = prefs.getString("econstatus", null)
            txtVACBansResult.text = prefs.getString("vacbans", null)
            txtDaysResult.text = prefs.getString("days", null)
            txtGameBansResult.text = prefs.getString("gamebans", null)

            if (visibility != 3) {
                txtCreationResult.text = "Private"
                txtLogoffResult.text = "Private"
            } else {
                txtCreationResult.text = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone( ZoneId.systemDefault() ).format((Instant.ofEpochSecond(prefs.getLong("creationdate", 0))))
                txtLogoffResult.text = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone( ZoneId.systemDefault() ).format((Instant.ofEpochSecond(prefs.getLong("lastlogoff", 0))))
                if (DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone( ZoneId.systemDefault() ).format((Instant.ofEpochSecond(prefs.getLong("lastlogoff", 0)))) == "01/01/1970 01:00") {
                    txtLogoffResult.text = "Unavailable"
                } else {
                    txtLogoffResult.text = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone( ZoneId.systemDefault() ).format((Instant.ofEpochSecond(prefs.getLong("lastlogoff", 0))))
                }
            }

            txtOnlineResult.text = prefs.getString("personastate", null)
            txtDateFetched2.text = "Last Updated: ${datefetched}"
        }

        bottomNav.selectedItemId = R.id.navigation_account

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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun loadData() {
        Log.d("Load Data", "This is calling LoadData function in AccountActivity")
        val steamID = prefs.getLong("steamid", 0).toString().toLong()

        val service = ServiceBuilder.buildService(Services::class.java)
        val requestCall = service.getBans(steamID)
        requestCall.enqueue(object : Callback<BanStatus> {
            override fun onResponse(call: Call<BanStatus>,
                                    response: Response<BanStatus>
            ) {
                if (response.isSuccessful) {
                    val BanStatus = response.body()!!

                    val commStatus = BanStatus.players[0].communityBanned
                    if (commStatus) {
                        txtCommStatusResult.text = "Banned"
                        editor.putString("commstatus", "Banned").apply()
                    } else {
                        txtCommStatusResult.text = "Not Banned"
                        editor.putString("commstatus", "Not Banned").apply()
                    }

                    val vacStatus = BanStatus.players[0].vACBanned
                    if (vacStatus) {
                        txtVACStatusResult.text = "Banned"
                        editor.putString("vacstatus", "Banned").apply()
                        Log.d("Test", "Test")
                    } else {
                        txtVACStatusResult.text = "Not Banned"
                        editor.putString("vacstatus", "Not Banned").apply()
                        Log.d("Test", "Test2")
                    }

                    txtEconStatusResult.text = BanStatus.players[0].economyBan.capitalize()
                    txtVACBansResult.text = BanStatus.players[0].numberOfVACBans.toString()
                    txtDaysResult.text = BanStatus.players[0].daysSinceLastBan.toString()
                    txtGameBansResult.text = BanStatus.players[0].numberOfGameBans.toString()

                    if (prefs.getInt("visibility", 0) == 3) {
                        txtCreationResult.text = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone( ZoneId.systemDefault() ).format((Instant.ofEpochSecond(prefs.getLong("creationdate", 0))))
                        txtLogoffResult.text = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone( ZoneId.systemDefault() ).format((Instant.ofEpochSecond(prefs.getLong("lastlogoff", 0))))
                    } else {
                        txtCreationResult.text = "Private"
                        txtLogoffResult.text = "Private"
                    }

                    txtOnlineResult.text = prefs.getString("personastate", null)
                    txtDateFetched2.text = "Last Updated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toString()}"

                    editor.putString("econstatus", BanStatus.players[0].economyBan.capitalize())
                    editor.putString("vacbans", BanStatus.players[0].numberOfVACBans.toString())
                    editor.putString("days", BanStatus.players[0].daysSinceLastBan.toString())
                    editor.putString("gamebans", BanStatus.players[0].numberOfGameBans.toString())
                    editor.putString("datefetchedaccount", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toString())
                    editor.apply()
                } else {
                    AlertDialog.Builder(this@AccountActivity)
                        .setTitle("API error")
                        .setMessage("There was a response, but this error occurred: ${response.message()}")
                        .setPositiveButton(android.R.string.ok) {_, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }

            override fun onFailure(call: Call<BanStatus>, t: Throwable) {
                AlertDialog.Builder(this@AccountActivity)
                    .setTitle("API error")
                    .setMessage("Could not retrieve data. Please check your internet connection!")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        })
    }
}