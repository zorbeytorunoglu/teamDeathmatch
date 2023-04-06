package com.zorbeytorunoglu.tdm.world

import com.google.common.collect.Lists
import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.kLib.configuration.createFileWithPath
import com.zorbeytorunoglu.kLib.extensions.toLegibleString
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.arena.Arena
import com.zorbeytorunoglu.tdm.utils.WorldUtils
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.World
import org.bukkit.World.Environment
import org.bukkit.WorldCreator
import org.bukkit.event.player.PlayerTeleportEvent
import java.io.*


class WorldManager(val plugin: TDM) {

    fun saveMap(mapName: String): Boolean {

        val world = plugin.server.getWorld(mapName) ?: return false

        world.players.stream().forEach { it.teleport(plugin.spawn!!, PlayerTeleportEvent.TeleportCause.PLUGIN) }

        unloadWorld(mapName, true)

        val dataDirectory = File(plugin.dataFolder, "maps")
        val target = File(dataDirectory, mapName)

        deleteFile(target)

        val source = File(plugin.server.worldContainer.absolutePath, mapName)

        copyWorld(source, target)

        return true

    }

    fun deleteWorld(name: String, removeFile: Boolean) {
        unloadWorld(name, false)
        val target: File = File(plugin.server.worldContainer.absolutePath, name)
        if (removeFile) deleteFile(target)
    }

    fun deleteFromMaps(name: String) {

        val dataDirectory = File(plugin.dataFolder, "maps")
        val target = File(dataDirectory, name)

        deleteFile(target)

    }

    fun deleteFile(path: File) {
        if (path.exists()) {
            val files = path.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        deleteFile(file)
                    } else {
                        file.delete()
                    }
                }
            }
        }
        path.delete()
    }

    fun createEmptyWorld(name: String, environment: Environment): World? {
        if (Bukkit.getWorld(name) == null) {
            loadWorld(name, environment)
            return Bukkit.getWorld(name)
        }
        return null
    }

    fun loadWorld(worldName: String?, environment: Environment?): Boolean {
        val worldCreator = WorldCreator(worldName!!)
        worldCreator.environment(environment!!)
        worldCreator.generateStructures(false)
        worldCreator.generator(WorldUtils.getChunkGenerator())
        val world = worldCreator.createWorld()
        world!!.difficulty = Difficulty.NORMAL
        world.setSpawnFlags(true, true)
        world.pvp = true
        world.setStorm(false)
        world.isThundering = false
        world.weatherDuration = Int.MAX_VALUE
        world.keepSpawnInMemory = false
        world.setTicksPerAnimalSpawns(1)
        world.setTicksPerMonsterSpawns(1)
        world.isAutoSave = false
        WorldUtils.setGameRule(world, "doMobSpawning", "false")
        WorldUtils.setGameRule(world, "mobGriefing", "true")
        WorldUtils.setGameRule(world, "doFireTick", "true")
        WorldUtils.setGameRule(world, "showDeathMessages", "false")
        WorldUtils.setGameRule(world, "announceAdvancements", "false")
        WorldUtils.setGameRule(world, "doDaylightCycle", "false")
        var loaded = false
        for (w in plugin.server.worlds) {
            if (w.name == world.name) {
                loaded = true
                break
            }
        }
        return loaded
    }

    fun unloadWorld(pWorld: String, save: Boolean) {
        val world: World? = plugin.server.getWorld(pWorld)
        if (world != null) {
            if (world.players.isNotEmpty()) {
                for (p in world.players) {
                    p.teleport(plugin.spawn!!)
                }
            }
            plugin.server.unloadWorld(world, save)
        }
    }

    fun copyWorld(source: File, target: File) {
        try {
            val ignore: List<String> = Lists.newArrayList("uid.dat", "session.dat", "session.lock")
            if (!ignore.contains(source.name)) {
                if (source.isDirectory) {
                    if (!target.exists() &&
                        target.mkdirs()
                    ) {
                        val files = source.list()
                        if (files != null) {
                            for (file in files) {
                                val srcFile = File(source, file)
                                val destFile = File(target, file)
                                copyWorld(srcFile, destFile)
                            }
                        }
                    }
                } else {
                    val `in`: InputStream = FileInputStream(source)
                    val out: OutputStream = FileOutputStream(target)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (`in`.read(buffer).also { length = it } > 0) out.write(buffer, 0, length)
                    `in`.close()
                    out.close()
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadWorldFromMaps(worldName: String): Boolean {
        val dataDirectory: File = File(plugin.dataFolder, "maps")
        val source = File(dataDirectory, worldName)
        val target: File = File(plugin.server.worldContainer.absolutePath, worldName)
        var mapExists = false
        if (target.isDirectory) {
            val list = target.list()
            if (list != null && list.isNotEmpty()) {
                mapExists = true
            }
        }
        if (mapExists) {
            deleteWorld(worldName, true)
        }
        copyWorld(source, target)
        return loadWorld(worldName, Environment.NORMAL)
    }

}