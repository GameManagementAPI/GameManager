package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class QueueHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    val bars: MutableMap<Game, MutableMap<GMAPlayer, BossBar>> = mutableMapOf()

    /**
     * Initializes the countdown boss bar for a player
     * @param player The player
     */
    private fun initCountdown(player: GMAPlayer) {
        if (player.game == null)
            return

        bars[player.game!!] =
            bars.getOrDefault(player.game, mutableMapOf())
                .apply {
                    this[player] = Bukkit.createBossBar(player.language.get("queue.countdown.title", "-1"), BarColor.GREEN, BarStyle.SOLID)
                    this[player]!!.addPlayer(player.bukkitPlayer)
                }
    }

    /**
     * Displays a countdown to all players
     * @param game The game to display the countdown for
     * @param seconds The time of the countdown
     */
    private fun startCountdown(game: Game, seconds: Int) {
        game.players.forEach { initCountdown(it)}

        var ticks = 0
        Bukkit.getScheduler().runTaskTimer(Main.instance, { task ->
            val time = seconds - ticks
            var cancel = false

            // Too little players
            // Can't fill two teams
            if (game.players.size < game.size.teamSize * 2) {
                game.players.forEach { it.bukkitPlayer.sendActionBar(it.language.getCmp("queue.countdown.cancelled")) }
                cancel = true
            }

            // Time has run out
            // Start game
            if (time <= 0 || game.isRunning) {
                game.start()
                cancel = true
            }

            // Cancel bar
            if (cancel) {
                bars.getOrDefault(game, mutableMapOf()).forEach { it.value.removeAll() }
                task.cancel()
            }

            // Update bar
            bars[game]?.forEach { (player, bar) ->
                bar.setTitle(player.language.get("queue.countdown.title", time.toString()))
                bar.progress = (time.toDouble() / seconds).coerceIn(0.0, 1.0)
            }

            ticks++
        }, 0, 20)
    }

    /**
     * Returns a factor for waiting time based on how full the game is
     * @param game The game
     */
    private fun waitingFactor(game: Game): Double {
        val max = game.size.maxPlayers
        return when (game.players.size) {
            (max * 0.25).toInt() -> 0.75
            (max * 0.5).toInt() -> 0.5
            (max * 0.75).toInt() -> 0.25
            else -> 0.0
        }
    }

    @EventHandler
    fun onJoin(event: GamePlayerJoinedEvent) {
        // Game is full
        // Start a five-second countdown
        if (event.game.isFull)
            startCountdown(event.game, 5)

        // Enough to fill two teams
        // Start a countdown
        else if (event.game.players.size >= event.game.size.teamSize * 2)
            startCountdown(event.game, (waitingFactor(event.game) * 60).toInt())

        // Initialize countdown for players that joined late
        if (bars.containsKey(event.game) && bars[event.game]?.containsKey(event.player) != true)
            initCountdown(event.player)
    }

    @EventHandler
    fun onQuit(event: GamePlayerQuitEvent) {
        // Clear bar
        bars[event.game]?.get(event.player)?.removeAll()
    }

    @EventHandler
    fun onStop(event: GameStopEvent) {
        // Clear game bars
        bars.remove(event.game)
    }
}