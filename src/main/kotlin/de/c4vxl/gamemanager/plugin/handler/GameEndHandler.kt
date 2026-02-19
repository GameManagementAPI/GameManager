package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.event.game.GameEndEvent
import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEliminateEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerLooseEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerWinEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameState
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Responsible for properly unregistering games after they stopped
 */
class GameEndHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, GameManager.instance)
    }

    /**
     * Starts a 10-second countdown for stopping a game
     * @param game The game to stop
     */
    private fun stopCountdown(game: Game) {
        game.broadcastMessage("end.countdown", "10")

        Bukkit.getScheduler().runTaskLater(GameManager.instance, Runnable {
            game.broadcastMessage("end.countdown", "3")

            Bukkit.getScheduler().runTaskLater(GameManager.instance, Runnable {
                game.broadcastMessage("end.countdown", "2")

                Bukkit.getScheduler().runTaskLater(GameManager.instance, Runnable {
                    game.broadcastMessage("end.countdown", "1")

                    Bukkit.getScheduler().runTaskLater(GameManager.instance, Runnable {
                        GameManager.instance.logger.info("Stopped game ${game.id}")
                        game.stop()
                    }, 20)
                }, 20)
            }, 20)
        }, 20 * 7)
    }

    @EventHandler
    fun onStop(event: GameStopEvent) {
        GMA.unregisterGame(event.game, false)
    }

    @EventHandler
    fun onEliminate(event: GamePlayerEliminateEvent) {
        // Game not running
        if (!event.game.isRunning) return

        // Multiple teams alive
        // No distinct winner
        if (event.game.teamManager.aliveTeams.size > 1) return

        // Set game state to stopping
        // We do this so this method doesn't run twice causing two countdowns to be created
        event.game.state = GameState.STOPPING

        // Get winner team
        val winnerTeam = event.game.teamManager.aliveTeams.getOrNull(0)

        GameManager.instance.logger.info("Game finished: ${event.game.id}")

        // Trigger event
        GameEndEvent(
            event.game,
            winnerTeam,
            event.game.teamManager.teams.values.filter { it != winnerTeam }
        ).let {
            it.callEvent()
            if (it.isCancelled) {
                GameManager.instance.logger.info("Game end cancelled by listener (${event.game.id})")
                return
            }
        }

        // No winner
        // Panic and exit
        if (winnerTeam == null) {
            GameManager.instance.logger.warning("Game finished but no winner found (${event.game.id}). Stopping game...")
            event.game.stop()
            return
        }

        // Trigger win event for every player
        winnerTeam.players.forEach { GamePlayerWinEvent(it, event.game).callEvent() }

        // Trigger loose event
        event.game.playerManager.eliminatedPlayers.filter { it.team != winnerTeam }
            .forEach { GamePlayerLooseEvent(it, event.game).callEvent() }

        // Stop game
        stopCountdown(event.game)
    }

    @EventHandler
    fun onQuit(event: GamePlayerQuitEvent) {
        // Game not in queue
        // If the game is running GameEndHandler#onEliminate will take care as GMAPlayer#quit calls #eliminate as well
        if (!event.game.isQueuing)
            return

        // Game is not empty
        if (event.game.players.isEmpty())
            return

        // Stop game immediately
        event.game.stop()
    }
}