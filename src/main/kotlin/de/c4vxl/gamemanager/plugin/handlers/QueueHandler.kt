package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class QueueHandler(plugin: Plugin) {
    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            Bukkit.getOnlinePlayers().forEach { player ->
                val game = player.asGamePlayer.game ?: return@forEach
                if (!game.isQueuing) return@forEach

                player.sendActionBar(Component.text("Waiting for other players...")
                    .color(NamedTextColor.WHITE)
                    .append(Component.text(" (${game.players.size}/${game.maxPlayer})")))
            }
        }, 0, 0)
    }
}