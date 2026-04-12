package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.event.game.GameStateChangeEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerReviveEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerSelfDamageEvent
import de.c4vxl.gamemanager.gma.event.team.GamePlayerFriendlyFireEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameState
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
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

        // Set collisions
        if (GameManager.instance.config.getBoolean("team.team-collision", false))
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS)
        else
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM)

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
    fun onFriendlyFire(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val damager = event.damageSource.causingEntity as? Player ?: return
        val team = player.gma.team ?: return
        val game = player.gma.game ?: return

        // Return if not the same game
        if (damager.gma.game != game) return

        // Return if different team
        if (!team.players.contains(damager.gma))
            return

        // Handle self damage
        if (damager == player) {
            // Call self damage event
            val allowSelfDamage = GamePlayerSelfDamageEvent(
                player.gma, game,
                GameManager.instance.config.getBoolean("team.allow-self-damage", false),
                event
            ).apply { callEvent() }.allow

            // Self damage enabled
            // Exit
            if (allowSelfDamage)
                return
        }

        // Return if friendly fire is allowed
        val allowFriendlyFire = GamePlayerFriendlyFireEvent(
            team, damager.gma, player.gma, game,
            GameManager.instance.config.getBoolean("team.friendly-fire", false),
            event
        ).apply { callEvent() }.allow

        if (allowFriendlyFire)
            return

        // Cancel damage
        event.isCancelled = true
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