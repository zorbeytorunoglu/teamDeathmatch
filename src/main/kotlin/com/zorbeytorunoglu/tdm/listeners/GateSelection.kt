package com.zorbeytorunoglu.tdm.listeners

import com.zorbeytorunoglu.kLib.extensions.registerEvents
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.arena.Arena
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class GateSelection(private val plugin: TDM): Listener {

    init {
        plugin.registerEvents(this)
    }

    companion object {
        val selectionMode = HashMap<String, Arena>()
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {

        if (!selectionMode.containsKey(event.player.uniqueId.toString())) return

        val arena = selectionMode[event.player.uniqueId.toString()]!!

        if (event.player.location.world!!.name != arena.name) return

        arena.gates.add(event.block.location)

        event.player.sendMessage(plugin.messages.gateBlockAdded.replace("%map%", arena.name))

        event.isCancelled = true

    }

}