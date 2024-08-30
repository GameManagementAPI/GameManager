package de.c4vxl.gamemanager.gamemanagementapi.game

import java.security.SecureRandom

class GameID(val asString: String) {
    companion object {
        fun generateRandom(): GameID {
            val randomBytes = ByteArray(10)
            SecureRandom().nextBytes(randomBytes)
            return GameID(randomBytes.toString())
        }

        fun fromString(string: String): GameID {
            return GameID(string)
        }

        fun fromBytes(bytes: ByteArray): GameID {
            return GameID(bytes.toString())
        }
    }
}