package de.c4vxl.gamemanager.gma.game.type

/**
 * Holds the size of a game
 * @param teamAmount The amount of teams
 * @param teamSize The amount of players per team
 */
class GameSize(
    val teamAmount: Int,
    val teamSize: Int
) {
    override fun toString(): String {
        return "${teamAmount}x$teamSize"
    }

    /**
     * Returns 'true' if game size matches the passed size
     * @param teamAmount The amount of teams
     * @param teamSize The amount of players per team
     */
    fun equals(teamAmount: Int, teamSize: Int) =
        teamAmount == this.teamAmount
            && teamSize == this.teamSize
}