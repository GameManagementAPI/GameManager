package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.event.player.GamePlayerScoreboardChangeEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerSelfDamageEvent
import de.c4vxl.gamemanager.gma.event.team.GamePlayerFriendlyFireEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
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
     * Displays a team to a player
     * @param gameTeam The team to display
     * @param viewer The player to display the team to
     * @param scoreboard The scoreboard to use
     */
    fun display(gameTeam: de.c4vxl.gamemanager.gma.team.Team, viewer: Player, scoreboard: Scoreboard = viewer.scoreboard) {
        // Wrong game
        if (viewer.gma.game != gameTeam.manager.game)
            return

        // Get team
        val team = "gma_team_${gameTeam.id}".let { scoreboard.getTeam(it) ?: scoreboard.registerNewTeam(it) }

        // Add prefix
        if (GameManager.instance.config.getBoolean("team.display-prefix", true))
            team.prefix(MiniMessage.miniMessage().deserialize((
                    GameManager.instance.config.getString("team.prefix-format")
                        ?: "<b>\$label<b> <gray>|</gray> "
                    )
                .replace("\$label", gameTeam.labelStr(viewer.language))))

        // Set team collision
        team.setOption(
            Team.Option.COLLISION_RULE,
            if (GameManager.instance.config.getBoolean("team.team-collision", false)) Team.OptionStatus.ALWAYS
            else Team.OptionStatus.FOR_OWN_TEAM
        )

        // Add team members
        team.addEntries(gameTeam.players.map { it.bukkitPlayer.name })
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
    fun onScoreboardChange(event: GamePlayerScoreboardChangeEvent) {
        // Returns if new scoreboard is not a game scoreboard
        val game = event.game ?: return

        game.teamManager.teams.values.forEach { team -> display(team, event.player.bukkitPlayer, event.to) }
    }
}