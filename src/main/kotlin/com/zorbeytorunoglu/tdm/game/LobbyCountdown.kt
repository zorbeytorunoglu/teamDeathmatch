package com.zorbeytorunoglu.tdm.game

import com.zorbeytorunoglu.kLib.extensions.colorHex
import com.zorbeytorunoglu.tdm.TDM
import com.zorbeytorunoglu.tdm.utils.Utils
import org.bukkit.scheduler.BukkitRunnable

class LobbyCountdown(private val plugin: TDM, private val gameMap: GameMap): BukkitRunnable() {

    private var countdown: Int = plugin.config.lobbyCountdown

    override fun run() {

        if (!gameMap.hasMinPlayers()) {
            cancel()
            return
        }

        if (countdown <= 0) {

            plugin.gameManager.startGame(gameMap)
            cancel()
            return

        } else {

            gameMap.lobbyPlayers.forEach {

                if (plugin.cacheManager.getPlayer(it) != null) {

                    val player = plugin.cacheManager.getPlayer(it)!!

                    Utils.sendActionBar(player, plugin.messages.lobbyCountdown.replace("%seconds%", "$countdown"))

                }

            }

            countdown--

        }

    }


}