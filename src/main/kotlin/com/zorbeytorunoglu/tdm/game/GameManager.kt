package com.zorbeytorunoglu.tdm.game

import com.zorbeytorunoglu.tdm.TDM
import org.bukkit.entity.Player

class GameManager(val plugin: TDM) {

    fun playerInGame(player: Player, gameMap: GameMap): Boolean {
        if (gameMap.players.isEmpty()) return false
        return true
        //devamke
    }

    fun arenaFull(gameMap: GameMap): Boolean {
        return gameMap.players.size == gameMap.arena.maxPlayers
    }

}