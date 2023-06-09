package com.zorbeytorunoglu.tdm

import com.zorbeytorunoglu.kLib.MCPlugin
import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.kLib.configuration.createYamlResource
import com.zorbeytorunoglu.tdm.arena.ArenaManager
import com.zorbeytorunoglu.tdm.cache.CacheManager
import com.zorbeytorunoglu.tdm.commands.TDMCmd
import com.zorbeytorunoglu.tdm.configuration.Config
import com.zorbeytorunoglu.tdm.configuration.messages.Messages
import com.zorbeytorunoglu.tdm.game.GameManager
import com.zorbeytorunoglu.tdm.inventory.KitManager
import com.zorbeytorunoglu.tdm.listeners.GameListener
import com.zorbeytorunoglu.tdm.listeners.GateSelection
import com.zorbeytorunoglu.tdm.listeners.LobbyListener
import com.zorbeytorunoglu.tdm.listeners.WorldInit
import com.zorbeytorunoglu.tdm.scoreboard.ScoreboardManager
import com.zorbeytorunoglu.tdm.world.WorldManager
import org.bukkit.Location

class TDM: MCPlugin() {

    lateinit var worldManager: WorldManager
    lateinit var arenaManager: ArenaManager
    lateinit var kitManager: KitManager
    lateinit var gameManager: GameManager
    lateinit var cacheManager: CacheManager
    lateinit var scoreboardManager: ScoreboardManager

    lateinit var messages: Messages
    lateinit var config: Config

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

        messages = Messages(messagesResource)
        config = Config(configResource)

        worldManager = WorldManager(this)
        arenaManager = ArenaManager(this)
        kitManager = KitManager(this)
        gameManager = GameManager(this)
        cacheManager = CacheManager(this, createYamlResource("cache.yml"))
        scoreboardManager = ScoreboardManager(this)

        TDMCmd(this)

        WorldInit(this)
        GameListener(this)
        LobbyListener(this)
        GateSelection(this)

        cacheManager.loadUUIDs()
        cacheManager.loadQuitters()

        arenaManager.loadArenas()

    }

    override fun onDisable() {
        super.onDisable()

        cacheManager.saveUUIDs()
        cacheManager.saveQuitters()

        arenaManager.unloadArenas()
        kitManager.savePlayerKitsToResource()

    }

    private fun getSpawn(spawnResource: Resource): Location? {
        spawnResource.load()
        if (!spawnResource.isLocation("spawn")) return null
        return spawnResource.getLocation("spawn")
    }

}