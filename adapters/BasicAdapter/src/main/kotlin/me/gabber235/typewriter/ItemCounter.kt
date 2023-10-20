package me.gabber235.typewriter

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun Player.checkItem(item: ItemStack, amount: Int = 1, remove: Boolean = false): Boolean {
    if (item.isAir()) {
        error("air")
    }
    return inventory.checkItem(item, amount, remove)
}

fun Inventory.checkItem(item: ItemStack, amount: Int = 1, remove: Boolean = false): Boolean {
    if (item.isAir()) {
        error("air")
    }
    return hasItem(amount) { it.isSimilar(item) } && (!remove || takeItem(amount) { it.isSimilar(item) })
}

fun Inventory.hasItem(amount: Int = 1, matcher: (itemStack: ItemStack) -> Boolean): Boolean {
    var checkAmount = amount
    contents.forEach { itemStack ->
        if (itemStack.isNotAir() && matcher(itemStack)) {
            checkAmount -= itemStack.amount
            if (checkAmount <= 0) {
                return true
            }
        }
    }
    return false
}

fun Inventory.takeItem(amount: Int = 1, matcher: (itemStack: ItemStack) -> Boolean): Boolean {
    var takeAmount = amount
    contents.forEachIndexed { index, itemStack ->
        if (itemStack.isNotAir() && matcher(itemStack)) {
            takeAmount -= itemStack.amount
            if (takeAmount < 0) {
                itemStack.amount = itemStack.amount - (takeAmount + itemStack.amount)
                return true
            } else {
                setItem(index, null)
                if (takeAmount == 0) {
                    return true
                }
            }
        }
    }
    return false
}

fun Inventory.countItem(matcher: (itemStack: ItemStack) -> Boolean): Int {
    var amount = 0
    contents.forEach { itemStack ->
        if (itemStack.isNotAir() && matcher(itemStack)) {
            amount += itemStack.amount
        }
    }
    return amount
}

@OptIn(ExperimentalContracts::class)
fun ItemStack?.isAir(): Boolean {
    contract { returns(false) implies (this@isAir != null) }
    return this == null || type == Material.AIR || type.name.endsWith("_AIR")
}

@OptIn(ExperimentalContracts::class)
fun ItemStack?.isNotAir(): Boolean {
    contract { returns(true) implies (this@isNotAir != null) }
    return !isAir()
}