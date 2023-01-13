package com.example.steamaccount


import com.google.gson.annotations.SerializedName

data class RecentlyPlayed(
    val response: Response
) {
    data class Response(
        val games: List<Game>,
        @SerializedName("total_count")
        val totalCount: Int
    ) {
        data class Game(
            val appid: Int,
            @SerializedName("img_icon_url")
            val imgIconUrl: String,
            @SerializedName("img_logo_url")
            val imgLogoUrl: String,
            val name: String,
            @SerializedName("playtime_2weeks")
            val playtime2weeks: Int,
            @SerializedName("playtime_forever")
            val playtimeForever: Int,
            @SerializedName("playtime_linux_forever")
            val playtimeLinuxForever: Int,
            @SerializedName("playtime_mac_forever")
            val playtimeMacForever: Int,
            @SerializedName("playtime_windows_forever")
            val playtimeWindowsForever: Int
        )
    }
}