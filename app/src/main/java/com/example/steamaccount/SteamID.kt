package com.example.steamaccount


import com.google.gson.annotations.SerializedName

data class SteamID(
    val response: Response
) {
    data class Response(
        val steamid: String,
        val success: Int
    )
}