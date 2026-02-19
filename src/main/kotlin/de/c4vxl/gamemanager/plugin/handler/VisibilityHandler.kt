package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Handles player visibility
 * Stops players of different games from seeing each other
 */
object VisibilityHandler {
    init {
        Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            Bukkit.getOnlinePlayers().forEach { handle(it) }
        }, 0, 10)
    }

    /**
     * Handles visibility for one player
     */
    private fun handle(self: Player) {
        Bukkit.getOnlinePlayers().forEach { other ->
            // Different game
            // always hide both ways
            if (other.gma.game != self.gma.game) {
                other.hidePlayer(Main.instance, self)
                self.hidePlayer(Main.instance, other)
                return@forEach
            }

            // TODO: Handle custom visibility for spectators

            // Same game
            other.showPlayer(Main.instance, self)
            self.showPlayer(Main.instance, other)
        }
    }

    // TODO: Handle chat messages
}