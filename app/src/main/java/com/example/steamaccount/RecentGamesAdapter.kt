package com.example.steamaccount

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.game_layout.view.*


class RecentGamesAdapter(private val RecentlyPlayed: RecentlyPlayed) :
        RecyclerView.Adapter<RecentGamesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.txtTitle
        val playtime = view.txtPlaytime
        val recent = view.txtRecentTime
        val image = view.imgBanner
        val button = view.btnStorePage
    }

    override fun getItemCount(): Int {
        return RecentlyPlayed.response.games.size
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.game_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val Game = RecentlyPlayed.response.games.get(position)
        holder.title.text = Game.name
        holder.playtime.text = "Play Time: ${(Game.playtimeForever / 60).toString()} hours"
        holder.recent.text = "Played last 2 weeks: ${(Game.playtime2weeks / 60).toString()} hours"

        if (holder.itemView.context.resources.configuration.orientation == 1) {
            Picasso.get().load("https://media.steampowered.com/steamcommunity/public/images/apps/${Game.appid}/${Game.imgIconUrl}.jpg").placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(holder.image)
        } else if (holder.itemView.context.resources.configuration.orientation == 2) {
            Picasso.get().load("https://media.steampowered.com/steamcommunity/public/images/apps/${Game.appid}/${Game.imgLogoUrl}.jpg").placeholder(R.drawable.default_gamebannersmall).error(R.drawable.default_gamebannersmall).fit().into(holder.image)
        }

        holder.button.setOnClickListener {
            val goStorePage = Intent(Intent.ACTION_VIEW, Uri.parse("http://store.steampowered.com/app/${Game.appid}"))
            holder.itemView.context.startActivity(goStorePage)
        }
    }
}
