package com.zorbeytorunoglu.tdm.commands

import com.zorbeytorunoglu.kLib.extensions.isAlphanumeric
import com.zorbeytorunoglu.kLib.extensions.isIntegerNumber
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.arena.Arena
import com.zorbeytorunoglu.tdm.arena.ArenaStatus
import com.zorbeytorunoglu.tdm.game.GameMap
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.lang.StringBuilder

class TDMCmd(val plugin: TDM): CommandExecutor {

    init {
        plugin.getCommand("tdm")!!.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (command.name != "tdm") return false

        if (args[0] == "create") {

            if (!isPlayer(sender)) return false

            if (!hasPermission(sender, "tdm.create")) return false

            if (args.size != 2) {
                sender.sendMessage(plugin.messages.createUsage)
                return false
            }

            val arenaName = args[1]

            if (!arenaName.isAlphanumeric) {
                sender.sendMessage(plugin.messages.invalidArenaName)
                return false
            }

            if (arenaName.length > 20) {
                sender.sendMessage(plugin.messages.arenaNameTooLong)
                return false
            }

            if (plugin.spawn == null) {
                sender.sendMessage(plugin.messages.spawnNotSet)
                return false
            }

            val world = plugin.worldManager.createEmptyWorld(arenaName, World.Environment.NORMAL)

            if (world == null) {
                sender.sendMessage(plugin.messages.mapExists.replace("%map%", arenaName))
                return false
            }

            world.isAutoSave = true
            world.getBlockAt(0,75,0).type = Material.STONE

            sender.sendMessage(plugin.messages.arenaCreated.replace("%map%", arenaName))

            plugin.arenaManager.arenas[arenaName] = Arena(arenaName)

            val player = sender as Player

            player.teleport(Location(world, 0.0, 76.0, 0.0),PlayerTeleportEvent.TeleportCause.PLUGIN)

            player.gameMode = GameMode.CREATIVE
            player.allowFlight = true
            player.isFlying = true

            return true

        }

        if (args[0] == "setspawn") {

            if (!hasPermission(sender, "tdm.setspawn")) return false

            if (!isPlayer(sender)) return false

            val player = sender as Player

            plugin.spawn = player.location

            plugin.spawnResource.set("spawn", player.location)
            plugin.spawnResource.save()

            player.sendMessage(plugin.messages.spawnSet)

            return false

        }

        if (args[0] == "save") {

            if (!hasPermission(sender, "tdm.save")) return false

            if (args.size != 2) {
                sender.sendMessage(plugin.messages.saveUsage)
                return false
            }

            val arenaName = args[1]

            if (!plugin.arenaManager.arenaExists(arenaName)) {
                sender.sendMessage(plugin.messages.arenaNotFound)
                return false
            }

            val arena = plugin.arenaManager.getArena(arenaName)

            if (!arena.isSetup()) {
                sender.sendMessage(plugin.messages.arenaNotSetup)
                return false
            }

            return if (plugin.worldManager.saveMap(arenaName)) {
                sender.sendMessage(plugin.messages.mapSaved.replace("%map%", arenaName))
                plugin.arenaManager.saveMapConfig(arena)
                plugin.arenaManager.gameMaps[arena] = GameMap(arena)
                true
            } else {
                sender.sendMessage(plugin.messages.mapNotSaved.replace("%map%", arenaName))
                false
            }

        }

        if (args[0] == "setup") {

            if (!hasPermission(sender, "tdm.setup")) return false

            if (args.size != 2) {
                sender.sendMessage(plugin.messages.setupUsage)
                return false
            }

            val arenaName = args[1]

            if (!plugin.arenaManager.arenaExists(arenaName)) {
                sender.sendMessage(plugin.messages.arenaNotFound)
                return false
            }

            val arena = plugin.arenaManager.getArena(arenaName)

            sender.sendMessage(plugin.messages.setup.replace("%map%", arenaName))
            sender.sendMessage(plugin.messages.setupDisplayName.replace("%x%", arena.displayName).replace("%map%", arenaName))
            sender.sendMessage(plugin.messages.setupRedSpawn
                .replace("%map%", arenaName)
                .replace("%x%", if (arena.redSpawn == null) plugin.messages.notSetMsg else plugin.messages.setMsg))
            sender.sendMessage(plugin.messages.setupBlueSpawn
                .replace("%map%", arenaName)
                .replace("%x%", if (arena.blueSpawn == null) plugin.messages.notSetMsg else plugin.messages.setMsg))
            sender.sendMessage(plugin.messages.setupLobby
                .replace("%map%", arenaName)
                .replace("%x%", if (arena.lobby == null) plugin.messages.notSetMsg else plugin.messages.setMsg))
            sender.sendMessage(plugin.messages.setupSpectatorSpawn
                .replace("%map%", arenaName)
                .replace("%x%", if (arena.spectatorSpawn == null) plugin.messages.notSetMsg else plugin.messages.setMsg))
            sender.sendMessage(plugin.messages.setupMaxPlayers
                .replace("%map%", arenaName)
                .replace("%x%", "${arena.maxPlayers}"))
            sender.sendMessage(plugin.messages.setupMinPlayers
                .replace("%map%", arenaName)
                .replace("%x%", "${arena.minPlayers}"))
            sender.sendMessage(plugin.messages.setupRedGate
                .replace("%map%", arenaName)
                .replace("%x%", if (arena.redGate.isEmpty()) plugin.messages.notSetMsg else plugin.messages.setMsg))
            sender.sendMessage(plugin.messages.setupBlueGate
                .replace("%map%", arenaName)
                .replace("%x%", if (arena.blueGate.isEmpty()) plugin.messages.notSetMsg else plugin.messages.setMsg))
            sender.sendMessage(plugin.messages.setupTime
                .replace("%map%", arenaName)
                .replace("%x%", "${arena.time}"))
            sender.sendMessage(plugin.messages.setupPlayerLives
                .replace("%map%", arenaName)
                .replace("%x%", "${arena.playerLives}"))

            return true

        }

        if (args[0] == "set") {

            if (!hasPermission(sender, "tdm.set")) return false

            if (args[1] == "displayname") {

                if (args.size < 4) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                val arena = plugin.arenaManager.getArena(arenaName)

                val sb = StringBuilder()

                for (i in 3 until args.size) {

                    if (args[i] == args[args.size-1]) {
                        sb.append(args[i])
                    } else {
                        sb.append(args[i]+" ")
                    }

                }

                arena.displayName = sb.toString()

                sender.sendMessage(plugin.messages.setDisplayName.replace("%map%", arenaName)
                    .replace("%x%", sb.toString()))

                return true

            }

            else if (args[1] == "redspawn") {

                if (!isPlayer(sender)) return false

                if (args.size != 3) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val player = sender as Player

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                val arena = plugin.arenaManager.getArena(arenaName)

                arena.redSpawn = player.location

                sender.sendMessage(plugin.messages.setRedSpawn.replace("%map%", arenaName))

                return true

            }

            else if (args[1] == "bluespawn") {

                if (!isPlayer(sender)) return false

                if (args.size != 3) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val player = sender as Player

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                val arena = plugin.arenaManager.getArena(arenaName)

                arena.blueSpawn = player.location

                sender.sendMessage(plugin.messages.setBlueSpawn.replace("%map%", arenaName))

                return true

            }

            else if (args[1] == "lobby") {

                if (!isPlayer(sender)) return false

                if (args.size != 3) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                val player = sender as Player

                plugin.arenaManager.getArena(arenaName).lobby = player.location

                sender.sendMessage(plugin.messages.setLobbySpawn.replace("%map%", arenaName))

                return true

            }

            else if (args[1] == "spectator") {

                if (!isPlayer(sender)) return false

                if (args.size != 3) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                val player = sender as Player

                plugin.arenaManager.getArena(arenaName).spectatorSpawn = player.location

                sender.sendMessage(plugin.messages.setSpectatorSpawn.replace("%map%", arenaName))

                return true

            }

            else if (args[1] == "maxplayers") {

                if (args.size != 4) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                if (!args[3].isIntegerNumber) {
                    sender.sendMessage(plugin.messages.invalidNumber)
                    return false
                }

                plugin.arenaManager.getArena(arenaName).maxPlayers = args[3].toInt()

                sender.sendMessage(plugin.messages.setMaxPlayers)

                return true

            }

            else if (args[1] == "minplayers") {

                if (args.size != 4) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                if (!args[3].isIntegerNumber) {
                    sender.sendMessage(plugin.messages.invalidNumber)
                    return false
                }

                plugin.arenaManager.getArena(arenaName).minPlayers = args[3].toInt()

                sender.sendMessage(plugin.messages.setMinPlayers)

                return true

            }

            else if (args[1] == "redgate") {

                if (!isPlayer(sender)) return false

                if (args.size != 3) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                sender.sendMessage(plugin.messages.setRedGate)

                //TODO: Red Gate & Blue Gate editors, listener

                return true

            }

            else if (args[1] == "bluegate") {

                if (!isPlayer(sender)) return false

                if (args.size != 3) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                sender.sendMessage(plugin.messages.setBlueGate)

                //TODO: Red Gate & Blue Gate editors, listener

                return true

            }

            else if (args[1] == "time") {

                if (args.size != 4) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                if (!args[3].isIntegerNumber) {
                    sender.sendMessage(plugin.messages.invalidNumber)
                    return false
                }

                plugin.arenaManager.getArena(arenaName).time = args[3].toInt()

                sender.sendMessage(plugin.messages.setTime.replace("%map%", arenaName))

                return true

            }

            else if (args[1] == "lives") {

                if (args.size != 4) {
                    sender.sendMessage(plugin.messages.setIncorrectUsage)
                    return false
                }

                val arenaName = args[2]

                if (!arenaExists(sender, arenaName)) return false

                if (!args[3].isIntegerNumber) {
                    sender.sendMessage(plugin.messages.invalidNumber)
                    return false
                }

                plugin.arenaManager.getArena(arenaName).playerLives = args[3].toInt()

                sender.sendMessage(plugin.messages.setPlayerLives.replace("%map%", arenaName))

                return true

            }

            else {
                sender.sendMessage(plugin.messages.setIncorrectUsage)
                return false
            }

        }

        if (args[0] == "tp") {

            if (!hasPermission(sender, "tdm.tp")) return false

            if (!isPlayer(sender)) return false

            if (args.size != 2) {
                sender.sendMessage(plugin.messages.tpUsage)
                return false
            }

            val mapName = args[1]

            val world = plugin.server.getWorld(mapName)

            if (world == null) {
                sender.sendMessage(plugin.messages.mapNotFound)
                return false
            }

            val player = sender as Player

            player.teleport(world.spawnLocation)

            player.sendMessage(plugin.messages.teleportedMap.replace("%map%", mapName))

            return true

        }

        if (args[0] == "arenas" || args[0] == "maps") {

            if (!hasPermission(sender, "tdm.arenas")) return false

            if (plugin.arenaManager.arenas.isEmpty()) {
                sender.sendMessage(plugin.messages.noArenaFound)
                return false
            }

            sender.sendMessage(plugin.messages.arenas)

            for (arena in plugin.arenaManager.arenas.values) {

                if (!arena.isSetup()) {
                    sender.sendMessage(plugin.messages.perArena
                        .replace("%map%", arena.name)
                        .replace("%status%", plugin.messages.perArenaNoSetup))
                } else {

                    val status = plugin.arenaManager.gameMaps[arena]!!.status

                    val message = plugin.messages.perArenaReady.replace("%map%", arena.name)

                    when (status) {
                        ArenaStatus.WAITING -> {
                            sender.sendMessage(message.replace("%status%", plugin.messages.perArenaReady))
                        }
                        ArenaStatus.NOT_SETUP -> {
                            sender.sendMessage(message.replace("%status%", plugin.messages.perArenaNoSetup))
                        }
                        ArenaStatus.IN_GAME -> {
                            sender.sendMessage(message.replace("%status%", plugin.messages.perArenaInGame))
                        }
                        ArenaStatus.RELOADING -> {
                            sender.sendMessage(message.replace("%status%", plugin.messages.perArenaReloading))
                        }
                    }

                }

            }

            return true

        }

        if (args[0] == "refresh") {

            if (!hasPermission(sender, "tdm.refresh")) return false

        }

        return false

    }

    private fun hasPermission(sender: CommandSender, permission: String): Boolean {
        return if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.messages.noPerm)
            false
        } else {
            true
        }
    }

    private fun isPlayer(sender: CommandSender): Boolean {
        return if (sender !is Player) {
            sender.sendMessage(plugin.messages.onlyPlayers)
            false
        } else {
            true
        }
    }

    private fun arenaExists(sender: CommandSender, arenaName: String): Boolean {
        return if (plugin.arenaManager.arenas.containsKey(arenaName)) {
            true
        } else {
            sender.sendMessage(plugin.messages.arenaNotFound)
            false
        }
    }

}