package de.c4vxl.gamemanager.gamemanagementapi.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class GameEvent: Event() {
    private companion object val HANDLER_LIST: HandlerList = HandlerList()
    var isCancelled: Boolean = false
    override fun getHandlers(): HandlerList = HANDLER_LIST
}