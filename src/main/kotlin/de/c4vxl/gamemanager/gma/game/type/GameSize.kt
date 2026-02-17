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
    companion object {
        /**
         * Constructs a GameSize-object from its string representation
         */
        fun fromString(string: String): GameSize? {
            val parts = string.split("x")
            val amount = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val size = parts.getOrNull(1)?.toIntOrNull() ?: return null

            return GameSize(amount, size)
        }
    }

    /**
     * The maximal amount of players a game of this size can contain
     */
    val maxPlayers: Int = teamAmount * teamSize

    /**
     * Converts size into string representation
     */
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