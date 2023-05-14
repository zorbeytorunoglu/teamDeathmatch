package com.zorbeytorunoglu.tdm.game

import com.zorbeytorunoglu.kLib.extensions.clearAllInventory
import com.zorbeytorunoglu.kLib.extensions.info
import com.zorbeytorunoglu.kLib.extensions.playSound
import com.zorbeytorunoglu.kLib.extensions.severe
import com.zorbeytorunoglu.kLib.task.Scopes
import com.zorbeytorunoglu.kLib.task.suspendFunctionSync
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.arena.Arena
import com.zorbeytorunoglu.tdm.arena.ArenaStatus
import com.zorbeytorunoglu.tdm.game.player.GamePlayer
import com.zorbeytorunoglu.tdm.game.player.PlayerStatus
import com.zorbeytorunoglu.tdm.game.player.Team
import com.zorbeytorunoglu.tdm.utils.Utils
import kotlinx.coroutines.launch
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*


class GameManager(private val plugin: TDM) {

    fun getPlayerGame(player: Player): GameMap? {

        if (plugin.arenaManager.gameMaps.isEmpty()) return null

        plugin.arenaManager.gameMaps.values.forEach {

            if (playerInGame(player, it)) return it

        }

        return null

    }

    fun getGamePlayer(player: Player): GamePlayer? {

        plugin.gameManager.getPlayerGame(player)!!.inGamePlayers.forEach {
            if (it.uuid == player.uniqueId.toString()) return it
        }

        return null

    }

    fun playerInAnyGame(player: Player): Boolean {

        if (plugin.arenaManager.gameMaps.isEmpty()) return false

        plugin.arenaManager.gameMaps.values.forEach {

            if (playerInGame(player, it)) {
                return true
            }

        }

        return false

    }

    fun playerInGame(player: Player, gameMap: GameMap): Boolean {

        return gameMap.inGamePlayers.any {
            it.uuid == player.uniqueId.toString()
        }

    }

    fun isArenaFull(gameMap: GameMap): Boolean {

        return gameMap.inGamePlayers.size >= gameMap.arena.maxPlayers

    }

    fun isLobbyFull(gameMap: GameMap): Boolean = gameMap.arena.maxPlayers <= gameMap.lobbyPlayers.size

    fun removeGates(arena: Arena) {

        if (arena.gates.isNotEmpty()) {
            arena.gates.forEach {
                it.block.type = Material.AIR
            }
        }

    }

    fun join(player: Player, gameMap: GameMap) {

        gameMap.lobbyPlayers.add(player.uniqueId.toString())

        player.teleport(gameMap.arena.lobby!!)

        plugin.kitManager.playerKits[player.uniqueId.toString()] =
            plugin.kitManager.inventoryToKit(player.inventory)

        player.clearAllInventory()

        plugin.scoreboardManager.giveLobbyBoard(player, gameMap)

        plugin.scoreboardManager.updateLobby(gameMap)

        //TODO: Scoreboard

        //TODO: Save kit, join lobby, wait, check for min player

    }

    fun startGame(gameMap: GameMap) {

        for (i in 0 until gameMap.lobbyPlayers.size) {
            if (i < gameMap.lobbyPlayers.size / 2) {
                gameMap.inGamePlayers.add(GamePlayer(gameMap.lobbyPlayers[i], Team.RED))
            } else {
                gameMap.inGamePlayers.add(GamePlayer(gameMap.lobbyPlayers[i], Team.BLUE))
            }
        }

        gameMap.lobbyPlayers.clear()

        Scopes.supervisorScope.launch {

            gameMap.getRedTeam().forEach {
                plugin.suspendFunctionSync {
                    teleportToSpawn(gameMap, it)
                }
            }

            gameMap.getBlueTeam().forEach {
                plugin.suspendFunctionSync {
                    teleportToSpawn(gameMap, it)
                }
            }

        }.invokeOnCompletion {

            Scopes.supervisorScope.launch {

                gameMap.getRedTeam().forEach {
                    plugin.suspendFunctionSync {
                        clearInventory(it)
                    }
                }

                gameMap.getBlueTeam().forEach {
                    plugin.suspendFunctionSync {
                        clearInventory(it)
                    }
                }

            }.invokeOnCompletion {

                Scopes.supervisorScope.launch {

                    gameMap.getRedTeam().forEach {
                        plugin.suspendFunctionSync {
                            giveTeamKit(it, gameMap)
                        }
                    }

                    gameMap.getBlueTeam().forEach {
                        plugin.suspendFunctionSync {
                            giveTeamKit(it, gameMap)
                        }
                    }

                }.invokeOnCompletion {

                    //TODO: Gate countdown, countdown, start

                    var waitTime = 10 //TODO: Can be configurable

                    val task = object : BukkitRunnable() {
                        override fun run() {

                            if (waitTime <= 0) {

                                //TODO: Remove gates, start game

                                cancel()
                                return
                            }

                            gameMap.inGamePlayers.forEach {
                                sendActionBar(it, "$waitTime")
                                if (plugin.config.countdownSound != null)
                                    getPlayer(it)!!.playSound(plugin.config.countdownSound!!, 1.0F, 1.0F)
                            }

                            waitTime--

                        }
                    }.runTaskTimer(plugin, 0L, 20L)

                }

            }

        }

    }

    fun teleportToSpawn(gameMap: GameMap, gamePlayer: GamePlayer) {

        if (getPlayer(gamePlayer) != null) {

            if (gamePlayer.team == Team.RED) {
                getPlayer(gamePlayer)!!.teleport(gameMap.arena.redSpawn!!)
            } else {
                getPlayer(gamePlayer)!!.teleport(gameMap.arena.blueSpawn!!)
            }

        } else {
            //TODO: Player left
        }

    }

    fun saveInventory(gamePlayer: GamePlayer) {
        if (getPlayer(gamePlayer) != null) {
            plugin.kitManager.playerKits[gamePlayer.uuid] = plugin.kitManager.inventoryToKit(getPlayer(gamePlayer)!!.inventory)
        } else {
            //TODO: Player left
        }
    }

    fun getPlayer(gamePlayer: GamePlayer): Player? {

        return plugin.server.getPlayer(plugin.cacheManager.getPlayerName(gamePlayer.uuid))

    }

    fun giveTeamKit(gamePlayer: GamePlayer, gameMap: GameMap) {
        if (getPlayer(gamePlayer) != null) {
            if (gamePlayer.team == Team.RED) {
                plugin.kitManager.giveKit(getPlayer(gamePlayer)!!, gameMap.arena.redKit)
            } else {
                plugin.kitManager.giveKit(getPlayer(gamePlayer)!!, gameMap.arena.blueKit)
            }
        }
    }

    fun clearInventory(gamePlayer: GamePlayer) {

        if (getPlayer(gamePlayer) != null) {
            getPlayer(gamePlayer)!!.clearAllInventory()
        } else {
            //TODO: Player left
        }

    }

    fun sendActionBar(gamePlayer: GamePlayer, message: String) {

        if (getPlayer(gamePlayer) != null) {
            Utils.sendActionBar(getPlayer(gamePlayer)!!, message)
        } else {
            //TODO: Player left
        }

    }

    fun getBlueAlivePlayers(gameMap: GameMap): List<GamePlayer> {

        if (gameMap.inGamePlayers.isEmpty()) return emptyList()

        return gameMap.inGamePlayers.filter {

            it.team == Team.BLUE

        }

    }

    fun getRedAlivePlayers(gameMap: GameMap): List<GamePlayer> {

        if (gameMap.inGamePlayers.isEmpty()) return emptyList()

        return gameMap.inGamePlayers.filter {

            it.team == Team.RED

        }

    }

    fun refreshGameMap(gameMap: GameMap) {

        gameMap.inGamePlayers.clear()
        gameMap.spectators.clear()

        gameMap.status = ArenaStatus.RELOADING

        if (plugin.worldManager.loadWorldFromMaps(gameMap.arena.name)) {
            plugin.info("${gameMap.arena.name} is refreshed.")
            gameMap.status = ArenaStatus.WAITING
        } else {
            plugin.severe("${gameMap.arena.name} could not be refreshed. Check its configurations.")
            gameMap.status = ArenaStatus.CLOSED
        }

    }

    fun win(gameMap: GameMap, team: Team) {

        if (gameMap.inGamePlayers.isEmpty()) return

        Scopes.supervisorScope.launch {

            gameMap.inGamePlayers.forEach {

                val player =
                    plugin.gameManager.getPlayer(it)

                if (player != null) {
                    plugin.suspendFunctionSync {

                        if (it.status == PlayerStatus.DEAD) {
                            Utils.respawnPlayer(player)
                        }

                        if (it.status == PlayerStatus.SPECTATING) {
                            player.gameMode = GameMode.SURVIVAL
                        }

                    }
                }

            }

        }.invokeOnCompletion {

            Scopes.supervisorScope.launch {

                gameMap.inGamePlayers.forEach {

                    val player =
                        plugin.gameManager.getPlayer(it)

                    if (player != null) {

                        plugin.suspendFunctionSync {
                            player.teleport(plugin.spawn!!)
                        }

                    }

                }

            }.invokeOnCompletion {

                Scopes.supervisorScope.launch {

                    gameMap.inGamePlayers.forEach {

                        val player =
                            plugin.gameManager.getPlayer(it)

                        if (player != null) {

                            if (it.team == team) {

                                plugin.config.winCommands.forEach {
                                    plugin.suspendFunctionSync {
                                        plugin.server.dispatchCommand(plugin.server.consoleSender, it.replace("%player%",
                                            plugin.cacheManager.getPlayerName(player.name)))
                                    }
                                }

                            } else {

                                player.sendMessage(plugin.messages.youLost)

                            }

                        }

                    }

                }.invokeOnCompletion {

                    refreshGameMap(gameMap)

                }

            }

        }

    }

}