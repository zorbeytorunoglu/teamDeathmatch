package com.zorbeytorunoglu.tdm.game.player

import org.bukkit.entity.Player

class GamePlayer {

    val uuid: String
    val stats: PlayerStats
    var status: PlayerStatus
    val team: Team

    constructor(player: Player, team: Team) {
        uuid = player.uniqueId.toString()
        stats = PlayerStats(2, 0)
        status = PlayerStatus.PLAYING
        this.team = team
    }

    constructor(player: Player, team: Team, live: Int) {
        uuid = player.uniqueId.toString()
        stats = PlayerStats(live, 0)
        status = PlayerStatus.PLAYING
        this.team = team
    }

    constructor(uuid: String, team: Team) {
        this.uuid = uuid
        stats = PlayerStats(2, 0)
        status = PlayerStatus.PLAYING
        this.team = team
    }

    constructor(uuid: String, team: Team, live: Int) {
        this.uuid = uuid
        stats = PlayerStats(live, 0)
        status = PlayerStatus.PLAYING
        this.team = team
    }

    fun isDead(): Boolean = status == PlayerStatus.DEAD

}