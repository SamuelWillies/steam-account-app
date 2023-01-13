package com.example.steamaccount


import com.google.gson.annotations.SerializedName

data class AccountLevel(
    val response: Response
) {
    data class Response(
        @SerializedName("player_level")
        val playerLevel: Int
    )
}