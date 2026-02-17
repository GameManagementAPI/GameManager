package de.c4vxl.gamemanager.gma.player

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.language.Language
import org.bukkit.entity.Player
import java.util.UUID

/**
 * A Wrapper around the Player object.
 * This "wrapper" contains utility functions for joining or leaving games and provides metadata
 *
 * For constructing please use GMAPlayer.get or Player.gma!
 *
 * @param bukkitPlayer The bukkit player to wrap
 * @param game The current game of the player
 */
class GMAPlayer(
    val bukkitPlayer: Player,
    var game: Game? = null
) {
    companion object {
        // We want to cache the GMAPlayer instances
        // If there are multiple instances of one bukkitPlayer they might get confused and metadata will get mixed up
        // Therefore this lookup-table is used to tie one GMAPlayer-instance to the uuid of a player
        private val instances: MutableMap<UUID, GMAPlayer> = mutableMapOf()

        /**
         * Returns an instance of GMAPlayer for a bukkit player
         */
        fun get(bukkitPlayer: Player) =
            instances.getOrPut(bukkitPlayer.uniqueId) {
                GMAPlayer(bukkitPlayer)
            }

        /**
         * Returns an instance of GMAPlayer for a bukkit player
         */
        val Player.gma
            get() = get(this)
    }

    /**
     * Holds the preferred language of the player
     */
    var language: Language
        get() = Language.get(Language.getPlayerLanguage(bukkitPlayer))
        set(value) = Language.setPlayerLanguage(bukkitPlayer, value.name)

    /**
     * Returns {@code true} if the player is currently in a game
     */
    val isInGame: Boolean get() = this.game != null

    /**
     * Make a player join this game
     * @param game The game to join
     * @param force If set to {@code true} player will be forced to quit his old game in order to join this one
     * @return {@code true} upon success
     */
    fun join(game: Game, force: Boolean = false) =
        game.join(this, force)

    /**
     * Make player quit his game
     * @return {@code true} upon success
     */
    fun quit() =
        this.game?.quit(this) ?: false
}