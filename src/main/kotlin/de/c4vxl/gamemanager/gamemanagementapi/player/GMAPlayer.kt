package de.c4vxl.gamemanager.gamemanagementapi.player

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.team.Team
import org.bukkit.OfflinePlayer

class GMAPlayer private constructor(val bukkitPlayer: OfflinePlayer) {
    companion object {
        private val playerInstances = mutableMapOf<OfflinePlayer, GMAPlayer>()

        fun fromBukkit(player: OfflinePlayer): GMAPlayer {
            return playerInstances.getOrPut(player) { GMAPlayer(player) }
        }

        val OfflinePlayer.asGamePlayer: GMAPlayer get() = fromBukkit(this)
    }

    val isOnline: Boolean get() = bukkitPlayer.isOnline

    // keeping track of games
    val isInGame: Boolean get() = game != null
    var game: Game? = null

    // keep track of team
    val isInTeam: Boolean get() = team != null
    val team: Team? get() = game?.teamManager?.getTeam(this)

    /**
     * Makes a player join a game by executing game.join(this)
     *  won't proceed if player is already in a game
     * @return Boolean
      */
    fun joinGame(game: Game): Boolean = game.join(this)

    /**
     * Makes player quit his current game if he is in one
     * @return Boolean
     */
    fun quitGame(): Boolean = game?.quit(this) ?: false

    /**
     * checks if a player can join a game
     *  ==> game must be in queue phase and player cannot be in **any** game
     *  @return Boolean
      */
    fun canJoin(game: Game): Boolean = !game.isFull && game.isQueuing && !game.players.contains(this) && !isInGame
}
