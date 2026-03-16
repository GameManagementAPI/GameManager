package de.c4vxl.gamemanager.gma.event.player

import de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer

/**
 * Listen to this to equip a player with default items at the start of a game
 * @see de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
 *
 * @param player The player to equip
 * @param game The game the player is currently in
 * @param reason The reason for the event
 */
data class GamePlayerEquipEvent(override val player: GMAPlayer, val game: Game, val reason: Reason) : GamePlayerEvent(player) {
    enum class Reason {
        GAME_START,
        REVIVE,
        RESPAWN
    }
}