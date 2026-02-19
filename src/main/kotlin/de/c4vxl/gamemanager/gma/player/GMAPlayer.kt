package de.c4vxl.gamemanager.gma.player

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.language.Language
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.util.*

/**
 * A Wrapper around the Player object.
 * This "wrapper" contains utility functions for joining or leaving games and provides metadata
 *
 * For constructing please use GMAPlayer.get or Player.gma!
 *
 * @param player The bukkit player to wrap
 * @param uuid The uuid of the bukkit player to wrap
 */
class GMAPlayer(
    private val player: Player,
    private val uuid: UUID = player.uniqueId
) {
    companion object {
        // We want to cache the GMAPlayer instances
        // If there are multiple instances of one bukkitPlayer they might get confused and metadata will get mixed up
        // Therefore this lookup-table is used to tie one GMAPlayer-instance to the uuid of a player
        private val instances: MutableMap<Player, GMAPlayer> = mutableMapOf()

        /**
         * Returns an instance of GMAPlayer for a bukkit player
         */
        fun get(bukkitPlayer: Player) =
            instances.getOrPut(bukkitPlayer) {
                GMAPlayer(bukkitPlayer)
            }

        /**
         * Removes a player from the cache
         */
        fun unregister(bukkitPlayer: Player) =
            instances.remove(bukkitPlayer)

        /**
         * Returns an instance of GMAPlayer for a bukkit player
         */
        val Player.gma
            get() = get(this)
    }

    /**
     * Returns the bukkit player wrapped by this GMAPlayer
     */
    val bukkitPlayer: Player
        get() = Bukkit.getPlayer(this.uuid) ?: player

    /**
     * Holds the preferred language of the player
     */
    var language: Language
        get() {
            var language = Language.get(Language.getPlayerLanguage(bukkitPlayer))
            if (language == null) {
                this@GMAPlayer.language = Language.default
                language = Language.default
            }

            return language
        }
        set(value) = Language.setPlayerLanguage(bukkitPlayer, value.name)

    /**
     * Holds the current game of the player
     */
    var game: Game? = null

    /**
     * Returns {@code true} if the player is currently in a game
     */
    val isInGame: Boolean get() = this.game != null

    /**
     * Returns the team the player is currently a participant of
     * This might be null depending on if the player is even part of a game or is in a team
     */
    val team: Team? get() = this.game?.teamManager?.get(this)

    /**
     * Returns {@code true} if the player is currently in a team
     */
    val isInTeam: Boolean get() = this.team != null

    val isEliminated: Boolean get() = this.game?.playerManager?.isEliminated(this) == true

    /**
     * Make a player join this game
     * @param game The game to join
     * @param force If set to {@code true} player will be forced to quit his old game in order to join this one
     * @return {@code true} upon success
     */
    fun join(game: Game, force: Boolean = false) =
        game.playerManager.join(this, force)

    /**
     * Make player quit his game
     * @return {@code true} upon success
     */
    fun quit() =
        this.game?.playerManager?.quit(this) ?: false

    /**
     * Eliminate this player from the game he is in
     * @return {@code true} upon success
     */
    fun eliminate() =
        this.game?.playerManager?.eliminate(this) ?: false

    /**
     * Revive this player in the game he is in
     * @return {@code true} upon success
     */
    fun revive() =
        this.game?.playerManager?.revive(this) ?: false

    /**
     * Resets common player data such as experience, inventory, health, etc
     */
    fun reset() {
        this.bukkitPlayer.let {
            it.isFlying = false
            it.exp = 0F
            it.totalExperience = 0
            it.level = 0
            it.saturation = 20f
            it.inventory.clear()
            it.enderChest.clear()
            it.activePotionEffects.forEach { effect -> it.removePotionEffect(effect.type) }
            it.fireTicks = 0
            it.resetCooldown()
            it.resetMaxHealth()
            it.health = it.maxHealth
            it.gameMode = GameMode.SURVIVAL
            it.fallDistance = 0F
        }
    }


    override fun equals(other: Any?): Boolean {
        val gma = (other as? GMAPlayer) ?: return false
        return gma.bukkitPlayer.uniqueId == this.bukkitPlayer.uniqueId
    }

    override fun hashCode(): Int {
        return this.bukkitPlayer.uniqueId.hashCode()
    }

    override fun toString(): String {
        return "GMAPlayer { name=${this.bukkitPlayer.name}, uuid=${this.bukkitPlayer.uniqueId}, game=${this.game}, team=${this.team}, isEliminated=${this.isEliminated} }"
    }
}