package de.c4vxl.gamemanager.gma.game

import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.game.type.GameState
import de.c4vxl.gamemanager.gma.player.GMAPlayer

class Game(
    val size: GameSize,
    val id: GameID = GameID.random(),
    var state: GameState = GameState.QUEUING
) {
    /**
     * Holds a list of all players in the game
     */
    val players: MutableList<GMAPlayer> = mutableListOf()

    /**
     * Returns 'true' when the game is in a queuing state
     */
    val isQueuing: Boolean get() = this.state == GameState.QUEUING

    /**
     * Returns 'true' when the game is in a running state
     */
    val isRunning: Boolean get() = this.state == GameState.RUNNING

    /**
     * Returns 'true' when the game has been stopped
     */
    val isStopped: Boolean get() = this.state == GameState.STOPPED

    /**
     * Starts the game
     */
    fun start() {

    }

    /**
     * Stops the game
     */
    fun stop() {

    }


    override fun equals(other: Any?): Boolean {
        return this.id == (other as? Game)?.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}