package de.c4vxl.gamemanager.gamemanagementapi.team

import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer

class Team(val manager: TeamManager, val id: Int, val players: MutableList<GMAPlayer> = mutableListOf()) {
    val maxSize: Int = manager.game.teamSize
    val isFull: Boolean get() = players.size >= maxSize

    /**
     * Makes a player join this team if he is in this game
     * @return Boolean
      */
    fun join(player: GMAPlayer): Boolean {
        if (!manager.game.players.contains(player) || player.isInTeam) return false

        players.add(player)

        return true
    }

    /**
     * Makes a player quit this team
      */
    fun quit(player: GMAPlayer): Boolean {
        if (!players.contains(player)) return false

        players.remove(player)

        return true
    }
}