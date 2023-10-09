package me.gabber235.typewriter.utils

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

fun Player.blackScreen() {
    addPotionEffect(
        PotionEffect(PotionEffectType.BLINDNESS, 200000, 1, false, false, false)
    )
    addPotionEffect(
        PotionEffect(PotionEffectType.SLOW, 200000, 1, false, false, false)
    )
}

fun Player.resetScreen() {
    removePotionEffect(PotionEffectType.BLINDNESS)
    removePotionEffect(PotionEffectType.SLOW)
}