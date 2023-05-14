package com.zorbeytorunoglu.tdm.game

import com.zorbeytorunoglu.tdm.arena.Arena
import com.zorbeytorunoglu.tdm.arena.ArenaStatus
import com.zorbeytorunoglu.tdm.game.player.GamePlayer
import com.zorbeytorunoglu.tdm.game.player.Team
import com.zorbeytorunoglu.tdm.scoreboard.FastBoard
import org.bukkit.entity.Player

class GameMap(val arena: Arena) {

    var status: ArenaStatus = ArenaStatus.WAITING
    val lobbyPlayers: ArrayList<String> = ArrayList()
    val inGamePlayers = ArrayList<GamePlayer>()
    val spectators = ArrayList<String>()
    val time: Int = arena.time*60
    val gameBoards = HashMap<String, FastBoard>()
    val lobbyBoards = HashMap<String, FastBoard>()

    fun getBlueTeam(): List<GamePlayer> = inGamePlayers.filter { it.team == Team.BLUE }

    fun getRedTeam(): List<GamePlayer> = inGamePlayers.filter { it.team == Team.RED }

    fun playerInGame(player: Player): Boolean {

        if (inGamePlayers.isEmpty()) return false

        return inGamePlayers.any { it.uuid == player.uniqueId.toString() }

    }

    fun playerIsSpectator(player: Player): Boolean {

        if (spectators.isEmpty()) return false

        return spectators.contains(player.uniqueId.toString())

    }

    fun hasMinPlayers(): Boolean = arena.minPlayers <= lobbyPlayers.size

    fun hasSpectator(): Boolean = spectators.isNotEmpty()

    fun getPlayerTeam(player: Player): Team {

        return if (getBlueTeam().any {
                it.uuid == player.uniqueId.toString()
            }) Team.BLUE
        else Team.RED

    }

}