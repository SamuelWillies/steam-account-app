package com.example.steamaccount

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.bottomNav
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var ambientLight: Sensor? = null
    private lateinit var menu: Menu
    lateinit var prefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        ambientLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        editor = prefs.edit()

        bottomNav.selectedItemId = R.id.navigation_home

        txtSteamID.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                btnGetData.performClick()
                return@setOnKeyListener false
            }
            false
        }

        btnGetData.setOnClickListener{
            if (txtSteamID.text.toString().toLongOrNull() == null) {
                if (txtSteamID.text.isNotEmpty()) {
                    getSteamID()
                } else {
                    Toast.makeText(this, "Please enter an account!", Toast.LENGTH_LONG).show()
                }
            } else {
                loadData()
            }
        }

        val steamID = prefs.getLong("steamid", 0)
        val nickname = prefs.getString("nickname", null)
        val avatar = prefs.getString("avatar", null)
        val datefetched = prefs.getString("datefetchedhome", null)
        val level = prefs.getInt("level", 0)
        val visibility = prefs.getInt("visibility", 0)
        val theme = prefs.getBoolean("dark", false)

        if (steamID != 0L) {
            txtSteamID.setText(steamID.toString())
            Picasso.get().load(avatar).placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar).fit().into(imgAvatar)
            txtNickname.text = nickname
            txtDateFetched.text = "Last Updated: ${datefetched}"

            if (visibility != 3) {
                txtAccLevel.text = "Level: Private"
            } else {
                txtAccLevel.text = "Level: ${level.toString()}"
            }
        }

        if (theme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        this.menu = menu!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.itemLight -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("dark", false).apply()
                true
            }
            R.id.itemDark -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("dark", true).apply()
                true
            }
            R.id.itemPrefs -> {
                val theme = prefs.getBoolean("dark", false)
                editor.clear().apply()
                editor.putBoolean("dark", theme).apply()
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    override fun onSensorChanged(event: SensorEvent) {
        if (::menu.isInitialized) {
            if (event.values[0] < 50) {
                menu.findItem(R.id.itemLight).title = "Light"
                menu.findItem(R.id.itemDark).title = "Dark (Recommended)"
            } else {
                menu.findItem(R.id.itemLight).title = "Light (Recommended)"
                menu.findItem(R.id.itemDark).title = "Dark"
            }
        } else {
            return
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        return
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, ambientLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    fun loadData() {
        val steamID = txtSteamID.text.toString().toLong()

        val service = ServiceBuilder.buildService(Services::class.java)
        val requestCall = service.getAccount(steamID)

        requestCall.enqueue(object : Callback<AccountDetails> {
            override fun onResponse(call: Call<AccountDetails>,
                                    response: Response<AccountDetails>
            ) {
                if (response.isSuccessful) {
                    val accountDetails = response.body()!!

                    if (accountDetails.response.players.isNotEmpty()) {
                        val theme = prefs.getBoolean("dark", false)
                        editor.clear().apply()
                        editor.putBoolean("dark", theme)
                        editor.putLong("steamid", steamID)
                        editor.apply()

                        val Avatar = accountDetails.response.players[0].avatarfull

                        Picasso.get().load(Avatar).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).fit().into(imgAvatar)
                        txtNickname.text = accountDetails.response.players[0].personaname
                        txtDateFetched.text = "Last Updated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toString()}"

                        editor.putString("nickname", txtNickname.text.toString())
                        editor.putString("avatar", Avatar)
                        editor.putString("datefetchedhome", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toString())
                        editor.putLong("creationdate", accountDetails.response.players[0].timecreated.toLong())
                        editor.putLong("lastlogoff", accountDetails.response.players[0].lastlogoff.toLong())

                        when (accountDetails.response.players[0].personastate) {
                            0 -> {
                                editor.putString("personastate", "Offline").apply()
                            }
                            1 -> {
                                editor.putString("personastate", "Online").apply()
                            }
                            2 -> {
                                editor.putString("personastate", "Busy").apply()
                            }
                            3 -> {
                                editor.putString("personastate", "Away").apply()
                            }
                            4 -> {
                                editor.putString("personastate", "Snooze").apply()
                            }
                            5 -> {
                                editor.putString("personastate", "Looking to Trade").apply()
                            }
                            6 -> {
                                editor.putString("personastate", "Looking to Play").apply()
                            }
                        }

                        editor.putInt("visibility", accountDetails.response.players[0].communityvisibilitystate) //1 = Private, 3 = Public
                        editor.apply()

                        getLevel()
                    } else {
                        editor.clear().apply()
                        Toast.makeText(this@MainActivity, "Account not found", Toast.LENGTH_LONG).show()
                    }

                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("API error")
                        .setMessage("There was a response, but this error occurred: ${response.message()}")
                        .setPositiveButton(android.R.string.ok) {_, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }

            override fun onFailure(call: Call<AccountDetails>, t: Throwable) {
                //process failure
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("API error")
                    .setMessage("Could not retrieve data. Please check your internet connection!")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        })
    }

    fun getLevel() {
        val steamID = txtSteamID.text.toString().toLong()

        val service = ServiceBuilder.buildService(Services::class.java)
        val requestCallLevel = service.getAccountLevel(steamID)
        requestCallLevel.enqueue(object : Callback<AccountLevel> {
            override fun onResponse(call: Call<AccountLevel>,
                                    response: Response<AccountLevel>
            ) {
                if (response.isSuccessful) {
                    val AccountLevel = response.body()!!
                    if (prefs.getInt("visibility", 0) != 3) {
                        txtAccLevel.text = "Level: Private"
                    } else {
                        txtAccLevel.text = "Level: ${AccountLevel.response.playerLevel.toString()}"
                        editor.putInt("level", AccountLevel.response.playerLevel)
                        editor.apply()
                    }

                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("API error")
                        .setMessage("There was a response, but this error occurred: ${response.message()}")
                        .setPositiveButton(android.R.string.ok) {_, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }

            override fun onFailure(call: Call<AccountLevel>, t: Throwable) {
                //process failure
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("API error")
                    .setMessage("Could not retrieve data. Please check your internet connection!")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        })
    }

    fun getSteamID() {
        val steamID = txtSteamID.text

        val service = ServiceBuilder.buildService(Services::class.java)
        val requestCall = service.getSteamID(steamID.toString())
        requestCall.enqueue(object : Callback<SteamID> {
            override fun onResponse(call: Call<SteamID>,
                                    response: Response<SteamID>
            ) {
                if (response.isSuccessful) {
                    val SteamID = response.body()!!
                    if (SteamID.response.success == 1) {
                        txtSteamID.setText(SteamID.response.steamid)
                        loadData()
                    } else {
                        Toast.makeText(this@MainActivity, "Account not found", Toast.LENGTH_LONG).show()
                    }

                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("API error")
                        .setMessage("There was a response, but this error occurred: ${response.message()}")
                        .setPositiveButton(android.R.string.ok) {_, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }

            override fun onFailure(call: Call<SteamID>, t: Throwable) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("API error")
                    .setMessage("Could not retrieve data. Please check your internet connection!")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        })
    }
}