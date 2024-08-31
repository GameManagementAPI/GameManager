package de.c4vxl.gamemanager.gamemanagementapi.team

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer

class TeamManager(val game: Game) {
    val teams: MutableList<Team> = mutableListOf()
    val joinableTeams: MutableList<Team> get() = teams.filter { !it.isFull }.toMutableList()

    init {
        // init teams list
        for (i in 0..<game.teamAmount) teams.add(Team(this, i))
    }

    // join/quit mechanics
    fun join(player: GMAPlayer, team: Team): Boolean {
        if (!joinableTeams.contains(team)) return false

        return team.join(player)
    }
    fun joinRandom(player: GMAPlayer): Boolean = joinableTeams.randomOrNull()?.let { t -> join(player, t) } ?: false

    fun quit(player: GMAPlayer): Boolean = player.team?.quit(player) ?: false

    /**
     * Gets the team of a player
     * @return Team?
     */
    fun getTeam(player: GMAPlayer): Team? = teams.find { it.players.contains(player) }

    /**
     * Checks if a player is in a team
     */
    fun isInTeam(player: GMAPlayer): Boolean = getTeam(player) != null
}