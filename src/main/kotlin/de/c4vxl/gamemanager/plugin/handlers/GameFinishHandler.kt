package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import de.c4vxl.gamemanager.gamemanagementapi.event.*
import de.c4vxl.gamemanager.gamemanagementapi.game.GameState
import de.c4vxl.gamemanager.plugin.commands.PrivateGameCommand
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

        // change GameState to stopping
        // the eliminatePlayer function will exit if the game is not in a running state
        // this ensures that the below code will not be executed multiple times in a game
        event.game.gameState = GameState.STOPPING

        if (winnerTeam == null) {
            event.game.stop()
            return
        }

        val winnerPlayers = winnerTeam.players.apply { addAll(winnerTeam.quitPlayers) }.distinct()

        // call win event
        winnerPlayers.forEach { GamePlayerWinEvent(it, event.game).callEvent() }
        GameFinishEvent(event.game, winnerTeam).callEvent()

        // call loose event
        event.game.deadPlayers.filter { !winnerPlayers.contains(it) }.forEach {
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

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        // remove all invites if game was private and had any
        if (event.game.isPrivate) PrivateGameCommand.invites.remove(event.game.id.asString)

        // unregister game
        GameManagementAPI.unregisterGame(event.game)
    }
}