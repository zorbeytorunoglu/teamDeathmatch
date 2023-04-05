package com.zorbeytorunoglu.tdm.utils

import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.ChunkGenerator

object WorldUtils {

    fun getChunkGenerator(): ChunkGenerator {
        return object : ChunkGenerator() {
            override fun generateChunkData(
                world: World,
                random: java.util.Random,
                chunkX: Int,
                chunkZ: Int,
                chunkGenerator: BiomeGrid
            ): ChunkData {
                val chunkData = Bukkit.getServer().createChunkData(world)
                val min = world.minHeight
                val max = world.maxHeight
                val biome = Biome.valueOf("THE_VOID")
                for (x in 0..15) {
                    for (z in 0..15) {
                        var y = min
                        while (y < max) {
                            chunkGenerator.setBiome(x, y, z, biome)
                            y += 4
                        }
                    }
                }
                return chunkData
            }
        }
    }

    fun setGameRule(world: World, ruleName: String, value: String) {
        // Handle bools
        var valueBool: Boolean? = null
        if (value.equals("true", ignoreCase = true)) valueBool = true else if (value.equals(
                "false",
                ignoreCase = true
            )
        ) valueBool = false
        // Handle ints
        var valueInt: Int? = null
        if (valueBool == null) {
            try {
                valueInt = value.toInt()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        // Apply
        try {
            if (valueBool == null) {
                val gameRule = GameRule.getByName(ruleName) as GameRule<Int>?
                if (gameRule == null || valueInt == null) throw Exception("Invalid GameRule or value provided: $ruleName -> $value")
                world.setGameRule(gameRule, valueInt)
            } else {
                val gameRule = GameRule.getByName(ruleName) as GameRule<Boolean>?
                    ?: throw Exception("Invalid GameRule: $ruleName")
                world.setGameRule(gameRule, valueBool)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}