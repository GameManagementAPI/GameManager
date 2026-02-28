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
    private val internalPlayers: MutableSet<GMAPlayer> = mutableSetOf()
    private val internalEliminatedPlayers: MutableSet<GMAPlayer> = mutableSetOf()
    private val internalSpectators: MutableSet<GMAPlayer> = mutableSetOf()

    /**
     * Returns a list of the players connected to this game
     */
    val players: List<GMAPlayer>
        get() = internalPlayers.distinct().filter { it.game == this.game }.toList()

    /**
     * Returns a list of all players that have been eliminated
     */
    val eliminatedPlayers: List<GMAPlayer>
        get() = internalEliminatedPlayers.distinct().filter { it.game == this.game }.toList()

    /**
     * Returns a list of all players that are still alive
     */
    val alivePlayers: List<GMAPlayer>
        get() = players.filter { !it.isEliminated && it.game == this.game }

    /**
     * Returns a list of the players currently spectating this game
     */
    val spectators: List<GMAPlayer>
        get() = internalSpectators.distinct().filter { it.game == this.game }.toList()

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

        revive(player, false)

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

        // Quit spectator
        success = quitSpectator(player, callEvent)

        // Quit players
        if (this.internalPlayers.contains(player)) {
            // Remove player from game
            internalPlayers.remove(player)

            // Eliminate player
            eliminate(player, false)
            player.game = null

            // Call quit event
            if (callEvent)
                GamePlayerQuitEvent(player, this.game).let {
                    it.callEvent()
                    if (it.isCancelled) {
                        internalPlayers.add(player)
                        player.game = this.game
                        player.revive()
                        return false
                    }
                }

            // Stop spectating
            quitSpectator(player, callEvent)

            // Quit team
            this.game.teamManager.quit(player)

            // Reset scoreboard
            player.bukkitPlayer.scoreboard = Bukkit.getScoreboardManager().mainScoreboard

            success = true
        }

        return success
    }

    /**
     * Make a player quit spectating this game
     * @param player The player
     * @param callEvent If set to {@code false} no events will be triggered
     * @return {@code true} upon success
     */
    private fun quitSpectator(player: GMAPlayer, callEvent: Boolean): Boolean {
        if (!internalSpectators.contains(player))
            return false

        // Remove from spectator
        internalSpectators.remove(player)
        player.game = null

        // Reset scoreboard
        player.bukkitPlayer.scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        // Call event
        if (callEvent)
            GamePlayerSpectateEndEvent(player, this.game).callEvent()

        return true
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
     * @param callEvent If set to {@code false} no events will be triggered
     */
    fun revive(player: GMAPlayer, callEvent: Boolean = true) {
        if (player.game != this.game) return
        if (!player.isEliminated) return

        internalEliminatedPlayers.remove(player)

        // Quit spectator
        quitSpectator(player, false)

        // Call event
        if (callEvent)
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