package com.zorbeytorunoglu.tdm.cache

import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.kLib.extensions.info
import com.zorbeytorunoglu.kLib.extensions.registerEvents
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.cache.player.PlayerUUIDCache
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList


class CacheManager(private val plugin: TDM, private val resource: Resource) {

    init {
        plugin.registerEvents(PlayerUUIDCache(plugin))
    }

    val playerUUIDs = HashMap<String, String>()
    val quitters = ArrayList<String>()

    fun saveQuitters() {

        if (quitters.isEmpty()) return

        resource.set("quitters", quitters)

        resource.save()

    }

    fun loadQuitters() {

        if (!resource.isList("quitters")) return

        resource.getStringList("quitters").forEach {
            quitters.add(it)
        }

        resource.set("quitters", null)

        resource.save()

    }

    fun saveUUIDs() {

        if (playerUUIDs.isEmpty()) return

        for (key in playerUUIDs.keys)
            resource.set("names.$key", playerUUIDs[key])

        resource.save()

    }

    fun loadUUIDs() {

        if (resource.getConfigurationSection("names") == null) return

        val keys = resource.getConfigurationSection("names")!!.getKeys(false)

        for (key in keys) {
            try {
                if (UUID.fromString(resource.getString("names.$key")) != null) {
                    playerUUIDs[key] = resource.getString("names.$key")!!
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