package com.zorbeytorunoglu.tdm.listeners

import com.zorbeytorunoglu.tdm.TDM
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

class WorldInit(plugin: TDM): Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onWorldInit(event: WorldInitEvent) {

        event.world.keepSpawnInMemory = false

    }

}