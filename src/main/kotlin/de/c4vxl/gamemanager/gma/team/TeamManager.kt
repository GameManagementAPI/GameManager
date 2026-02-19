package de.c4vxl.gamemanager.gma.team

import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.event.team.GamePlayerTeamJoinEvent
import de.c4vxl.gamemanager.gma.event.team.GamePlayerTeamQuitEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer

/**
 * Holds information about teams of a game
 * @param game The game this manager is responsible for
 */
class TeamManager(
    val game: Game
) {
    /**
     * Holds a list of all teams registered in this manager
     */
    val teams: MutableMap<Int, Team> = mutableMapOf()

    /**
     * Returns a list of all teams that still have at least one player alive
     */
    val aliveTeams: List<Team>
        get() = teams.values.filter {
            it.players.any { player ->
                // Using contains to properly handle players that joined another game in the meantime
                this.game.playerManager.alivePlayers.contains(player)
            }
        }

    init {
        for (id in 0..<game.size.teamAmount)
            teams[id] = Team(this, id)
    }

    /**
     * Returns the team a player is part of
     * @param player The player
     */
    fun get(player: GMAPlayer): Team? = teams.values.find { it.players.contains(player) }

    /**
     * Returns a random team
     * @param joinable If set to {@code true} only teams that aren't already full can be returned
     */
    fun random(joinable: Boolean = true): Team? {
        // Filter teams
        val teams = teams
            .filterValues { !joinable || !it.isFull }
            .values.toList()

        return teams.randomOrNull()
    }

    /**
     * Makes a player join a random game
     * @param player The player
     * @param force If set to {@code true} the player will quit his old team in order to join
     */
    fun joinRandom(player: GMAPlayer, force: Boolean = false): Team? {
        val team = random() ?: return null

        val success = join(player, team.id, force)

        return if (success) team
               else null
    }

    /**
     * Makes a player join a specific team
     * @param player The player
     * @param id The id of the team the player should join
     * @param force If set to {@code true} the player will quit his old team in order to join
     * @return {@code true} upon success
     */
    fun join(player: GMAPlayer, id: Int, force: Boolean = false): Boolean {
        // Wrong game
        if (player.game != this.game) return false

        // Get team
        val team = teams[id] ?: return false

        // Return if team is full
        if (team.isFull) return false

        // Call join event
        GamePlayerTeamJoinEvent(team, player, this.game).let {
            it.callEvent()
            if (it.isCancelled) return false
        }

        // Quit old team if force flag is passed
        if (force)
            player.team?.manager?.quit(player)

        // Add player to team
        return team.players.add(player)
    }

    /**
     * Makes a player quit his team
     * @param player The player to quit
     * @return {@code true} upon success
     */
    fun quit(player: GMAPlayer): Boolean {
        // Wrong game
        if (player.game != this.game) return false

        val team = player.team ?: return false

        // Call quit event
        GamePlayerTeamQuitEvent(team, player, this.game).let {
            it.callEvent()
            if (it.isCancelled) return false
        }

        // Remove player from team
        return team.players.remove(player)
    }
}