package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.event.player.GamePlayerDeathEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerRespawnEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent

/**
 * Handles proper respawning on death of players
 */
class RespawnHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player.gma
        val game = player.game ?: return

        // Trigger event
        GamePlayerDeathEvent(player, game, event).let {
            it.callEvent()
            event.isCancelled = it.isCancelled
        }

        // No death message
        event.deathMessage(null)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.gma
        val game = player.game ?: return

        // Get respawn location
        val spawn = player.team?.let { game.worldManager.map?.getSpawnLocation(it.id) } ?:
            game.worldManager.map?.world?.spawnLocation
            ?: return

        // Trigger event
        GamePlayerRespawnEvent(player, game, spawn, event).let {
            it.callEvent()
            event.respawnLocation = it.spawnLocation
        }
    }
}