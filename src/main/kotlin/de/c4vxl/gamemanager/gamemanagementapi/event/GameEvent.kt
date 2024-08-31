package de.c4vxl.gamemanager.gamemanagementapi.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class GameEvent: Event() {
    companion object {
        private val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    var isCancelled: Boolean = false
    override fun getHandlers(): HandlerList = HANDLER_LIST
}