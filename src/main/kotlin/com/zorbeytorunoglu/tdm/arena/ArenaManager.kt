package com.zorbeytorunoglu.tdm.arena

import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.kLib.configuration.createFileWithPath
import com.zorbeytorunoglu.kLib.extensions.info
import com.zorbeytorunoglu.kLib.extensions.severe
import com.zorbeytorunoglu.kLib.extensions.toLegibleString
import com.zorbeytorunoglu.kLib.extensions.warning
import com.zorbeytorunoglu.kLib.task.MCDispatcher
import com.zorbeytorunoglu.kLib.task.Scopes
import com.zorbeytorunoglu.kLib.task.suspendFunctionAsync
import com.zorbeytorunoglu.kLib.task.suspendFunctionSync
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.game.GameMap
import kotlinx.coroutines.launch
import java.io.File

class ArenaManager(val plugin: TDM) {

    val arenas: HashMap<String, Arena> = HashMap()
    val gameMaps: HashMap<Arena, GameMap> = HashMap()
    val arenaResources: HashMap<Arena, Resource> = HashMap()

    fun arenaResourceExists(arena: Arena): Boolean {
        return arenaResources.containsKey(arena)
    }

    fun getArenaResource(arena: Arena): Resource {
        return arenaResources[arena]!!
    }

    fun arenaExists(name: String): Boolean {
        return arenas.containsKey(name)
    }

    fun gameMapExists(name: String): Boolean {
        if (!arenaExists(name)) return false
        return gameMaps.containsKey(getArena(name))
    }

    fun gameMapExists(arena: Arena): Boolean {
        return gameMaps.containsKey(arena)
    }

    fun getGameMap(name: String): GameMap {
        return gameMaps[getArena(name)]!!
    }

    fun getGameMap(arena: Arena): GameMap {
        return gameMaps[arena]!!
    }

    fun getArena(name: String): Arena {
        return arenas[name]!!
    }

    fun saveMapConfig(arena: Arena): Resource {

        val resource = plugin.createFileWithPath(plugin.dataFolder.absolutePath.toString()+"/mapConfigs","${arena.name}.yml")

        resource.set("name", arena.name)
        resource.set("displayname", arena.displayName)
        resource.set("redSpawn", arena.redSpawn)
        resource.set("blueSpawn", arena.blueSpawn)
        resource.set("lobby", arena.lobby)
        resource.set("spectatorSpawn", arena.spectatorSpawn)
        resource.set("maxPlayers", arena.maxPlayers)
        resource.set("minPlayers", arena.minPlayers)
        resource.set("time", arena.time)
        resource.set("playerLives", arena.playerLives)

        if (arena.redGate.isNotEmpty()) {

            arena.redGate.keys.forEach {
                resource.set(it.toLegibleString(), arena.redGate[it].toString())
            }

        }

        if (arena.blueGate.isNotEmpty()) {

            arena.blueGate.keys.forEach {
                resource.set(it.toLegibleString(), arena.blueGate[it].toString())
            }

        }

        resource.save()

        arenaResources[arena] = resource

        return resource

    }

    fun unloadArenas() {

        if (gameMaps.isEmpty()) return

        Scopes.supervisorScope.launch(MCDispatcher(plugin, async = false)) {

            gameMaps.keys.forEach {
                plugin.logger.info("[TDM] Unloading ${it.name}...")
                plugin.suspendFunctionSync {

                    plugin.worldManager.deleteWorld(it.name, true)
                    plugin.info("[TDM] Map ${it.name} is unloaded and deleted successfully. It will be restored on start.")

                }

            }

        }

    }

    fun arenaBusy(arena: Arena): Boolean {
        if (!gameMapExists(arena)) return false
        return getGameMap(arena).status == ArenaStatus.IN_GAME || getGameMap(arena).status == ArenaStatus.RELOADING
    }

    fun loadArenas() {

        val mapConfigsDir: File = File(plugin.dataFolder, "mapConfigs")
        val mapsDir = File(plugin.dataFolder, "maps")

        if (!mapsDir.exists()) return

        if (!mapConfigsDir.exists()) return

        val configs = mapConfigsDir.listFiles() ?: return

        val maps = mapsDir.listFiles() ?: return

        Scopes.supervisorScope.launch {

            plugin.info("[TDM] Loading the team death match maps...")

            for (file in maps) {

                plugin.suspendFunctionSync {

                    plugin.info("[TDM] Loading ${file.name}...")

                    if (plugin.worldManager.loadWorldFromMaps(file.name)) {
                        plugin.info("[TDM] ${file.name} is loaded.")
                        plugin.arenaManager.arenas[file.name] = Arena(file.name)
                    } else {
                        plugin.severe("[TDM] Map ${file.name} could not be loaded! Skipping.")
                    }

                }

            }

        }.invokeOnCompletion {
            plugin.info("[TDM] Loading configurations of maps...")
            Scopes.supervisorScope.launch {
                for (file in configs) {

                    plugin.info("[TDM] Loading ${file.name}...")

                    plugin.suspendFunctionAsync {
                        val resource = plugin.createFileWithPath(mapConfigsDir.absolutePath, file.name)
                        resource.load()
                        if (!isValidMapConfig(resource)) return@suspendFunctionAsync

                        val arena = plugin.arenaManager.getArena(resource.getString("name")!!)

                        arenaResources[arena] = resource

                        arena.displayName = resource.getString("displayname")!!
                        arena.redSpawn = resource.getLocation("redSpawn")
                        arena.blueSpawn = resource.getLocation("blueSpawn")
                        arena.lobby = resource.getLocation("lobby")
                        arena.spectatorSpawn = resource.getLocation("spectatorSpawn")
                        arena.maxPlayers = resource.getInt("maxPlayers")
                        arena.minPlayers = resource.getInt("minPlayers")
                        arena.time = resource.getInt("time")
                        arena.playerLives = resource.getInt("playerLives")

                        if (!arena.isSetup()) {
                            plugin.warning("[TDM] ${arena.name} is not setup properly. Try to setup this map again. Skipping.")
                            return@suspendFunctionAsync
                        }

                        plugin.arenaManager.gameMaps[arena] = GameMap(arena)

                        plugin.info("[TDM] ${arena.name} is loaded successfully.")

                    }
                }
            }
        }

    }

    private fun isValidMapConfig(resource: Resource): Boolean {

        val name = resource.getString("name")
        val displayName = resource.getString("displayname")

        if (name == null) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid name. It will not be loaded.")
            return false
        }

        if (name.isBlank()) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid name. It will not be loaded.")
            return false
        }

        if (!plugin.server.worlds.stream().anyMatch { it.name == name }) {
            plugin.severe("[TDM] Map could not be found in loaded maps list. It will not be loaded.")
            return false
        }

        if (displayName == null) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid display name. It will not be loaded.")
            return false
        }

        if (displayName.isBlank()) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid display name. It will not be loaded.")
            return false
        }

        if (!resource.isSet("redSpawn")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid red spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isLocation("redSpawn")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid red spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isSet("blueSpawn")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid blue spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isLocation("blueSpawn")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid blue spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isSet("lobby")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid lobby spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isLocation("lobby")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid lobby spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isSet("spectatorSpawn")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid spectator spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isLocation("spectatorSpawn")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid spectator spawn location. It will not be loaded.")
            return false
        }

        if (!resource.isSet("maxPlayers")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid max players. It will not be loaded.")
            return false
        }

        if (resource.getInt("maxPlayers") <= 1) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid max players. It will not be loaded.")
            return false
        }

        if (!resource.isSet("minPlayers")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid min players. It will not be loaded.")
            return false
        }

        if (resource.getInt("minPlayers") <= 0) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid min players. It will not be loaded.")
            return false
        }

        if (!resource.isSet("time")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid time. It will not be loaded.")
            return false
        }

        if (resource.getInt("time") <= 0) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid time. It will not be loaded.")
            return false
        }

        if (!resource.isSet("playerLives")) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid player lives. It will not be loaded.")
            return false
        }

        if (resource.getInt("playerLives") <= 0) {
            plugin.logger.severe("[TDM] ${resource.name} does not have a valid player lives. It will not be loaded.")
            return false
        }

        return true

    }

}