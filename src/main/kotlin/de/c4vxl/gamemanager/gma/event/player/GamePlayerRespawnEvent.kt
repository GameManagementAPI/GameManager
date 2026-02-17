package de.c4vxl.gamemanager.gma.event.player

import de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer

/**
 * Triggered when a player respawns in a game
 * @see de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
 *
 * @param player The player that triggered the event
 * @param game The game the player is currently in
 * @param isEliminated If set to {@code true} the player will be eliminated upon respawn
 */
data class GamePlayerRespawnEvent(override val player: GMAPlayer, val game: Game, var isEliminated: Boolean) : GamePlayerEvent(player)