package de.c4vxl.gamemanager.plugin.handler

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.event.player.GamePlayerDeathEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerRespawnEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerReviveEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles proper respawning on death of players
 */
class RespawnHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, GameManager.instance)
    }

    private val lastKillers = ConcurrentHashMap<UUID, UUID?>()

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player.gma
        val game = player.game ?: return

        // Cache killer
        lastKillers[event.entity.uniqueId] = event.entity.killer?.uniqueId

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

        if (!player.isInGame)
            return

        // Get respawn location
        val spawn = player.team?.let { game.worldManager.map?.getSpawnLocation(it.id) } ?:
            game.worldManager.map?.world?.spawnLocation
            ?: return

        event.respawnLocation = spawn
    }

    @EventHandler
    fun onRespawned(event: PlayerPostRespawnEvent) {
        val player = event.player.gma
        val game = player.game ?: return

        if (!player.isInGame)
            return

        // Get killer from cache
        val killer = lastKillers.remove(event.player.uniqueId)?.let { Bukkit.getPlayer(it) }?.gma

        // Trigger event
        GamePlayerRespawnEvent(player, game, killer, event.respawnLocation, event)
            .callEvent()
    }

    @EventHandler
    fun onRevive(event: GamePlayerReviveEvent) {
        // Reset player
        event.player.reset()

        // Teleport to spawn
        event.player.team?.let {
            val spawn = event.game.worldManager.map?.getSpawnLocation(it.id) ?: return@let
            event.player.bukkitPlayer.teleport(spawn)
        }
    }
}