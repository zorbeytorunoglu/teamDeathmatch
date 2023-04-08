package com.zorbeytorunoglu.tdm.inventory

import org.bukkit.inventory.ItemStack

class Kit(contentList: MutableList<ItemStack>, armorList: MutableList<ItemStack>) {

    var contents: Array<ItemStack> = contentList.toTypedArray()
    var armors: Array<ItemStack> = armorList.toTypedArray()
    var offHand: ItemStack? = null
    var helmet: ItemStack? = null

}