package me.gabber235.typewriter.entries.action

import com.github.shynixn.mccoroutine.bukkit.launch
import lirand.api.extensions.inventory.meta
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Colored
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.MultiLine
import me.gabber235.typewriter.adapters.modifiers.Placeholder
import me.gabber235.typewriter.entry.Criteria
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.entries.ActionEntry
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.utils.Icons
import me.gabber235.typewriter.utils.asMini
import me.xiaozhangup.capybara.items.ItemSaveCommand
import me.xiaozhangup.capybara.items.ItemSaveCommand.cachedItemStack
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

@Entry("drop_cached_item", "Drop an cached item at location, or on player", Colors.RED, Icons.DROPBOX)
/**
 * The `Drop Item Action` is an action that drops an item in the world.
 * This action provides you with the ability to drop an item with a specified Minecraft material, amount, display name, lore, and location.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations.
 * You can use it to create treasure chests with randomized items, drop loot from defeated enemies, or spawn custom items in the world.
 * The possibilities are endless!
 */
class DropCachedItemActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria>,
    override val modifiers: List<Modifier>,
    override val triggers: List<String> = emptyList(),
    @Help("The cached item's id to drop.")
    private val cachedId: String = "",
    @Help("The amount of items to drop.")
    private val amount: Int = 1,
    @Help("The location to drop the item. (Defaults to the player's location)")
    // The location to drop the item at. If this field is left blank, the item will be dropped at the location of the player triggering the action.
    private val location: Optional<Location> = Optional.empty(),
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)
        val item = cachedItemStack(cachedId) ?: return

        item.amount = amount
        // Run on main thread
        plugin.launch {
            if (location.isPresent) {
                location.get().world.dropItem(location.get(), item)
            } else {
                player.location.world.dropItem(player.location, item)
            }
        }
    }
}