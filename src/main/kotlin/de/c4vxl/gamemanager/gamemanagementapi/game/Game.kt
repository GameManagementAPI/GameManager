package de.c4vxl.gamemanager.gamemanagementapi.game

import de.c4vxl.gamemanager.gamemanagementapi.event.*
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.team.Team
import de.c4vxl.gamemanager.gamemanagementapi.team.TeamManager
import de.c4vxl.gamemanager.gamemanagementapi.world.WorldManager
import net.kyori.adventure.text.Component

class Game(
    val teamAmount: Int,
    val teamSize: Int,
    val id: GameID = GameID.generateRandom(),
    val players: MutableList<GMAPlayer> = mutableListOf()
) {
    // game size as a string
    val gameSize: String = "${teamAmount}x${teamSize}"

    // classes for management
    val worldManager: WorldManager = WorldManager(this)
    val teamManager: TeamManager = TeamManager(this)

    // player functions
    val maxPlayer: Int get() = teamSize * teamAmount
    val isFull: Boolean get() = players.size >= maxPlayer

    // list of all eliminated players
    val deadPlayers: MutableList<GMAPlayer> = mutableListOf()

    // list of all alive players
    val alivePlayer: MutableList<GMAPlayer> get() = players.filter { !deadPlayers.contains(it) }.toMutableList()

    // list of all alive teams
    val aliveTeams: MutableList<Team> get() = alivePlayer.mapNotNull { it.team }.distinct().toMutableList()

    // functions to keep track of current state of the game
    var gameState: GameState = GameState.QUEUEING
        set(value) {
            GameStateChangeEvent(this, gameState, value).let {
                it.callEvent()
                if (it.isCancelled) return // stop if event has been canceled
            }

            field = value
        }
    val isRunning: Boolean get() = mutableListOf(GameState.RUNNING, GameState.STOPPING).contains(gameState)
    val isQueuing: Boolean get() = gameState == GameState.QUEUEING
    val isStarting: Boolean get() = gameState == GameState.STARTING
    val isOver: Boolean get() = mutableListOf(GameState.STOPPED, GameState.STOPPING).contains(gameState)

    fun eliminatePlayer(player: GMAPlayer) {
        if (!players.contains(player)) return
        if (!isRunning) return
        if (deadPlayers.contains(player)) return

        // add to dead player list
        deadPlayers.add(player)

        // call event
        GamePlayerEliminateEvent(player, this).let {
            it.callEvent()
            if (it.isCancelled) deadPlayers.remove(player) // stop if event has been canceled
        }
    }

    fun revivePlayer(player: GMAPlayer) {
        if (!players.contains(player)) return
        if (!isRunning) return
        if (!deadPlayers.contains(player)) return

        // call event
        GamePlayerReviveEvent(player, this).let {
            it.callEvent()
            if (it.isCancelled) return // stop if event has been canceled
        }

        // remove from dead player list
        deadPlayers.remove(player)
    }

    fun join(player: GMAPlayer): Boolean {
        if (!player.canJoin(this)) return false

        // call event
        GamePlayerJoinEvent(player, this).let {
            it.callEvent()
            if (it.isCancelled) return false // stop if event has been canceled
        }

        // add player to list
        players.add(player)

        // set player's game
        player.game = this

        if (isFull) start() // start game if it is full

        return true
    }

    fun quit(player: GMAPlayer): Boolean {
        if (!players.contains(player)) return false

        // call event
        GamePlayerQuitEvent(player, this).callEvent()

        player.kill()
        players.remove(player)
        player.game = null

        return true
    }

    fun start(): Boolean {
        if (!isQueuing) return false

        // call event
        GameStartEvent(this).let {
            it.callEvent()
            if (it.isCancelled) return false // stop if event has been canceled
        }

        gameState = GameState.STARTING

        // join random team if player is in none
        players.forEach { player ->
            if (!player.isInTeam)
                teamManager.joinRandom(player)
        }

        // load map
        worldManager.loadMap()

        // teleport players to spawn
        teamManager.teams.forEach { team ->
            worldManager.mapConfig.getTeamSpawn(team.id)?.let { spawn ->
                team.players.forEach { it.bukkitPlayer.teleport(spawn) }
            } ?: team.players.forEach { it.quitGame() }
        }

        gameState = GameState.RUNNING

        return true
    }

    fun forceStop(): Boolean {
        // call event
        GameForceStopEvent(this).callEvent()

        gameState = GameState.RUNNING
        return stop().also {
            gameState = GameState.KILLED
        }
    }

    fun stop(): Boolean {
        if (!isRunning) return false

        // call event
        val event = GameStopEvent(this)
        event.let {
            it.callEvent()
            if (it.isCancelled) return false // stop if event has been canceled
        }

        gameState = GameState.STOPPING

        // kick players so map can be unloaded
        // if an event listener sets kickPlayers to false, we just assume the external plugin takes care of removing the players so the world can be unloaded
        if (event.kickPlayers) worldManager.world?.players?.forEach { player -> player.kick(Component.text("Map reloading...")) }

        // delete world
        worldManager.removeWorld()

        gameState = GameState.STOPPED

        return true
    }

    // broadcasting to all players
    fun broadcast(message: Component) {
        GameMessageBroadcastEvent(this, message).let {
            it.callEvent()
            if (it.isCancelled) return // stop if event has been canceled
        }

        players.forEach { it.bukkitPlayer.sendMessage(message) }
    }
}