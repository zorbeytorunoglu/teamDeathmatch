package com.zorbeytorunoglu.tdm.configuration.messages

import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.kLib.extensions.colorHex

class Messages(config: Resource) {

    val noPerm = config.getString("no-perm")!!.colorHex
    val createUsage = config.getString("create-usage")!!.colorHex
    val invalidArenaName = config.getString("invalid-arena-name")!!.colorHex
    val arenaNameTooLong = config.getString("too-long-arena-name")!!.colorHex
    val spawnNotSet = config.getString("spawn-not-set")!!.colorHex
    val mapExists = config.getString("map-exists")!!.colorHex
    val arenaCreated = config.getString("arena-created")!!.colorHex
    val onlyPlayers = config.getString("only-players")!!.colorHex
    val spawnSet = config.getString("spawn-set")!!.colorHex
    val saveUsage = config.getString("save-usage")!!.colorHex
    val arenaNotFound = config.getString("arena-not-found")!!.colorHex
    val mapSaved = config.getString("map-saved")!!.colorHex
    val mapNotSaved = config.getString("map-not-saved")!!.colorHex
    val setupUsage = config.getString("setup-usage")!!.colorHex
    val setup = config.getString("setup")!!.colorHex
    val setupDisplayName = config.getString("setup-displayName")!!.colorHex
    val setupRedSpawn = config.getString("setup-redSpawn")!!.colorHex
    val setupBlueSpawn = config.getString("setup-blueSpawn")!!.colorHex
    val setupLobby = config.getString("setup-lobby")!!.colorHex
    val setupSpectatorSpawn = config.getString("setup-spectatorSpawn")!!.colorHex
    val setupMaxPlayers = config.getString("setup-maxPlayers")!!.colorHex
    val setupMinPlayers = config.getString("setup-minPlayers")!!.colorHex
    val setupRedGate = config.getString("setup-redGate")!!.colorHex
    val setupBlueGate = config.getString("setup-blueGate")!!.colorHex
    val setupTime = config.getString("setup-time")!!.colorHex
    val setupPlayerLives = config.getString("setup-playerLives")!!.colorHex
    val setMsg = config.getString("setMsg")!!.colorHex
    val notSetMsg = config.getString("notSetMsg")!!.colorHex
    val setIncorrectUsage = config.getString("set-incorrect-usage")!!.colorHex
    val setDisplayName = config.getString("set-displayName")!!.colorHex
    val setRedSpawn = config.getString("set-redSpawn")!!.colorHex
    val setBlueSpawn = config.getString("set-blueSpawn")!!.colorHex
    val setLobbySpawn = config.getString("set-lobbySpawn")!!.colorHex
    val setSpectatorSpawn = config.getString("set-spectatorSpawn")!!.colorHex
    val setMaxPlayers = config.getString("set-maxPlayers")!!.colorHex
    val setMinPlayers = config.getString("set-minPlayers")!!.colorHex
    val setRedGate = config.getString("set-redGate")!!.colorHex
    val setBlueGate = config.getString("set-blueGate")!!.colorHex
    val setTime = config.getString("set-time")!!.colorHex
    val setPlayerLives = config.getString("set-playerLives")!!.colorHex
    val invalidNumber = config.getString("invalid-number")!!.colorHex
    val tpUsage = config.getString("tp-usage")!!.colorHex
    val mapNotFound = config.getString("map-not-found")!!.colorHex
    val teleportedMap = config.getString("teleported-map")!!.colorHex
    val arenas = config.getString("arenas")!!.colorHex
    val perArena = config.getString("per-arena")!!.colorHex
    val arenaNotSetup = config.getString("arena-not-setup")!!.colorHex
    val perArenaNoSetup = config.getString("per-arena-no-setup")!!.colorHex
    val perArenaReady = config.getString("per-arena-ready")!!.colorHex
    val perArenaInGame = config.getString("per-arena-in-game")!!.colorHex
    val perArenaReloading = config.getString("per-arena-reloading")!!.colorHex
    val noArenaFound = config.getString("no-arena-found")!!.colorHex
    val refreshUsage = config.getString("refresh-usage")!!.colorHex
    val refreshInGame = config.getString("refresh-in-game")!!.colorHex
    val refreshAlready = config.getString("refresh-already")!!.colorHex
    val refreshing = config.getString("refreshing")!!.colorHex
    val refreshed = config.getString("refreshed")!!.colorHex
    val notRefreshed = config.getString("not-refreshed")!!.colorHex
    val arenaBusy = config.getString("arena-busy")!!.colorHex
    val deleteUsage = config.getString("delete-usage")!!.colorHex
    val mapDeleted = config.getString("map-deleted")!!.colorHex

}