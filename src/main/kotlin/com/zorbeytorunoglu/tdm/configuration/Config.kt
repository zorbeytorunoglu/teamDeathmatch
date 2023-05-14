package com.zorbeytorunoglu.tdm.configuration

import com.zorbeytorunoglu.kLib.configuration.Resource
import org.bukkit.Sound

class Config(config: Resource) {

    val countdownSound: Sound? = try {
        Sound.valueOf(config.getString("countdownSound")!!)
    } catch (e: Exception) {
        null
    }

    val winCommands: List<String> = config.getStringList("winCommands")

}