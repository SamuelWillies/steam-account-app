package com.example.steamaccount


import com.google.gson.annotations.SerializedName

data class AccountDetails(
    val response: Response
) {
    data class Response(
        val players: List<Player>
    ) {
        data class Player(
            val avatar: String,
            val avatarfull: String,
            val avatarhash: String,
            val avatarmedium: String,
            val commentpermission: Int,
            val communityvisibilitystate: Int,
            val lastlogoff: Int,
            val personaname: String,
            val personastate: Int,
            val personastateflags: Int,
            val primaryclanid: String,
            val profilestate: Int,
            val profileurl: String,
            val steamid: String,
            val timecreated: Int
        )
    }
}