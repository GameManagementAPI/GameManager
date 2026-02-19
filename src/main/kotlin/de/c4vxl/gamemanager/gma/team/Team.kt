package de.c4vxl.gamemanager.gma.team

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
    // TODO: Implement custom team labels
    val label: String get() = "#${id + 1}"

    override fun toString(): String {
        return "Team { label=${this.label} }"
    }
}