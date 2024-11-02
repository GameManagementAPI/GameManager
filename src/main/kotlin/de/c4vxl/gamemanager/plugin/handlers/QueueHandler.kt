package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerJoinEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStopEvent
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
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
                        .append(Component.text(" (${game.players.size}/${game.maxPlayers})")))
            }
        }, 10, 10)

        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private val countdowns: MutableMap<Game, Pair<Int, Int>> = mutableMapOf()
    private val bars: MutableMap<Game, BossBar> = mutableMapOf()

    private fun startCountdown(game: Game, seconds: Int) {
        countdowns[game] = Pair(seconds, seconds)

        val bar: BossBar = Bukkit.createBossBar("Game starts in: $seconds", BarColor.GREEN, BarStyle.SOLID)
        bars[game] = bar
        game.players.forEach { bar.addPlayer(it.bukkitPlayer) }

        Bukkit.getScheduler().runTaskTimer(GameManager.instance, { task ->
            val c = countdowns[game] ?: Pair(0, 1)

            if (c.first <= 0) {
                bar.removeAll()
                game.start()
                task.cancel()
                return@runTaskTimer
            }

            bar.setTitle("Game starts in: ${c.first}")
            bar.progress = c.first.toDouble() / c.second

            countdowns[game] = Pair(c.first - 1, c.second)
        }, 0, 20)
    }

    private fun setCountdown(game: Game, seconds: Int) {
        if (countdowns.containsKey(game))
            countdowns[game] = Pair(seconds, seconds)
        else
            startCountdown(game, seconds)
    }

    private fun checkForPlayerStatus(game: Game): Double {
        val maxPlayers: Int = game.maxPlayers

        return when (game.players.size) {
            (maxPlayers * 0.25).toInt() -> 0.75
            (maxPlayers * 0.5).toInt() -> 0.5
            (maxPlayers * 0.75).toInt() -> 0.25
            else -> 1.0
        }
    }

    @EventHandler
    fun onPlayerJoin(event: GamePlayerJoinEvent) {
        if (event.game.teamAmount == 1) {
            event.game.start()
            return
        }

        if (event.game.isFull)
            setCountdown(event.game, 10)
        else if (event.game.players.size >= event.game.teamSize * 2)
            setCountdown(event.game, (checkForPlayerStatus(event.game) * 60).toInt())
    }

    @EventHandler
    fun onPlayerQuit(event: GamePlayerQuitEvent) {
        bars[event.game]?.removePlayer(event.player.bukkitPlayer)
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        countdowns.remove(event.game)
        bars.remove(event.game)
    }
}