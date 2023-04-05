package com.zorbeytorunoglu.tdm.utils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.entity.Player

object Utils {

    fun sendTitle(player: Player, fadein: Int, stay: Int, fadeout: Int, titleFirst: String?, subtitleFirst: String?) {
        var title = titleFirst
        var subtitle = subtitleFirst
        if (subtitle != null) {
            subtitle = subtitle.replace("%player%".toRegex(), player.displayName)
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle)
        }
        if (title != null) {
            title = title.replace("%player%".toRegex(), player.displayName)
            title = ChatColor.translateAlternateColorCodes('&', title)
        }
        player.sendTitle(title, subtitle, fadein, stay, fadeout)
    }

    fun sendActionBar(player: Player, msg: String?) {
        var msg = msg
        msg = ChatColor.translateAlternateColorCodes('&', msg)
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *ComponentBuilder(msg).create())
    }

    fun respawnPlayer(player: Player) {
        player.spigot().respawn()
    }

}