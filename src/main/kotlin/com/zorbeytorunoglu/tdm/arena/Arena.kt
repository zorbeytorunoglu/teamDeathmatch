package com.zorbeytorunoglu.tdm.arena

import com.zorbeytorunoglu.tdm.inventory.Kit
import org.bukkit.Bukkit
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
    var cooldown: Int = 0

    fun isSetup(): Boolean {

        return redSpawn != null && blueSpawn != null && lobby != null && spectatorSpawn != null && maxPlayers > 0 && minPlayers > 0 &&
                playerLives > 0 && time > 0

    }

    fun reloadLocations() {

        if (redSpawn != null) {
            redSpawn = Location(Bukkit.getServer().getWorld(redSpawn!!.world!!.name),
                redSpawn!!.x, redSpawn!!.y, redSpawn!!.z, redSpawn!!.yaw, redSpawn!!.pitch)
        }

        if (blueSpawn != null) {
            blueSpawn = Location(Bukkit.getServer().getWorld(blueSpawn!!.world!!.name),
                blueSpawn!!.x, blueSpawn!!.y, blueSpawn!!.z,
                blueSpawn!!.yaw, blueSpawn!!.pitch)
        }

        if (lobby != null) {
            lobby = Location(Bukkit.getServer().getWorld(lobby!!.world!!.name), lobby!!.x, lobby!!.y, lobby!!.z,
                lobby!!.yaw, lobby!!.pitch)
        }

        if (spectatorSpawn != null) {
            spectatorSpawn = Location(
                Bukkit.getServer().getWorld(spectatorSpawn!!.world!!.name), spectatorSpawn!!.x,
                spectatorSpawn!!.y, spectatorSpawn!!.z, spectatorSpawn!!.yaw, spectatorSpawn!!.pitch)
        }

        if (gates.isNotEmpty()) {

            val newList = arrayListOf<Location>()

            gates.forEach {
                newList.add(Location(Bukkit.getServer().getWorld(it.world!!.name),
                    it.x, it.y, it.z, it.yaw, it.pitch))
            }

            gates = newList

        }

    }

}