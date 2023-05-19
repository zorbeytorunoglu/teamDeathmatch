package com.zorbeytorunoglu.tdm.scoreboard

import com.zorbeytorunoglu.kLib.configuration.createYamlResource
import com.zorbeytorunoglu.kLib.extensions.colorHex
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.configuration.scoreboards.Scoreboards
import com.zorbeytorunoglu.tdm.game.GameMap
import com.zorbeytorunoglu.tdm.game.player.GamePlayer
import com.zorbeytorunoglu.tdm.game.player.Team
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class ScoreboardManager(private val plugin: TDM) {

    val scoreboardsConfig: Scoreboards

    init {
        val scoreboardResource = plugin.createYamlResource("scoreboards.yml")
        scoreboardResource.load()
        scoreboardsConfig = Scoreboards(scoreboardResource)
    }

    fun giveLobbyBoard(player: Player, gameMap: GameMap) {

        val board = FastBoard(player)

        board.updateTitle(applyPlaceholders(scoreboardsConfig.lobbyScoreboardTitle, gameMap).colorHex)

        val newLines = ArrayList<String>()

        scoreboardsConfig.lobbyScoreboardLines.forEach {

            newLines.add(applyPlaceholders(it, gameMap).colorHex)

        }

        board.updateLines(newLines)

        gameMap.lobbyBoards[player.uniqueId.toString()] = board

    }

    fun updateLobbyBoard(player: Player, gameMap: GameMap) {

        if (!gameMap.lobbyBoards.containsKey(player.uniqueId.toString())) return

        val board = gameMap.lobbyBoards[player.uniqueId.toString()]!!

        val newLines = ArrayList<String>()

        scoreboardsConfig.lobbyScoreboardLines.forEach {

            newLines.add(applyPlaceholders(it, gameMap).colorHex)

        }

        board.updateLines(newLines)

    }

    fun removeLobbyBoard(player: Player, gameMap: GameMap) {

        if (!gameMap.lobbyBoards.containsKey(player.uniqueId.toString())) return

        val board = gameMap.lobbyBoards[player.uniqueId.toString()]

        gameMap.lobbyBoards.remove(player.uniqueId.toString())

        board?.delete()

    }

    fun giveGameBoard(gamePlayer: GamePlayer, gameMap: GameMap) {

        val player = plugin.gameManager.getPlayer(gamePlayer)

        val board = FastBoard(player)

        board.updateTitle(applyPlaceholdersForGame(scoreboardsConfig.gameScoreboardTitle, gameMap, gamePlayer))

        val newLines = ArrayList<String>()

        scoreboardsConfig.gameScoreboardLines.forEach {

            newLines.add(applyPlaceholdersForGame(it, gameMap, gamePlayer))

        }

        board.updateLines(newLines)

        gameMap.gameBoards[player!!.uniqueId.toString()] = board

    }

    fun giveGameBoard(player: Player, gameMap: GameMap) {

        val board = FastBoard(player)

        board.updateTitle(scoreboardsConfig.gameScoreboardTitle
            .replace("%arena%", gameMap.arena.displayName.colorHex))

        val newLines = ArrayList<String>()

        val gamePlayer = plugin.gameManager.getGamePlayer(player)!!

        scoreboardsConfig.gameScoreboardLines.forEach {

            newLines.add(it
                .replace("%arena%", gameMap.arena.displayName)
                .replace("%players%", "${gameMap.lobbyPlayers.size}")
                .replace("%min_players%", "${gameMap.arena.minPlayers}")
                .replace("%max_players%", "${gameMap.arena.maxPlayers}")
                .replace("%red_alive%", "${plugin.gameManager.getRedAlivePlayers(gameMap).size}")
                .replace("%blue_alive%", "${plugin.gameManager.getBlueAlivePlayers(gameMap).size}")
                .replace("%live%", "${gamePlayer.stats.live}")
                .replace("%kills%", "${gamePlayer.stats.kills}")
                .colorHex)

        }

        board.updateLines(newLines)

        gameMap.gameBoards[player.uniqueId.toString()] = board

    }

    fun updateGameBoard(player: Player, gameMap: GameMap) {

        if (!gameMap.gameBoards.containsKey(player.uniqueId.toString())) return

        val board = gameMap.gameBoards[player.uniqueId.toString()]!!

        val newLines = ArrayList<String>()

        val gamePlayer = plugin.gameManager.getGamePlayer(player)!!

        scoreboardsConfig.gameScoreboardLines.forEach {

            newLines.add(applyPlaceholdersForGame(it, gameMap, gamePlayer))

        }

        board.updateLines(newLines)

    }

    fun updateGame(gameMap: GameMap) {

        if (gameMap.gameBoards.isEmpty()) return

        val newLines = ArrayList<String>()

        gameMap.inGamePlayers.forEach { gamePlayer ->

            updateGameBoard(plugin.gameManager.getPlayer(gamePlayer)!!, gameMap)

        }

    }

    fun removeGameBoard(player: Player, gameMap: GameMap) {

        if (!gameMap.gameBoards.containsKey(player.uniqueId.toString())) return

        val board = gameMap.gameBoards[player.uniqueId.toString()]

        gameMap.gameBoards.remove(player.uniqueId.toString())

        board?.delete()

    }

    fun updateLobby(gameMap: GameMap) {

        gameMap.lobbyPlayers.forEach {

            val player = plugin.server.getPlayer(UUID.fromString(it))!!

            updateLobbyBoard(player, gameMap)

        }

    }

    fun removeBoards(player: Player, gameMap: GameMap) {
        if (gameMap.gameBoards.containsKey(player.uniqueId.toString()))
            plugin.scoreboardManager.removeGameBoard(player, gameMap)

        if (gameMap.lobbyBoards.containsKey(player.uniqueId.toString()))
            plugin.scoreboardManager.removeLobbyBoard(player, gameMap)
    }

    private fun applyPlaceholdersForGame(string: String, gameMap: GameMap, gamePlayer: GamePlayer): String {

        return string.replace("%arena%", gameMap.arena.name)
            .replace("%arena_displayname%", gameMap.arena.displayName)
            .replace("%players%", "${gameMap.inGamePlayers.size}")
            .replace("%min_players%", "${gameMap.arena.minPlayers}")
            .replace("%max_players%", "${gameMap.arena.maxPlayers}")
            .replace("%red_alive%", "${plugin.gameManager.getRedAlivePlayers(gameMap).size}")
            .replace("%blue_alive%", "${plugin.gameManager.getBlueAlivePlayers(gameMap).size}")
            .replace("%live%", "${gamePlayer.stats.live}")
            .replace("%team%", if (gamePlayer.team == Team.RED) plugin.messages.red else plugin.messages.blue)
            .replace("%kills%", "${gamePlayer.stats.kills}").colorHex

    }

    private fun applyPlaceholders(string: String, gameMap: GameMap): String {

        return string.replace("%arena_displayname%", gameMap.arena.displayName)
            .replace("%arena%", gameMap.arena.name)
            .replace("%min_players%", "${gameMap.arena.minPlayers}")
            .replace("%max_players%", "${gameMap.arena.maxPlayers}")
            .replace("%players%", "${gameMap.lobbyPlayers.size}")

    }

}