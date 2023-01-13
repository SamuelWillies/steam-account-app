package com.example.steamaccount

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Services {
    @GET("ISteamUser/GetPlayerBans/v1/?key=55716338233B5FB50264C534BB4404A3")

    fun getBans(@Query("steamids") steamid: Long): Call<BanStatus>

    @GET("ISteamUser/GetPlayerSummaries/v0002/?key=55716338233B5FB50264C534BB4404A3")

    fun getAccount(@Query("steamids") steamid: Long): Call<AccountDetails>

    @GET("IPlayerService/GetOwnedGames/v0001/?key=55716338233B5FB50264C534BB4404A3&include_appinfo=true")

    fun getOwnedGames(@Query("steamid") steamid: Long): Call<OwnedGames>

    @GET("IPlayerService/GetRecentlyPlayedGames/v0001/?key=55716338233B5FB50264C534BB4404A3")

    fun getRecentlyPlayed(@Query("steamid") steamid: Long): Call<RecentlyPlayed>

    @GET("IPlayerService/GetSteamLevel/v1/?key=55716338233B5FB50264C534BB4404A3")

    fun getAccountLevel(@Query("steamid") steamid: Long): Call<AccountLevel>

    @GET("ISteamUser/ResolveVanityURL/v0001/?key=55716338233B5FB50264C534BB4404A3")

    fun getSteamID(@Query("vanityurl") vanity: String): Call<SteamID>
}