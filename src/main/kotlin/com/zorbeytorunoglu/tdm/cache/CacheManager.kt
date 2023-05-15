package com.zorbeytorunoglu.tdm.cache

import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.kLib.extensions.info
import com.zorbeytorunoglu.kLib.extensions.registerEvents
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.cache.player.PlayerUUIDCache
import org.bukkit.entity.Player
import java.util.*


class CacheManager(private val plugin: TDM, private val resource: Resource) {

    init {
        plugin.registerEvents(PlayerUUIDCache(plugin))
    }

    val playerUUIDs = HashMap<String, String>()

    fun saveUUIDs() {

        if (playerUUIDs.isEmpty()) return

        for (key in playerUUIDs.keys)
            resource.set(key,playerUUIDs[key])

        resource.save()

    }

    fun loadUUIDs() {

        val keys = resource.getKeys(false)

        for (key in keys) {
            try {
                if (UUID.fromString(resource.getString(key)) != null) {
                    playerUUIDs[key] = resource.getString(key)!!
                    plugin.info("$key is cached.")
                }
            } catch (ex: Exception) {
                continue
            }
        }

    }

    fun getPlayerName(uuid: UUID): String {

        return playerUUIDs[uuid.toString()]!!

    }

    fun getPlayerName(uuidString: String): String {

        return playerUUIDs[uuidString]!!

    }

    fun getPlayer(uuidString: String): Player? {

        return plugin.server.getPlayer(UUID.fromString(uuidString))

    }

}