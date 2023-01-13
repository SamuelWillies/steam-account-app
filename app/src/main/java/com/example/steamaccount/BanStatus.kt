package com.example.steamaccount

import com.google.gson.annotations.SerializedName

data class BanStatus(
    val players: List<Player>
) {
    data class Player(
        @SerializedName("CommunityBanned")
        val communityBanned: Boolean,
        @SerializedName("DaysSinceLastBan")
        val daysSinceLastBan: Int,
        @SerializedName("EconomyBan")
        val economyBan: String,
        @SerializedName("NumberOfGameBans")
        val numberOfGameBans: Int,
        @SerializedName("NumberOfVACBans")
        val numberOfVACBans: Int,
        @SerializedName("SteamId")
        val steamId: String,
        @SerializedName("VACBanned")
        val vACBanned: Boolean
    )
}