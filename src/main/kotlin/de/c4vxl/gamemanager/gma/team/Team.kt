package de.c4vxl.gamemanager.gma.team

import de.c4vxl.gamemanager.gma.event.team.GameTeamMessageBroadcastEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer

/**
 * Object that holds information about one team of a game
 * @param manager The manager managing this team
 * @param id The id of this team
 * @param size The maximum amount of players that can join this team
 */
data class Team(
    val manager: TeamManager,
    val id: Int,
    val size: Int = manager.game.size.teamSize
) {
    /**
     * Holds a list of all players in this team
     */
    val players: MutableList<GMAPlayer> = mutableListOf()

    /**
     * Returns {@code true} if the team is full
     */
    val isFull: Boolean get() = players.size >= this.size

    /**
     * Returns the label of this team
     */
    val label: String get() = this.manager.game.worldManager.map?.metadata?.getString("team.$id.prefix") ?: "#${id + 1}"

    /**
     * List of players that have left the team
     */
    val playersLeft = mutableSetOf<GMAPlayer>()

    /**
     * Broadcasts a message to the entire team
     * @param key The language key of the message
     * @param args The arguments of the translation
     */
    fun broadcastMessage(key: String, vararg args: String) {
        val audience = this.players.distinct()

        // Call event
        GameTeamMessageBroadcastEvent(this, this.manager.game, key, args.toList(), audience).let {
            it.callEvent()
            if (it.isCancelled) return
        }

        // Send message
        audience.forEach { it.bukkitPlayer.sendMessage(it.language.getCmp(key, *args)) }
    }

    override fun toString(): String { return "Team { label=${this.label} }" }
    override fun hashCode(): Int { return this.id.hashCode() }
    override fun equals(other: Any?): Boolean { return this.id == (other as? Team)?.id && this.manager.game == other.manager.game }
}