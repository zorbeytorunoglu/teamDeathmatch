package com.zorbeytorunoglu.tdm.game

import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.arena.Arena
import org.bukkit.Material
import org.bukkit.entity.Player

class GameManager(private val plugin: TDM) {

    fun playerInGame(player: Player, gameMap: GameMap): Boolean {

        return gameMap.inGamePlayers.any {
            it.uuid == player.uniqueId.toString()
        }

    }

    fun isArenaFull(gameMap: GameMap): Boolean {

        return gameMap.inGamePlayers.size >= gameMap.arena.maxPlayers

    }

    fun removeGates(arena: Arena) {

        if (arena.gates.isNotEmpty()) {
            arena.gates.forEach {
                it.block.type = Material.AIR
            }
        }

    }


}