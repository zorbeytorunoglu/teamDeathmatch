package com.zorbeytorunoglu.tdm.listeners

import com.zorbeytorunoglu.kLib.extensions.registerEvents
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.game.GameMap
import com.zorbeytorunoglu.tdm.game.player.PlayerStatus
import com.zorbeytorunoglu.tdm.game.player.Team
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent

class GameListener(private val plugin: TDM): Listener {

    init {

        plugin.registerEvents(this)

    }

    @EventHandler
    fun onHit(event: EntityDamageByEntityEvent) {

        if (!bothPlayers(event.entity, event.damager)) return

        if (!bothInSameGame(event.entity as Player, event.damager as Player)) return

        val gameMap = plugin.gameManager.getPlayerGame(event.entity as Player)

        if (isFriendly(event.entity, event.damager, gameMap!!)) {

            event.isCancelled = true

            val damager = event.damager as Player

            damager.sendMessage(plugin.messages.noFriendlyFire)

            return

        }

    }

    @EventHandler
    fun onKill(event: PlayerDeathEvent) {

        if (event.entity.killer == null) return

        if (event.entity.killer !is Player) return

        if (!bothInSameGame(event.entity, event.entity.killer!!)) return

        val gameMap = plugin.gameManager.getPlayerGame(event.entity)

        val gamePlayer = plugin.gameManager.getGamePlayer(event.entity)

        gamePlayer!!.status = PlayerStatus.DEAD

        if (plugin.gameManager.getRedAlivePlayers(gameMap!!).isEmpty()) {
            plugin.gameManager.win(gameMap, Team.BLUE)
        }

        if (plugin.gameManager.getBlueAlivePlayers(gameMap).isEmpty()) {
            plugin.gameManager.win(gameMap, Team.RED)
        }

    }

    private fun bothPlayers(entity: Entity, otherEntity: Entity): Boolean = entity is Player && otherEntity is Player

    private fun bothInSameGame(player: Player, otherPlayer: Player): Boolean {

        val playerGameMap = plugin.gameManager.getPlayerGame(player)

        val otherPlayerGameMap = plugin.gameManager.getPlayerGame(otherPlayer)

        if (playerGameMap == null || otherPlayerGameMap == null) return false

        return playerGameMap.arena.name == otherPlayerGameMap.arena.name

    }

    private fun isFriendly(entity: Entity, otherEntity: Entity, gameMap: GameMap): Boolean =
        gameMap.getPlayerTeam(entity as Player) == gameMap.getPlayerTeam(otherEntity as Player)

}