package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStartEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStateChangeEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStopEvent
import de.c4vxl.gamemanager.gamemanagementapi.game.GameState
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

class PlayerPrefixHandler(plugin: Plugin) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onGameRunning(event: GameStateChangeEvent) {
        if (event.newState != GameState.RUNNING) return

        event.game.teamManager.teams.forEach { gameTeam ->
            val sb: Scoreboard = event.game.scoreboard
            val team: Team = "gamemanager_${event.game.id.asString}_${gameTeam.id}".let { sb.getTeam(it) ?: sb.registerNewTeam(it) }

            // disable friendly fire
            team.setAllowFriendlyFire(false)

            team.prefix(
                LegacyComponentSerializer.legacySection().deserialize(gameTeam.name)
                    .append(Component.text(" | ").color(NamedTextColor.GRAY)))

            gameTeam.players.forEach { team.addPlayer(it.bukkitPlayer) }
        }
    }

    @EventHandler
    fun onGameStart(event: GameStartEvent) {
        event.game.players.forEach {
            it.bukkitPlayer.scoreboard = event.game.scoreboard
        }
    }

    @EventHandler
    fun onPlayerLeave(event: GamePlayerQuitEvent) {
        // get team of player
        val team = event.player.team ?: return

        // get scoreboard team
        val sb: Scoreboard = event.game.scoreboard
        val sbTeams: List<Team> = sb.teams.filter { it.name.startsWith("gamemanager_${event.game.id.asString}_") }

        // remove player from sb team
        sbTeams.forEach { it.removePlayer(event.player.bukkitPlayer) }

        event.player.bukkitPlayer.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        event.game.players.apply { addAll(event.game.spectators) }.forEach {
            it.bukkitPlayer.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        }
    }
}