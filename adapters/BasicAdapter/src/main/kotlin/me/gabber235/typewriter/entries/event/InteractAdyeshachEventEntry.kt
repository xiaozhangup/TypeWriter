package me.gabber235.typewriter.entries.event

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.entity.manager.ManagerType
import ink.ptms.adyeshach.core.event.AdyeshachEntityInteractEvent
import me.gabber235.typewriter.adapters.Colors
import me.gabber235.typewriter.adapters.Entry
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.adapters.modifiers.MaterialProperties
import me.gabber235.typewriter.adapters.modifiers.MaterialProperty.BLOCK
import me.gabber235.typewriter.adapters.modifiers.MaterialProperty.ITEM
import me.gabber235.typewriter.entry.EntryListener
import me.gabber235.typewriter.entry.Query
import me.gabber235.typewriter.entry.entries.EventEntry
import me.gabber235.typewriter.entry.startDialogueWithOrNextDialogue
import me.gabber235.typewriter.utils.Icons
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*

@Entry("on_interact_with_adyeshach", "When the player interacts with a Adyeshach npc", Colors.YELLOW, Icons.HAND_POINTER)
/**
 * The `Interact Block Event` is triggered when a player interacts with a block by right-clicking it.
 *
 * ## How could this be used?
 *
 * This could be used to create special interactions with blocks, such as opening a secret door when you right-click a certain block, or a block that requires a key to open.
 */
class InteractAdyeshachEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<String> = emptyList(),
    @MaterialProperties(BLOCK)
    @Help("The npc that was interacted with.")
    val npcId: String = "null",
) : EventEntry

@EntryListener(InteractAdyeshachEventEntry::class)
fun onInteractAdyeshach(event: AdyeshachEntityInteractEvent, query: Query<InteractAdyeshachEventEntry>) {
    query findWhere { entry ->
        val manager = Adyeshach.api().getPublicEntityManager(ManagerType.PERSISTENT)

        manager.getEntities().any { entityInstance -> entityInstance.id == entry.npcId }
    } startDialogueWithOrNextDialogue event.player
}