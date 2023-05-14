package com.zorbeytorunoglu.tdm.scoreboard

import com.zorbeytorunoglu.kLib.configuration.createYamlResource
import com.zorbeytorunoglu.kLib.extensions.colorHex
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.configuration.scoreboards.Scoreboards
import com.zorbeytorunoglu.tdm.game.GameMap
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

        board.updateTitle(scoreboardsConfig.lobbyScoreboardTitle.replace("%arena%", gameMap.arena.displayName.colorHex))

        val newLines = ArrayList<String>()

        scoreboardsConfig.lobbyScoreboardLines.forEach {

            newLines.add(it
                .replace("%arena%", gameMap.arena.displayName)
                .replace("%players%", "${gameMap.lobbyPlayers.size}")
                .replace("%min_players%", "${gameMap.arena.minPlayers}")
                .replace("%max_players%", "${gameMap.arena.maxPlayers}")
                .colorHex)

        }

        board.updateLines(newLines)

        gameMap.lobbyBoards[player.uniqueId.toString()] = board

    }

    fun updateLobbyBoard(player: Player, gameMap: GameMap) {

        if (!gameMap.lobbyBoards.containsKey(player.uniqueId.toString())) return

        val board = gameMap.lobbyBoards[player.uniqueId.toString()]!!

        val newLines = ArrayList<String>()

        scoreboardsConfig.lobbyScoreboardLines.forEach {

            newLines.add(it
                .replace("%arena%", gameMap.arena.displayName)
                .replace("%players%", "${gameMap.lobbyPlayers.size}")
                .replace("%min_players%", "${gameMap.arena.minPlayers}")
                .replace("%max_players%", "${gameMap.arena.maxPlayers}")
                .colorHex)

        }

        board.updateLines(newLines)

    }

    fun removeLobbyBoard(player: Player, gameMap: GameMap) {

        if (!gameMap.lobbyBoards.containsKey(player.uniqueId.toString())) return

        val board = gameMap.lobbyBoards[player.uniqueId.toString()]

        gameMap.lobbyBoards.remove(player.uniqueId.toString())

        board?.delete()

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

}