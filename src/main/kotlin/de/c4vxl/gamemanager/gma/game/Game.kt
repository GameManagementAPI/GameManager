package de.c4vxl.gamemanager.gma.game

import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.game.type.GameState
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.team.TeamManager

/**
 * Core game object
 * @param size The size of the game
 * @param id A unique identifier for each game
 * @param state The initial game state (Recommended: Queuing)
 */
class Game(
    val size: GameSize,
    val id: GameID = GameID.random(),
    var state: GameState = GameState.QUEUING
) {
    /**
     * Holds the information about the teams in this game
     */
    val teamManager: TeamManager = TeamManager(this)

    /**
     * Holds a list of all players in the game
     */
    val players: MutableList<GMAPlayer> = mutableListOf()

    /**
     * Returns {@code true} when the game is in a queuing state
     */
    val isQueuing: Boolean get() = this.state == GameState.QUEUING

    /**
     * Returns {@code true} when the game is in a running state
     */
    val isRunning: Boolean get() = this.state == GameState.RUNNING

    /**
     * Returns {@code true} when the game has been stopped
     */
    val isStopped: Boolean get() = this.state == GameState.STOPPED

    /**
     * Returns {@code true} if the game has reached maximum players
     */
    val isFull: Boolean get() = this.players.size >= size.maxPlayers

    /**
     * Returns {@code true} if join conditions for a certain player are met
     * @param player The player
     */
    fun canJoin(player: GMAPlayer): Boolean =
        !isFull && isQueuing && !players.contains(player) && !player.isInGame

    /**
     * Starts the game
     * @return Returns {@code true} upon success
     */
    fun start(): Boolean {
        if (!isQueuing) return false

        this.state = GameState.STARTING

        // Make players without a team join a random one
        this.players.forEach {
            if (it.isInTeam)
                this.teamManager.joinRandom(it)
        }

        // TODO: Implement world loading

        this.teamManager.teams.values.forEach { team ->
            // TODO: Teleport players to team spawn

            // Reset players
            team.players.forEach { it.reset() }
        }

        this.state = GameState.RUNNING
        return true
    }

    /**
     * Stops the game
     * @return Returns {@code true} upon success
     */
    fun stop(): Boolean {
        if (!isRunning) return false
        this.state = GameState.STOPPING

        // TODO: Add game stop logic

        this.state = GameState.STOPPED
        return true
    }

    /**
     * Make a player join this game
     * @param player The player to make join
     * @param force If set to {@code true} player will be forced to quit his old game in order to join this one
     * @return {@code true} upon success
     */
    fun join(player: GMAPlayer, force: Boolean = false): Boolean {
        if (!canJoin(player)) return false

        // Player is already in a game
        // Quit if force-flag is passed, otherwise exit
        if (player.isInGame)
            if (force) player.quit()
            else return false

        // Add player to this game
        players.add(player)
        player.game = this

        return true
    }

    /**
     * Make a player quit this game
     * @param player The player
     * @return {@code true} upon success
     */
    fun quit(player: GMAPlayer): Boolean {
        // Player not in this game
        if (!this.players.contains(player))
            return false

        // Quit team
        this.teamManager.quit(player)

        // Remove player from game
        players.remove(player)
        player.game = null

        return true
    }

    override fun equals(other: Any?): Boolean {
        return this.id.asString == (other as? Game)?.id?.asString
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}