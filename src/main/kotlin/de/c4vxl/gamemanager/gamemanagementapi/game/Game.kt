package de.c4vxl.gamemanager.gamemanagementapi.game

import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.team.TeamManager
import de.c4vxl.gamemanager.gamemanagementapi.world.WorldManager

class Game(
    val teamSize: Int,
    val teamAmount: Int,
    val id: GameID = GameID.generateRandom(),
    val players: MutableList<GMAPlayer> = mutableListOf()
) {
    // classes for management
    val worldManager: WorldManager = WorldManager(this)
    val teamManager: TeamManager = TeamManager(this)

    // player functions
    val maxPlayer: Int get() = teamSize * teamAmount
    val isFull: Boolean get() = players.size >= maxPlayer

    // functions to keep track of current state of the game
    var gameState: GameState = GameState.QUEUEING
    val isRunning: Boolean get() = mutableListOf(GameState.RUNNING, GameState.STOPPING).contains(gameState)
    val isQueuing: Boolean get() = gameState == GameState.QUEUEING
    val isStarting: Boolean get() = gameState == GameState.STARTING
    val isOver: Boolean get() = mutableListOf(GameState.STOPPED, GameState.STOPPING).contains(gameState)

    fun join(player: GMAPlayer): Boolean {
        if (!player.canJoin(this)) return false

        // add player to list
        players.add(player)

        // set player's game
        player.game = this

        if (isFull) start() // start game if it is full

        return true
    }

    fun quit(player: GMAPlayer): Boolean {
        if (!players.contains(player)) return false

        players.remove(player)
        player.game = null

        return true
    }

    fun start(): Boolean {
        if (!isQueuing) return false

        gameState = GameState.STARTING

        players.forEach { player ->
            if (!player.isInTeam)
                teamManager.joinRandom(player) // join random team if player is in none

            // load map
            worldManager.loadMap()

            // teleport players to spawn
            teamManager.teams.forEach { team ->
                worldManager.mapConfig.getTeamSpawn(team.id)?.let { spawn ->
                    team.players.forEach { it.bukkitPlayer.teleport(spawn) }
                } ?: team.players.forEach { it.quitGame() }
            }
        }

        gameState = GameState.RUNNING

        return true
    }

    fun forceStop(): Boolean {
        gameState = GameState.RUNNING
        return stop().also {
            gameState = GameState.KILLED
        }
    }

    fun stop(): Boolean {
        if (!isRunning) return false

        gameState = GameState.STOPPING

        worldManager.removeWorld()

        gameState = GameState.STOPPED

        return true
    }
}