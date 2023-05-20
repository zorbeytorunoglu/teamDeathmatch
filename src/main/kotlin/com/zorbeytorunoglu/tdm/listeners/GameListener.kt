package com.zorbeytorunoglu.tdm.listeners

import com.zorbeytorunoglu.kLib.extensions.registerEvents
import com.zorbeytorunoglu.kLib.task.Scopes
import com.zorbeytorunoglu.kLib.task.suspendFunctionSync
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.game.GameMap
import com.zorbeytorunoglu.tdm.game.player.GamePlayer
import com.zorbeytorunoglu.tdm.game.player.PlayerStatus
import com.zorbeytorunoglu.tdm.game.player.Team
import com.zorbeytorunoglu.tdm.utils.Utils
import kotlinx.coroutines.launch
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent

class GameListener(private val plugin: TDM): Listener {

    init {

        plugin.registerEvents(this)
        println("registered game listener")

    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {

        if (plugin.gameManager.playerInAnyGame(event.player)) {

            val args = event.message.split(" ")

            var whitelisted = false

            plugin.config.gameCommandWhitelist.forEach {
                if (it.startsWith(args[0])) whitelisted = true
            }

            if (!whitelisted) {
                event.isCancelled = true
                event.player.sendMessage(plugin.messages.gameCommand)
                return
            }

        }

    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {

        println("player quit fired")

        val gamePair = plugin.gameManager.getGameMapGamePlayer(event.player.uniqueId.toString())

        if (gamePair.first != null) {

            println("null degil, atesledim")

            plugin.gameManager.playerLeft(gamePair.second!!, gamePair.first!!)

        }

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
    fun onSuicide(event: PlayerDeathEvent) {

        if (event.entity.killer != null) return

        val game = plugin.gameManager.getGameMapGamePlayer(event.entity.uniqueId.toString())

        if (game.first == null || game.second == null) return

        kill(game.second!!, game.first!!, event.entity)

    }

    @EventHandler
    fun onKill(event: PlayerDeathEvent) {

        if (event.entity.killer == null) return

        if (event.entity.killer !is Player) return

        if (!bothInSameGame(event.entity, event.entity.killer!!)) return

        val gameMap = plugin.gameManager.getPlayerGame(event.entity)!!

        val gamePlayer = plugin.gameManager.getGamePlayer(event.entity)!!

        kill(gamePlayer, gameMap, event.entity)

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

    private fun kill(gamePlayer: GamePlayer, gameMap: GameMap, player: Player) {

        val live = gamePlayer.stats.live--

        if (live <= 1) {

            gamePlayer.status = PlayerStatus.DEAD

            if (gamePlayer.team == Team.RED) {
                if (gameMap.getAlivePlayers(Team.RED).isEmpty()) {
                    plugin.gameManager.win(gameMap, Team.BLUE)
                    return
                }
            } else {
                if (gameMap.getAlivePlayers(Team.BLUE).isEmpty()) {
                    plugin.gameManager.win(gameMap, Team.RED)
                    return
                }
            }

            Scopes.supervisorScope.launch {

                plugin.suspendFunctionSync {
                    Utils.respawnPlayer(player)
                }

                plugin.suspendFunctionSync {
                    player.teleport(gameMap.arena.spectatorSpawn!!)
                }

                plugin.suspendFunctionSync {
                    player.gameMode = GameMode.SPECTATOR
                }

                plugin.suspendFunctionSync {
                    plugin.scoreboardManager.removeGameBoard(player, gameMap)
                }

                plugin.suspendFunctionSync {
                    plugin.scoreboardManager.updateGame(gameMap)
                }

                gameMap.getEveryoneAsPlayer().forEach {
                    it.sendMessage(plugin.messages.killed.replace("%player%", player.name)
                        .replace("%team%", if (gamePlayer.team == Team.RED) plugin.messages.red else plugin.messages.blue))
                }

                player.sendMessage(plugin.messages.spectating)

            }

        } else {

            Scopes.supervisorScope.launch {
                plugin.suspendFunctionSync {
                    Utils.respawnPlayer(player)
                }
                plugin.suspendFunctionSync {
                    plugin.gameManager.teleportToSpawn(gameMap, gamePlayer)
                }
                plugin.suspendFunctionSync {
                    plugin.gameManager.giveTeamKit(gamePlayer, gameMap)
                }
                plugin.suspendFunctionSync {
                    plugin.scoreboardManager.updateGameBoard(player, gameMap)
                }
            }.invokeOnCompletion {
                player.sendMessage(plugin.messages.liveRemaining.replace("%live%", "${gamePlayer.stats.live}"))
                gameMap.getEveryoneAsPlayer().forEach {
                    it.sendMessage(plugin.messages.diedLive.replace("%player%", player.name)
                        .replace("%live%", "${gamePlayer.stats.live}"))
                }
            }

        }

    }

}