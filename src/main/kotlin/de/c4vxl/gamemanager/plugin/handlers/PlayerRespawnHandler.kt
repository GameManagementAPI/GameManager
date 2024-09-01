package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerRespawnEvent
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.Plugin

class PlayerRespawnHandler(plugin: Plugin): Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player.asGamePlayer
        val game = player.game ?: return
        val team = player.team ?: return
        val spawnLocation: Location = game.worldManager.mapConfig.getTeamSpawn(team.id) ?: game.worldManager.world?.spawnLocation ?: return

        val cevent = GamePlayerRespawnEvent(player, game, spawnLocation)
        cevent.callEvent()
        event.respawnLocation = cevent.respawnLocation
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player.asGamePlayer
        val game = player.game ?: return

        event.keepInventory = false
        event.keepLevel = true
        event.deathMessage(null)
    }
}