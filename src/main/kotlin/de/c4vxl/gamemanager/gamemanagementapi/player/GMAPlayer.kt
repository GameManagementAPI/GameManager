package de.c4vxl.gamemanager.gamemanagementapi.player

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.team.Team
import org.bukkit.entity.Player

class GMAPlayer private constructor(val bukkitPlayer: Player) {
    companion object {
        private val playerInstances = mutableMapOf<Player, GMAPlayer>()

        fun fromBukkit(player: Player): GMAPlayer {
            return playerInstances.getOrPut(player) { GMAPlayer(player) }
        }

        val Player.asGamePlayer: GMAPlayer get() = fromBukkit(this)
    }

    val isOnline: Boolean get() = bukkitPlayer.isOnline

    // keeping track of games
    val isInGame: Boolean get() = game != null
    var game: Game? = null
    val isSpectating: Boolean get() = game?.spectators?.contains(this) == true

    // keep track of team
    val isInTeam: Boolean get() = team != null
    val team: Team? get() = game?.teamManager?.getTeam(this)

    /**
     * Start spectating a game
     *  ==> executes game.spectate
     */
    fun spectate(game: Game): Boolean = game.spectate(this)

    // game connection logic
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


    // eliminate/revive logic
    /**
     * Shortcut to eliminate a player in his game
     *  ==> will execute game.eliminatePlayer
     */
    fun eliminate(): Boolean = game?.eliminatePlayer(this) == true

    /**
     * Shortcut to revive an eliminated player in his game
     *  ==> will execute game.revivePlayer
     */
    fun revive(): Boolean = game?.revivePlayer(this) == true

    /**
     * Check if a player is marked as eliminated in his game
     */
    val isEliminated: Boolean get() = game?.deadPlayers?.contains(this) == true
}
