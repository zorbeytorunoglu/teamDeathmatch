package com.zorbeytorunoglu.tdm.game

import com.zorbeytorunoglu.tdm.arena.Arena
import com.zorbeytorunoglu.tdm.arena.ArenaStatus
import org.bukkit.entity.Player

class GameMap(val arena: Arena) {

    // lobi olsun bu

    var status: ArenaStatus = ArenaStatus.WAITING
    var players: ArrayList<Player> = ArrayList()
    var redTeam: HashMap<Player, PlayerStats> = HashMap()
    var blueTeam: HashMap<Player, PlayerStats> = HashMap()
    var time: Int = arena.time*60

}