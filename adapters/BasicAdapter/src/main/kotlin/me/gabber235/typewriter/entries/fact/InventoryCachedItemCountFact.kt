package me.gabber235.typewriter.entries.fact

import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.countItem
import me.gabber235.typewriter.entry.entries.ReadableFactEntry
import me.gabber235.typewriter.facts.Fact
import me.gabber235.typewriter.utils.Icons
import me.gabber235.typewriter.utils.asMini
import me.xiaozhangup.capybara.book.builder.StackBook.openIndex
import me.xiaozhangup.capybara.items.ItemSaveCommand.cachedItemStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

@Entry(
    "inventory_cached_item_count_fact",
    "The amount of a specific item in the player's inventory",
    Colors.PURPLE,
    Icons.BAG_SHOPPING
)
/**
 * The `Inventory Item Count Fact` is a fact that returns the amount of a specific item in the player's inventory.
 *
 * <fields.ReadonlyFactInfo />
 *
 * ## How could this be used?
 *
 * This could be used to check if the player has a specific item in their inventory, or to check if they have a specific amount of an item.
 * Like giving the player a quest to collect 10 apples, and then checking if they have 10 apples in their inventory.
 */
class InventoryCachedItemCountFact(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    @Help("The cached id of the item.")
    private val cacheId: String = "",
) : ReadableFactEntry {
    private fun isValid(item: ItemStack): Boolean {
        if (cacheId.isNotBlank()) {
            if (!item.isSimilar(cachedItemStack(cacheId))) return false
        }
        return true
    }

    override fun read(playerId: UUID): Fact {
        val player = server.getPlayer(playerId) ?: return Fact(id, 0)
        val amount = player.inventory.countItem { itemStack -> itemStack.isSimilar(cachedItemStack(id)) }
        return Fact(id, amount)
    }
}
