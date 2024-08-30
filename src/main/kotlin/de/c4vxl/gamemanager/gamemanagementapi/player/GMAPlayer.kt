package de.c4vxl.gamemanager.gamemanagementapi.player

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
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
