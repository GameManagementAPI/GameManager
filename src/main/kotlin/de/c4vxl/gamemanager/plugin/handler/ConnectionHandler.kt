package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Responsible for handling bukkitPlayer connections
 */
class ConnectionHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        // Quit game
        event.player.gma.quit()

        // Unregister player cache
        GMAPlayer.unregister(event.player)
    }
}