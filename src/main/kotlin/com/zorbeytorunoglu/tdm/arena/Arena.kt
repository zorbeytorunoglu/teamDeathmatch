package com.zorbeytorunoglu.tdm.arena

import com.zorbeytorunoglu.tdm.inventory.Kit
import org.bukkit.Location
import org.bukkit.Material

class Arena(val name: String) {

    var displayName: String = name
    var redSpawn: Location? = null
    var blueSpawn: Location? = null
    var lobby: Location? = null
    var spectatorSpawn: Location? = null
    var maxPlayers: Int = 10
    var minPlayers: Int = 2
    var gates = ArrayList<Location>()
    var time: Int = 30
    var playerLives: Int = 2
    var redKit: Kit = Kit(mutableListOf(), mutableListOf())
    var blueKit: Kit = Kit(mutableListOf(), mutableListOf())

    fun isSetup(): Boolean {

        return redSpawn != null && blueSpawn != null && lobby != null && spectatorSpawn != null && maxPlayers > 0 && minPlayers > 0 &&
                playerLives > 0 && time > 0

    }

}