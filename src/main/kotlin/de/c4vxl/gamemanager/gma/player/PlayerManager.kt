package de.c4vxl.gamemanager.gma.player

import de.c4vxl.gamemanager.gma.event.player.GamePlayerEliminateEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerReviveEvent
import de.c4vxl.gamemanager.gma.game.Game

/**
 * Object responsible for managing players of a game
 * @param game The game to manage the players of
 */
class PlayerManager(
    val game: Game
) {
    private val internalPlayers: MutableList<GMAPlayer> = mutableListOf()
    private val internalEliminatedPlayers: MutableList<GMAPlayer> = mutableListOf()

    /**
     * Returns a list of the players connected to this game
     */
    val players: List<GMAPlayer>
        get() = internalPlayers.distinct().toList()

    /**
     * Returns a list of all players that have been eliminated
     */
    val eliminatedPlayers: List<GMAPlayer>
        get() = internalEliminatedPlayers.distinct().toList()

    /**
     * Returns a list of all players that are still alive
     */
    val alivePlayers: List<GMAPlayer>
        get() = players.filterNot { !it.isEliminated && it.game == this.game }

    /**
     * Returns {@code true} if join conditions for a certain player are met
     * @param player The player
     */
    fun canJoin(player: GMAPlayer): Boolean =
        !game.isFull && game.isQueuing && !internalPlayers.contains(player) && !player.isInGame

    /**
     * Make a player join this game
     * @param player The player to make join
     * @param force If set to {@code true} player will be forced to quit his old game in order to join this one
     * @return {@code true} upon success
     */
    fun join(player: GMAPlayer, force: Boolean = false): Boolean {
        if (!canJoin(player)) return false

        val pastGame = player.game

        // Player is already in a game
        // Quit if force-flag is passed, otherwise exit
        if (player.isInGame)
            if (force) player.quit()
            else return false

        // Add player to this game
        internalPlayers.add(player)
        player.game = this.game

        // Call join event
        GamePlayerJoinedEvent(player, this.game).let {
            it.callEvent()
            if (it.isCancelled) {
                internalPlayers.remove(player)
                player.game = pastGame
                return false
            }
        }

        return true
    }

    /**
     * Make a player quit this game
     * @param player The player
     * @return {@code true} upon success
     */
    fun quit(player: GMAPlayer): Boolean {
        // Player not in this game
        if (!this.internalPlayers.contains(player))
            return false

        // Call quit event
        GamePlayerQuitEvent(player, this.game).let {
            it.callEvent()
            if (it.isCancelled) return false
        }

        // Eliminate player
        player.eliminate()

        // Quit team
        this.game.teamManager.quit(player)

        // Remove player from game
        internalPlayers.removeAll { it.bukkitPlayer.uniqueId == player.bukkitPlayer.uniqueId }
        player.game = null

        return true
    }

    /**
     * Eliminates a player from the game
     * @param player The player to eliminate
     */
    fun eliminate(player: GMAPlayer) {
        if (player.game != this.game) return
        if (player.isEliminated) return

        internalEliminatedPlayers.add(player)

        // Call event
        GamePlayerEliminateEvent(player, this.game).let {
            it.callEvent()
            if (it.isCancelled) internalEliminatedPlayers.remove(player)
        }
    }

    /**
     * Revives an eliminated player
     * @param player The player to revive
     */
    fun revive(player: GMAPlayer) {
        if (player.game != this.game) return
        if (!player.isEliminated) return

        internalEliminatedPlayers.remove(player)

        // Call event
        GamePlayerReviveEvent(player, this.game).let {
            it.callEvent()
            if (it.isCancelled) internalEliminatedPlayers.add(player)
        }
    }

    /**
     * Returns {@code true} if a player is eliminated
     * @param player The player to check
     */
    fun isEliminated(player: GMAPlayer): Boolean =
        this.internalEliminatedPlayers.contains(player)
}