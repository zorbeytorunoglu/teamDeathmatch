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
import java.util.UUID

class GameManager(private val plugin: TDM) {

    fun getPlayerGame(player: Player): GameMap? {

        if (plugin.arenaManager.gameMaps.isEmpty()) return null

        plugin.arenaManager.gameMaps.values.forEach {

            if (playerInGame(player, it)) return it

        }

        return null

    }

    fun getPlayerGame(uuid: String): GameMap? {

        if (plugin.arenaManager.gameMaps.isEmpty()) return null

        plugin.arenaManager.gameMaps.values.forEach {gameMap ->

            gameMap.inGamePlayers.forEach { if (it.uuid == uuid) return gameMap }

        }

        return null

    }

    fun getGameMapGamePlayer(uuid: String): Pair<GameMap?, GamePlayer?> {

        if (plugin.arenaManager.gameMaps.isEmpty()) return Pair(null, null)

        plugin.arenaManager.gameMaps.values.forEach { gameMap ->

            gameMap.inGamePlayers.forEach {
                if (it.uuid == uuid) return Pair(gameMap, it)
            }

        }

        return Pair(null, null)

    }

    fun getGameMap(player: Player): GameMap? {

        if (plugin.arenaManager.gameMaps.isEmpty()) return null

        plugin.arenaManager.gameMaps.values.forEach { gameMap ->
            if (gameMap.inGamePlayers.any { it.uuid == player.uniqueId.toString() }) return gameMap
        }

        plugin.arenaManager.gameMaps.values.forEach { gameMap ->
            if (gameMap.lobbyPlayers.any { it == player.uniqueId.toString() })
                return gameMap
        }

        return null

    }

    fun getPlayerGame(gamePlayer: GamePlayer): GameMap? {

        if (plugin.arenaManager.gameMaps.isEmpty()) return null

        return plugin.arenaManager.gameMaps.values.firstOrNull {
            it.inGamePlayers.contains(gamePlayer)
        }

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

    fun playerInAnyLobby(player: Player): Boolean {

        if (plugin.arenaManager.gameMaps.isEmpty()) return false

        return plugin.arenaManager.gameMaps.values.any {

            it.lobbyPlayers.contains(player.uniqueId.toString())

        }

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

        player.gameMode = GameMode.SURVIVAL

        player.teleport(gameMap.arena.lobby!!)

        plugin.kitManager.playerKits[player.uniqueId.toString()] =
            plugin.kitManager.inventoryToKit(player.inventory)

        player.clearAllInventory()

        plugin.scoreboardManager.giveLobbyBoard(player, gameMap)

        plugin.scoreboardManager.updateLobby(gameMap)

        if (gameMap.arena.minPlayers == gameMap.lobbyPlayers.size) {

            LobbyCountdown(plugin, gameMap).runTaskTimer(plugin, 20L, 20L)

        }

    }

    fun startGame(gameMap: GameMap) {

        for (i in 0 until gameMap.lobbyPlayers.size) {
            if (i < gameMap.lobbyPlayers.size / 2) {
                gameMap.inGamePlayers.add(GamePlayer(gameMap.lobbyPlayers[i], Team.RED))
                println("team red verdim")
            } else {
                gameMap.inGamePlayers.add(GamePlayer(gameMap.lobbyPlayers[i], Team.BLUE))
                println("team blue verdim")

                //TODO: Debug messages
            }
        }

        gameMap.lobbyPlayers.clear()

        Scopes.supervisorScope.launch {

            gameMap.getRedTeam().forEach {
                plugin.suspendFunctionSync {
                    teleportToSpawn(gameMap, it)
                    plugin.scoreboardManager.giveGameBoard(it, gameMap)
                }
            }

            gameMap.getBlueTeam().forEach {
                plugin.suspendFunctionSync {
                    teleportToSpawn(gameMap, it)
                    plugin.scoreboardManager.giveGameBoard(it, gameMap)
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

                    var waitTime = 10 //TODO: Can be configurable

                    val task = object : BukkitRunnable() {
                        override fun run() {

                            if (waitTime <= 0) {

                                removeGates(gameMap.arena)

                                cancel()
                                return
                            }

                            gameMap.inGamePlayers.forEach {
                                sendActionBar(it, plugin.messages.waitTime.replace("%seconds%", "$waitTime"))
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
            playerLeft(gamePlayer, gameMap)
        }

    }

    fun saveInventory(gamePlayer: GamePlayer) {
        if (getPlayer(gamePlayer) != null) {
            plugin.kitManager.playerKits[gamePlayer.uuid] = plugin.kitManager.inventoryToKit(getPlayer(gamePlayer)!!.inventory)
        } else {
            playerLeft(gamePlayer, getPlayerGame(gamePlayer)!!)
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
            playerLeft(gamePlayer, getPlayerGame(gamePlayer)!!)
        }

    }

    fun sendActionBar(gamePlayer: GamePlayer, message: String) {

        if (getPlayer(gamePlayer) != null) {
            Utils.sendActionBar(getPlayer(gamePlayer)!!, message)
        } else {
            playerLeft(gamePlayer, getPlayerGame(gamePlayer)!!)
        }

    }

    fun getBlueAlivePlayers(gameMap: GameMap): List<GamePlayer> {

        if (gameMap.inGamePlayers.isEmpty()) return emptyList()

        return gameMap.inGamePlayers.filter {

            it.team == Team.BLUE && it.status == PlayerStatus.PLAYING

        }

    }

    fun getRedAlivePlayers(gameMap: GameMap): List<GamePlayer> {

        if (gameMap.inGamePlayers.isEmpty()) return emptyList()

        return gameMap.inGamePlayers.filter {

            it.team == Team.RED && it.status == PlayerStatus.PLAYING

        }

    }

    fun refreshGameMap(gameMap: GameMap) {

        gameMap.inGamePlayers.clear()
        gameMap.spectators.clear()
        gameMap.lobbyBoards.clear()
        gameMap.gameBoards.clear()

        gameMap.status = ArenaStatus.RELOADING

        if (plugin.worldManager.loadWorldFromMaps(gameMap.arena.name)) {
            plugin.info("${gameMap.arena.name} is refreshed.")
            gameMap.arena.reloadLocations()
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
                            plugin.scoreboardManager.removeBoards(player, gameMap)
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

                                player.sendMessage(plugin.messages.youWon)

                                plugin.config.winCommands.forEach {
                                    plugin.suspendFunctionSync {
                                        plugin.server.dispatchCommand(plugin.server.consoleSender, it.replace("%player%",
                                            plugin.cacheManager.getPlayerName(player.uniqueId.toString())))
                                    }
                                }

                            } else {

                                player.sendMessage(plugin.messages.youLost)

                            }

                        }

                    }

                }.invokeOnCompletion {

                    Scopes.supervisorScope.launch {
                        plugin.suspendFunctionSync {
                            refreshGameMap(gameMap)
                        }
                    }

                }

            }

        }

    }

    fun playerKilled(gamePlayer: GamePlayer, gameMap: GameMap) {

        val player = getPlayer(gamePlayer)!!

        gamePlayer.status = PlayerStatus.DEAD

        Scopes.supervisorScope.launch {
            plugin.suspendFunctionSync {

                Utils.respawnPlayer(player)

            }
        }.invokeOnCompletion {
            player.clearAllInventory()

            Scopes.supervisorScope.launch {
                plugin.suspendFunctionSync {
                    player.teleport(gameMap.arena.spectatorSpawn!!)
                }
            }.invokeOnCompletion {
                player.sendMessage(plugin.messages.spectating)
            }

        }

    }

    fun playerLeft(gamePlayer: GamePlayer, gameMap: GameMap) {

        if (!plugin.cacheManager.quitters.contains(gamePlayer.uuid))
            plugin.cacheManager.quitters.add(gamePlayer.uuid)

        plugin.scoreboardManager.removeBoards(getPlayer(gamePlayer)!!, gameMap)

        val team = gamePlayer.team

        println("eski alive players boyutu ${gameMap.getAlivePlayers(team).size}")

        gamePlayer.status = PlayerStatus.DEAD

        println("yenisi ${gameMap.getAlivePlayers(team).size}")

        if (gameMap.getAlivePlayers(team).isEmpty()) {
            if (team == Team.RED)
                win(gameMap, Team.BLUE)
            else
                win(gameMap, Team.RED)

        }

    }

    fun leaveLobby(player: Player, gameMap: GameMap) {

        plugin.scoreboardManager.removeLobbyBoard(player, gameMap)

        if (gameMap.lobbyPlayers.contains(player.uniqueId.toString()))
            gameMap.lobbyPlayers.remove(player.uniqueId.toString())

        player.teleport(plugin.spawn!!)

        if (plugin.kitManager.playerKits.containsKey(player.uniqueId.toString()))
            plugin.kitManager.giveKit(player,
                plugin.kitManager.playerKits[player.uniqueId.toString()]!!)

        plugin.scoreboardManager.updateLobby(gameMap)

        //TODO: Lobby is complete, game test is now on.

        //TODO: Lobby block breaking happens

    }

    fun isPlayerSpectating(player: Player): Boolean {

        if (!playerInAnyGame(player)) return false

        val gamePlayer = getGamePlayer(player)!!

        return gamePlayer.status == PlayerStatus.SPECTATING

    }

    fun leaveGame(player: Player) {

        player.gameMode = GameMode.SURVIVAL

        val gameMap = getGameMap(player)!!

        player.teleport(plugin.spawn!!)

        if (plugin.kitManager.playerKits.containsKey(player.uniqueId.toString())) {
            plugin.kitManager.giveKit(player, plugin.kitManager.playerKits[player.uniqueId.toString()]!!)
        }

        plugin.scoreboardManager.removeBoards(player, gameMap)

    }

}