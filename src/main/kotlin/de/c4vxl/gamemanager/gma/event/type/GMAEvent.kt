package de.c4vxl.gamemanager.gma.event.type

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Base class of gma-specific events
 */
open class GMAEvent : Event() {
    companion object {
        private val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    /**
     * If set to {@code true} the event will be reverted
     */
    var isCancelled: Boolean = false

    override fun getHandlers(): HandlerList = HANDLER_LIST
}