package com.zorbeytorunoglu.tdm.configuration.scoreboards

import com.zorbeytorunoglu.kLib.configuration.Resource

class Scoreboards(resource: Resource) {

    val lobbyScoreboardTitle = resource.getString("lobbyScoreboard.title")!!
    val lobbyScoreboardLines = resource.getStringList("lobbyScoreboard.lines")
    val gameScoreboardTitle = resource.getString("gameScoreboard.title")!!
    val gameScoreboardLines = resource.getStringList("gameScoreboard.lines")

}