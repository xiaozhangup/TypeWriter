package me.gabber235.typewriter.entry.dialogue

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import lirand.api.extensions.events.listen
import me.gabber235.typewriter.adapters.AdapterLoader
import me.gabber235.typewriter.adapters.MessengerData
import me.gabber235.typewriter.adapters.MessengerFilter
import me.gabber235.typewriter.entry.Modifier
import me.gabber235.typewriter.entry.entries.DialogueEntry
import me.gabber235.typewriter.interaction.chatHistory
import me.gabber235.typewriter.plugin
import me.gabber235.typewriter.utils.blackScreen
import me.gabber235.typewriter.utils.resetScreen
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.abs
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

class MessengerFinder : KoinComponent {
    private val adapterLoader: AdapterLoader by inject()

    private var messengers = emptyMap<MessengerData, MessengerFilter>()

    fun initialize() {
        messengers = adapterLoader.adapters.flatMap { it.messengers }.associateBy({ it }, ::instantiateFilter)
    }

    private fun instantiateFilter(data: MessengerData): MessengerFilter {
        return if (data.filter.kotlin.isCompanion) {
            data.filter.kotlin.objectInstance as MessengerFilter
        } else {
            data.filter.kotlin.createInstance()
        }
    }

    fun findMessenger(player: Player, entry: DialogueEntry): DialogueMessenger<out DialogueEntry> {
        val messenger =
            messengers
                .filter { it.key.dialogue.isInstance(entry) }
                .filter { it.value.filter(player, entry) }
                .maxByOrNull { it.key.priority }?.key?.messenger
                ?: return EmptyDialogueMessenger(player, entry)

        return messenger.kotlin.primaryConstructor!!.call(player, entry)
    }
}

enum class MessengerState {
    RUNNING,
    FINISHED,
    CANCELLED,
}

open class DialogueMessenger<DE : DialogueEntry>(val player: Player, val entry: DE) {

    protected val listener: Listener = object : Listener {}

    open var state: MessengerState = MessengerState.RUNNING
        protected set

    open fun init() {
        player.blackScreen()
        val start = player.location.clone()
        object : BukkitRunnable() {
            override fun run() {
                if (state != MessengerState.RUNNING) cancel()
                if (!player.isOnline) cancel()

                if (
                    (start.world != player.world) ||
                    (start.distance(player.location) > 2) ||
                    (abs(start.yaw - player.location.yaw) > 45)
                    ) {
                    end()
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0, 2)
    }

    open fun tick(cycle: Int) {}

    open fun dispose() {
        HandlerList.unregisterAll(listener)
    }

    open fun end() {
        state = MessengerState.FINISHED
        player.chatHistory.resendMessages(player)

        // Resend the chat history again after a delay to make sure that the dialogue chat is fully cleared
        plugin.launch {
            delay(1.ticks)
            player.chatHistory.resendMessages(player)
        }
        player.resetScreen()
    }

    open val triggers: List<String>
        get() = entry.triggers

    open val modifiers: List<Modifier>
        get() = entry.modifiers

    protected inline fun <reified E : Event> listen(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        noinline block: (event: E) -> Unit,
    ) {
        plugin.listen(listener, priority, ignoreCancelled, block)
    }
}

class EmptyDialogueMessenger(player: Player, entry: DialogueEntry) : DialogueMessenger<DialogueEntry>(player, entry) {
    override fun init() {
        state = MessengerState.FINISHED
    }
}