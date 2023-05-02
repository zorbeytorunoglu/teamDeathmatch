package com.zorbeytorunoglu.tdm.cache.player

import com.zorbeytorunoglu.kLib.extensions.info
import com.zorbeytorunoglu.tdm.TDM
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerUUIDCache(private val plugin: TDM): Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {

        if (plugin.cacheManager.playerUUIDs.containsKey(event.player.uniqueId.toString())) return

        plugin.cacheManager.playerUUIDs[event.player.uniqueId.toString()] = event.player.name

        plugin.info("${event.player.name} is cached.")

    }

}