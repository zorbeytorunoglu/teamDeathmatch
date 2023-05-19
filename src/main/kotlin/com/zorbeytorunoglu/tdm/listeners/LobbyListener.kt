package com.zorbeytorunoglu.tdm.listeners

import com.zorbeytorunoglu.kLib.extensions.registerEvents
import com.zorbeytorunoglu.tdm.TDM
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent

class LobbyListener(private val plugin: TDM): Listener {

    init {

        plugin.registerEvents(this)

    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {

        if (plugin.gameManager.playerInAnyLobby(event.player)) {

            val args = event.message.split(" ")

            var whitelisted = false

            plugin.config.lobbyCommandWhitelist.forEach {
                if (it.startsWith(args[0])) whitelisted = true
            }

            if (!whitelisted) event.isCancelled = true

            event.player.sendMessage(plugin.messages.lobbyCommand)

        }

    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {

        if (plugin.gameManager.playerInAnyLobby(event.player)) event.isCancelled = true

    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {

        if (plugin.gameManager.playerInAnyLobby(event.player)) event.isCancelled = true

    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {

        if (plugin.gameManager.playerInAnyLobby(event.player)) event.isCancelled = true

    }

    @EventHandler
    fun onHit(event: EntityDamageByEntityEvent) {

        if (event.damager !is Player) return

        if (plugin.gameManager.playerInAnyLobby(event.damager as Player)) event.isCancelled = true

    }

}