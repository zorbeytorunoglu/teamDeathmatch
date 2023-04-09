package com.zorbeytorunoglu.tdm.inventory

import com.zorbeytorunoglu.kLib.configuration.Resource
import com.zorbeytorunoglu.tdm.TDM
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class KitManager(val plugin: TDM) {

    val playerKits = HashMap<String, Kit>()

    fun inventoryToKit(playerInventory: PlayerInventory): Kit {

        val contents = mutableListOf<ItemStack>()

        if (playerInventory.storageContents.isNotEmpty()) {

            playerInventory.storageContents.forEach {
                if (it != null) {
                    if (it.type != Material.AIR) {
                        contents.add(it)
                    }
                }
            }

        }

        val armors = mutableListOf<ItemStack>()

        if (playerInventory.armorContents.isNotEmpty()) {

            playerInventory.armorContents.forEach {
                if (it != null) {
                    if (it.type != Material.AIR) {
                        armors.add(it)
                    }
                }
            }
        }

        val kit = Kit(contents, armors)

        if (playerInventory.itemInOffHand.type != Material.AIR) {
            kit.offHand = playerInventory.itemInOffHand
        }

        return kit

    }

    fun saveKit(kit: Kit, path: String, resource: Resource) {

        if (kit.contents.isNotEmpty()) {

            var slot = 0

            for (item in kit.contents) {
                if (validItem(item)) {
                    resource.set("$path.contents.$slot", item)
                    slot++
                }
            }

        }

        if (kit.armors.isNotEmpty()) {

            var slot = 0

            for (item in kit.armors) {
                if (validItem(item)) {
                    resource.set("$path.armors.$slot", item)
                    slot++
                }
            }

        }

        if (validItem(kit.offHand)) {
            resource.set("$path.offhand", kit.offHand)
        }

        if (validItem(kit.helmet)) {
            resource.set("$path.helmet", kit.helmet)
        }

        resource.save()

    }

    fun loadKit(path: String, resource: Resource): Kit {

        val contents = mutableListOf<ItemStack>()
        val armors = mutableListOf<ItemStack>()

        val contentsSection: ConfigurationSection? = resource.getConfigurationSection("$path.contents")

        if (contentsSection != null) {

            if (contentsSection.getKeys(false).isNotEmpty()) {
                for (key in contentsSection.getKeys(false)) {
                    if (resource.isItemStack("$path.contents.$key")) {
                        contents.add(resource.getItemStack("$path.contents.$key")!!)
                    }
                }
            }

        }

        val armorsSection: ConfigurationSection? = resource.getConfigurationSection("$path.armors")

        if (armorsSection != null) {

            if (armorsSection.getKeys(false).isNotEmpty()) {
                for (key in armorsSection.getKeys(false)) {
                    if (resource.isItemStack("$path.armors.$key")) {
                        armors.add(resource.getItemStack("$path.armors.$key")!!)
                    }
                }
            }

        }

        val kit = Kit(contents, armors)

        if (resource.isSet("$path.offhand")) {
            if (resource.isItemStack("$path.offhand")) {
                kit.offHand = resource.getItemStack("$path.offhand")
            }
        }

        if (resource.isSet("$path.helmet")) {
            if (resource.isItemStack("$path.helmet")) {
                kit.helmet = resource.getItemStack("$path.helmet")
            }
        }

        return kit

    }

    fun giveKit(player: Player, kit: Kit) {

        player.inventory.clear()

        if (kit.contents.isNotEmpty()) {
            player.inventory.storageContents = kit.contents
        }

        if (kit.armors.isNotEmpty()) {
            player.inventory.setArmorContents(kit.armors)
        }

        if (validItem(kit.offHand)) {
            player.inventory.setItemInOffHand(kit.offHand)
        }

        if (validItem(kit.helmet)) {
            player.inventory.helmet = kit.helmet
        }

        player.updateInventory()

    }

    private fun validItem(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        return itemStack.type != Material.AIR
    }

    fun isKitEmpty(kit: Kit): Boolean {

        return kit.contents.isEmpty() && kit.armors.isEmpty() && kit.offHand == null && kit.helmet == null

    }

}