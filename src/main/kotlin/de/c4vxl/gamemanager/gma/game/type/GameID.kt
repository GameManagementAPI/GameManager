package de.c4vxl.gamemanager.gma.game.type

import java.security.SecureRandom

/**
 * A unique identifier for each game
 */
class GameID(val asString: String) {
    companion object {
        var GAME_ID_LENGTH: Int = 5

        /**
         * Constructs a game id from its string representation
         */
        fun fromString(string: String) =
            GameID(string)

        /**
         * Constructs a game id from its bytes
         */
        fun fromBytes(bytes: ByteArray) =
            GameID(String(bytes))

        /**
         * Returns a random game id
         */
        fun random(): GameID {
            val bytes = ByteArray(GAME_ID_LENGTH)
            SecureRandom().nextBytes(bytes)
            return fromString(bytes.joinToString("") { "%02x".format(it) })
        }
    }

    override fun toString(): String {
        return this.asString
    }

    override fun hashCode(): Int {
        return this.asString.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.asString == other
    }
}