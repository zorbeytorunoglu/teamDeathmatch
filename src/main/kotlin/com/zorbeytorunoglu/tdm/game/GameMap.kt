package com.zorbeytorunoglu.tdm.game

import com.zorbeytorunoglu.tdm.arena.Arena
import com.zorbeytorunoglu.tdm.arena.ArenaStatus
import com.zorbeytorunoglu.tdm.game.player.GamePlayer
import org.bukkit.entity.Player

class GameMap(val arena: Arena) {

    var status: ArenaStatus = ArenaStatus.WAITING
    val lobbyPlayers: ArrayList<String> = ArrayList()
    val inGamePlayers = ArrayList<GamePlayer>()
    val spectators = ArrayList<String>()
    val time: Int = arena.time*60

    fun playerInGame(player: Player): Boolean {

        if (inGamePlayers.isEmpty()) return false

        return inGamePlayers.any { it.uuid == player.uniqueId.toString() }

    }

    fun playerIsSpectator(player: Player): Boolean {

        if (spectators.isEmpty()) return false

        return spectators.contains(player.uniqueId.toString())

    }

}