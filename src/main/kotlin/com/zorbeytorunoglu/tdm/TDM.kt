package com.zorbeytorunoglu.tdm

import com.zorbeytorunoglu.kLib.MCPlugin
import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.kLib.configuration.createYamlResource
import com.zorbeytorunoglu.kLib.extensions.fromLegibleString
import com.zorbeytorunoglu.tdm.arena.ArenaManager
import com.zorbeytorunoglu.tdm.commands.TDMCmd
import com.zorbeytorunoglu.tdm.configuration.messages.Messages
import com.zorbeytorunoglu.tdm.listeners.WorldInit
import com.zorbeytorunoglu.tdm.world.WorldManager
import org.bukkit.Location

class TDM: MCPlugin() {

    lateinit var worldManager: WorldManager
    lateinit var arenaManager: ArenaManager

    lateinit var messages: Messages
    lateinit var spawnResource: Resource

    var spawn: Location? = null

    override fun onEnable() {

        super.onEnable()

        val configResource = createYamlResource("config.yml")
        configResource.load()

        val messagesResource = createYamlResource("messages.yml")
        messagesResource.load()

        spawnResource = createYamlResource("spawn.yml")
        spawn = getSpawn(spawnResource)

        this.messages = Messages(messagesResource)

        this.worldManager = WorldManager(this)
        this.arenaManager = ArenaManager(this)

        TDMCmd(this)
        WorldInit(this)

        arenaManager.loadArenas()

    }

    override fun onDisable() {
        super.onDisable()

        arenaManager.unloadArenas()

    }

    private fun getSpawn(spawnResource: Resource): Location? {
        spawnResource.load()
        if (!spawnResource.isLocation("spawn")) return null
        return spawnResource.getLocation("spawn")
    }

}