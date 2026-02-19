package de.c4vxl.gamemanager.gma.game

import de.c4vxl.gamemanager.gma.event.game.GameMessageBroadcastEvent
import de.c4vxl.gamemanager.gma.event.game.GameStartEvent
import de.c4vxl.gamemanager.gma.event.game.GameStateChangeEvent
import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEliminateEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerReviveEvent
import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.game.type.GameState
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.team.TeamManager
import de.c4vxl.gamemanager.gma.world.WorldManager
import net.kyori.adventure.text.Component

/**
 * Core game object
 * @param size The size of the game
 * @param id A unique identifier for each game
 */
class Game(
    val size: GameSize,
    val id: GameID = GameID.random()
) {
    /**
     * Holds the information about the teams in this game
     */
    val teamManager: TeamManager = TeamManager(this)

    /**
     * Holds the world manager
     */
    val worldManager: WorldManager = WorldManager(this)

    /**
     * Holds the current state of the game
     */
    var state: GameState = GameState.QUEUING
        set(value) {
            // Call event
            GameStateChangeEvent(this, state, value).let {
                it.callEvent()
                if (it.isCancelled) return
            }

            field = value
        }

    /**
     * Holds a list of all players in the game
     */
    val players: MutableList<GMAPlayer> = mutableListOf()

    /**
     * Holds a list of all players that have been eliminated
     */
    val eliminatedPlayers: MutableList<GMAPlayer> = mutableListOf()

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

    // TODO: Implement player elimination
    // TODO: Implement player revive

    // TODO: Handle player loose / win

    /**
     * Starts the game
     * @return Returns {@code true} upon success
     */
    fun start(): Boolean {
        if (!isQueuing) return false

        // Call game start event
        GameStartEvent(this).let {
            it.callEvent()
            if (it.isCancelled) return false
        }

        this.state = GameState.STARTING

        // Make players without a team join a random one
        this.players.forEach {
            if (!it.isInTeam)
                this.teamManager.joinRandom(it)
        }

        // Load map
        this.worldManager.forcemap?.let { this.worldManager.load(it) } // If forcemap is set use it
            ?: this.worldManager.loadRandom()                          // otherwise choose a random one

        // Prepare players
        this.teamManager.teams.values.forEach { team ->
            this.worldManager.map.getSpawnLocation(team.id)?.let { spawn ->
                team.players.forEach {
                    // Teleport players
                    it.bukkitPlayer.teleport(spawn)

                    // Reset players
                    it.reset()
                }
            }
        }

        this.state = GameState.RUNNING
        return true
    }

    /**
     * Stops the game
     * @return Returns {@code true} upon success
     */
    fun stop(): Boolean {
        if (!isRunning && !isQueuing && state != GameState.STOPPING) return false

        // Call game stop event
        val stopEvent = GameStopEvent(this).also {
            it.callEvent()
            if (it.isCancelled) return false
        }

        this.state = GameState.STOPPING

        // Remove all players from game
        this.players.distinct().forEach { this.quit(it) }

        // Kick players to unload world properly
        // If kickPlayers was set to false we assume another plugin takes care of removing the players from the world
        if (stopEvent.kickPlayers)
            this.worldManager.map.world?.players?.forEach { it.kick() }

        // Delete world
        this.worldManager.map.unload()

        this.state = GameState.STOPPED
        return true
    }

    /**
     * Eliminates a player from the game
     * @param player The player to eliminate
     */
    fun eliminate(player: GMAPlayer) {
        if (player.game != this) return
        if (player.isEliminated) return

        // Call event
        GamePlayerEliminateEvent(player, this).let {
            it.callEvent()
            if (it.isCancelled) return
        }

        eliminatedPlayers.add(player)
    }

    /**
     * Revives an eliminated player
     * @param player The player to revive
     */
    fun revive(player: GMAPlayer) {
        if (player.game != this) return
        if (!player.isEliminated) return

        // Call event
        GamePlayerReviveEvent(player, this).let {
            it.callEvent()
            if (it.isCancelled) return
        }

        eliminatedPlayers.remove(player)
    }

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
        players.add(player)
        player.game = this

        // Call join event
        GamePlayerJoinedEvent(player, this).let {
            it.callEvent()
            if (it.isCancelled) {
                players.remove(player)
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
        if (!this.players.contains(player))
            return false

        // Call quit event
        GamePlayerQuitEvent(player, this).let {
            it.callEvent()
            if (it.isCancelled) return false
        }

        // Quit team
        this.teamManager.quit(player)

        // Remove player from game
        players.removeAll { it.bukkitPlayer.uniqueId == player.bukkitPlayer.uniqueId }
        player.game = null

        return true
    }

    /**
     * Broadcasts a message to the entire game
     * @param message The message to send
     */
    fun broadcastMessage(message: Component) {
        val audience = this.players.distinct()

        // Call event
        GameMessageBroadcastEvent(this, message, audience).let {
            it.callEvent()
            if (it.isCancelled) return
        }

        // Send message
        audience.forEach { it.bukkitPlayer.sendMessage(message) }
    }

    override fun equals(other: Any?): Boolean {
        return this.id.asString == (other as? Game)?.id?.asString
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    override fun toString(): String {
        return "Game { id=${this.id}, state=${this.state} }"
    }
}