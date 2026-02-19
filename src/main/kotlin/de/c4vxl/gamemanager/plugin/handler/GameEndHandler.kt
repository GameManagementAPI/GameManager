package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Responsible for properly unregistering games after they stopped
 */
class GameEndHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onStop(event: GameStopEvent) {
        GMA.unregisterGame(event.game, false)
    }
}