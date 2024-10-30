package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerJoinEvent
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class QueueHandler(plugin: Plugin) : Listener {
    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            Bukkit.getOnlinePlayers().forEach { player ->
                Bukkit.getScheduler().callSyncMethod(plugin) {
                    PlayerVisibilityHandler.handleVisibility(player.asGamePlayer)
                }

                val game = player.asGamePlayer.game ?: return@forEach
                if (!game.isQueuing) return@forEach

                if (!game.isFull)
                    player.sendActionBar(Component.text("Waiting for other players...")
                        .color(NamedTextColor.WHITE)
                        .append(Component.text(" (${game.players.size}/${game.maxPlayer})")))
            }
        }, 10, 10)

        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onLastPlayerJoin(event: GamePlayerJoinEvent) {
        // check if is last player
        if (event.game.players.size+1 != event.game.maxPlayer) return

        if (event.game.teamAmount == 1) {
            event.game.start()
        }

        var ticks = 10
        val players = event.game.players.map { it.bukkitPlayer }
        Bukkit.getScheduler().runTaskTimer(GameManager.instance, { task ->
            players.forEach { it.sendActionBar(Component.text("Game starting in ").color(NamedTextColor.WHITE)
                .append(Component.text(ticks).color(NamedTextColor.GREEN))) }

            ticks--

            if (event.game.players.size < event.game.teamSize + 1) {
                players.forEach { it.sendActionBar(Component.text("Start has been cancelled").color(NamedTextColor.RED)) }
                task.cancel()
            }

            if (ticks == 0) {
                event.game.start()
                task.cancel()
            }
        }, 0, 20)
    }
}