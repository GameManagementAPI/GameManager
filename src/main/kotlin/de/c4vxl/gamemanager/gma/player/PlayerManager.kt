package de.c4vxl.gamemanager.gma.player

import de.c4vxl.gamemanager.gma.event.player.*
import de.c4vxl.gamemanager.gma.game.Game
import org.bukkit.Bukkit
import org.bukkit.GameMode

/**
 * Object responsible for managing players of a game
 * @param game The game to manage the players of
 */
class PlayerManager(
    val game: Game
) {
    private val internalPlayers: MutableList<GMAPlayer> = mutableListOf()
    private val internalEliminatedPlayers: MutableList<GMAPlayer> = mutableListOf()
    private val internalSpectators: MutableList<GMAPlayer> = mutableListOf()

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
        get() = players.filter { !it.isEliminated && it.game == this.game }

    /**
     * Returns a list of the players currently spectating this game
     */
    val spectators: List<GMAPlayer>
        get() = internalSpectators.distinct().toList()

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
     * @param callEvent If set to {@code false} no events will be triggered
     * @return {@code true} upon success
     */
    fun quit(player: GMAPlayer, callEvent: Boolean = true): Boolean {
        var success = false

        // Player is spectator
        if (spectators.contains(player)) {
            // Remove from spectator
            internalSpectators.remove(player)
            player.game = null

            // Reset scoreboard
            player.bukkitPlayer.scoreboard = Bukkit.getScoreboardManager().mainScoreboard

            // Call event
            if (callEvent)
                GamePlayerSpectateEndEvent(player, this.game).callEvent()

            success = true
        }

        // Player is game player
        if (this.players.contains(player)) {
            // Call quit event
            if (callEvent)
                GamePlayerQuitEvent(player, this.game).let {
                    it.callEvent()
                    if (it.isCancelled) return false
                }

            // Eliminate player
            eliminate(player, false)

            // Quit team
            this.game.teamManager.quit(player)

            // Remove player from game
            internalPlayers.removeAll { it.bukkitPlayer.uniqueId == player.bukkitPlayer.uniqueId }
            player.game = null

            // Reset scoreboard
            player.bukkitPlayer.scoreboard = Bukkit.getScoreboardManager().mainScoreboard

            success = true
        }

        return success
    }

    /**
     * Make a player spectate this game
     * @param player The player to put in spectator mode
     * @param force If set to {@code true} the player will quit his old game to spectate
     * @return {@code true} upon success
     */
    fun spectate(player: GMAPlayer, force: Boolean = false): Boolean {
        // Player is already in a game that isn't this one
        if (player.isInGame && player.game != this.game && !force)
            return false

        // Quit past game
        // Don't call events here to prevent misconception of player intending to "quit" his game
        // just because he started spectating
        this.quit(player, false)

        // Return if player is already spectating
        if (isSpectating(player)) return false

        // Add player to spectators
        internalSpectators.add(player)
        player.game = this.game

        // Set game-mode to spectator
        player.bukkitPlayer.gameMode = GameMode.SPECTATOR
        player.bukkitPlayer.scoreboard = game.scoreboard
        this.game.worldManager.map?.world?.spawnLocation?.let { player.bukkitPlayer.teleport(it) }

        // Call event
        GamePlayerSpectateStartEvent(player, this.game).callEvent()

        return true
    }

    /**
     * Returns {@code true} if a player is spectating this game
     * @param player The player to check
     */
    fun isSpectating(player: GMAPlayer): Boolean =
        this.internalSpectators.contains(player)


    /**
     * Eliminates a player from the game
     * @param player The player to eliminate
     * @param spectate If set to {@code true} player will be put in spectator
     */
    fun eliminate(player: GMAPlayer, spectate: Boolean = true) {
        if (player.game != this.game) return
        if (player.isEliminated) return

        internalEliminatedPlayers.add(player)

        // Call event
        GamePlayerEliminateEvent(player, this.game).let {
            it.callEvent()
            if (it.isCancelled) internalEliminatedPlayers.remove(player)
        }

        // Set to spectator
        if (spectate)
            this.spectate(player)
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