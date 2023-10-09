package me.gabber235.typewriter

import App
import lirand.api.extensions.server.server
import me.gabber235.typewriter.adapters.Adapter
import me.gabber235.typewriter.adapters.TypewriteAdapter
import me.gabber235.typewriter.utils.Baffle
import java.util.concurrent.TimeUnit

@Adapter("Basic", "For all the most basic entries", App.VERSION)
/**
 * The Basic Adapter contains all the essential entries for Typewriter.
 * In most cases, it should be installed with Typewriter.
 * If you haven't installed Typewriter or the adapter yet,
 * please follow the [Installation Guide](/docs/Installation-Guide)
 * first.
 */
object BasicAdapter : TypewriteAdapter() {
    val baffle = Baffle(100)

    override fun initialize() {
        if (!server.pluginManager.isPluginEnabled("Adyeshach")) {
            logger.warning("Adyeshach plugin not found!")
            return
        }
    }
}