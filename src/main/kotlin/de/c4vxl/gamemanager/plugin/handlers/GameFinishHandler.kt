package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.event.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class GameFinishHandler(val plugin: Plugin): Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerElimination(event: GamePlayerEliminateEvent) {
        if (!event.game.isRunning) return
        if (event.game.aliveTeams.size > 1) return
        val winnerTeam = event.game.aliveTeams.getOrNull(0)

        if (winnerTeam == null) {
            event.game.stop()
            return
        }

        // call win event
        winnerTeam.players.forEach { GamePlayerWinEvent(event.player, event.game).callEvent() }
        GameTeamWinEvent(event.game, winnerTeam).callEvent()

        // call loose event
        event.game.deadPlayers.forEach {
            GamePlayerLooseEvent(it, event.game).callEvent()
        }

        // stop game in
        event.game.broadcast(GameManager.prefix.append(Component.text("This server will stop in ").append(
            Component.text("10").color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD))))
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                event.game.broadcast(GameManager.prefix.append(Component.text("This server will stop in ").append(
                    Component.text("3").color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD))))

                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    event.game.broadcast(GameManager.prefix.append(Component.text("This server will stop in ").append(
                        Component.text("2").color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD))))

                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        event.game.broadcast(GameManager.prefix.append(Component.text("This server will stop in ").append(
                            Component.text("1").color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD))))

                        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                            event.game.stop()
                        }, 20)
                    }, 20)
                }, 20)
            }, 20)
        }, 20 * 7)
    }

    @EventHandler
    fun onWin(event: GamePlayerWinEvent) {
        if (event.player.bukkitPlayer.world.name != event.player.game?.id?.asString) return
        event.player.bukkitPlayer.sendActionBar(Component.text("Congratulations! You ").color(NamedTextColor.WHITE).append(
            Component.text("WON!").color(NamedTextColor.GREEN)))
    }

    @EventHandler
    fun onLoose(event: GamePlayerLooseEvent) {
        if (event.player.bukkitPlayer.world.name != event.player.game?.id?.asString) return
        event.player.bukkitPlayer.sendActionBar(Component.text("It is a shame! You ").color(NamedTextColor.WHITE).append(
            Component.text("Lost!").color(NamedTextColor.RED)))
    }
}