package com.zorbeytorunoglu.tdm.cache.player

import com.zorbeytorunoglu.kLib.extensions.info
import com.zorbeytorunoglu.tdm.TDM
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerUUIDCache(private val plugin: TDM): Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {

        if (!plugin.cacheManager.playerUUIDs.containsKey(event.player.uniqueId.toString())) {
            plugin.cacheManager.playerUUIDs[event.player.uniqueId.toString()] = event.player.name

            plugin.info("${event.player.name} is cached.")
        }

        if (plugin.cacheManager.quitters.contains(event.player.uniqueId.toString())) {

            plugin.server.scheduler.scheduleSyncDelayedTask(plugin, Runnable {
                event.player.teleport(plugin.spawn!!)
                if (plugin.kitManager.playerKits.containsKey(event.player.uniqueId.toString())) {
                    plugin.kitManager.giveKit(event.player, plugin.kitManager.playerKits[event.player.uniqueId.toString()]!!)
                }
                plugin.cacheManager.quitters.remove(event.player.uniqueId.toString())
            }, 10L)

        }

    }

}