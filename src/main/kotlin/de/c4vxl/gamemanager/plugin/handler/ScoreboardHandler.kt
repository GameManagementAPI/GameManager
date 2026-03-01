package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.event.game.GameStateChangeEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerReviveEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameState
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

/**
 * Handles scoreboard teams for games
 */
class ScoreboardHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, GameManager.instance)
    }

    /**
     * Retrieves a team by its name or creates it if it doesn't exist
     * @param scoreboard The scoreboard to look in
     * @param name The name of the team
     */
    private fun getOrCreateTeam(scoreboard: Scoreboard, name: String) =
        scoreboard.getTeam(name) ?: scoreboard.registerNewTeam(name)

    /**
     * Initializes a team for enforcing common team rules such as friendly fire or prefixes
     * @param game The game
     * @param gameTeam The game team to enforce the rules for
     */
    fun initTeam(game: Game, gameTeam: de.c4vxl.gamemanager.gma.team.Team) {
        // Get team
        val team = getOrCreateTeam(
            game.scoreboard,
            "gma_${game.id}_${gameTeam.id}"
        )

        // Set friendly fire
        team.setAllowFriendlyFire(GameManager.instance.config.getBoolean("team.friendly-fire", false))

        // Set collisions
        if (GameManager.instance.config.getBoolean("team.team-collision", false))
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS)
        else
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS)

        // Set prefix
        if (GameManager.instance.config.getBoolean("team.display-prefix", true))
            team.prefix(MiniMessage.miniMessage().deserialize((
                    GameManager.instance.config.getString("team.prefix-format")
                        ?: "<b>\$label<b> <gray>|</gray> "
                    )
                .replace("\$label", gameTeam.label)))

        // Add players
        gameTeam.players.forEach { team.addPlayer(it.bukkitPlayer) }
    }

    @EventHandler
    fun onStarted(event: GameStateChangeEvent) {
        // Listen to state "RUNNING" since scoreboard isn't enforced for players at the time of "GameStartEvent"
        if (event.newState != GameState.RUNNING) return

        // Initialize team rules for each team of the game
        event.game.teamManager.teams.values.forEach { initTeam(event.game, it) }
    }

    @EventHandler
    fun onQuit(event: GamePlayerQuitEvent) {
        // Remove player from all scoreboard teams
        event.game.scoreboard.teams.forEach { it.removePlayer(event.player.bukkitPlayer) }
    }

    @EventHandler
    fun onRevive(event: GamePlayerReviveEvent) {
        initTeam(event.game, event.player.team ?: return)
    }
}