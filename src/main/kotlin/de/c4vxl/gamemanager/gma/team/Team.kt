package de.c4vxl.gamemanager.gma.team

import de.c4vxl.gamemanager.gma.event.game.GameMessageBroadcastEvent
import de.c4vxl.gamemanager.gma.event.team.GameTeamMessageBroadcastEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import net.kyori.adventure.text.Component

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
    // TODO: Implement custom team labels
    val label: String get() = "#${id + 1}"

    /**
     * Broadcasts a message to the entire team
     * @param message The message to send
     */
    fun broadcastMessage(message: Component) {
        val audience = this.players.distinct()

        // Call event
        GameTeamMessageBroadcastEvent(this, this.manager.game, message, audience).let {
            it.callEvent()
            if (it.isCancelled) return
        }

        // Send message
        audience.forEach { it.bukkitPlayer.sendMessage(message) }
    }

    override fun toString(): String {
        return "Team { label=${this.label} }"
    }
}