package de.c4vxl.gamemanager.gma.event.game

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game

/**
 * Triggered when a game stops
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 * @param kickPlayers If set to {@code true} players connected to the game world will be kicked to allow for unloading it. If you set this to false, you need to take care of removing the player from the world otherwise.
 */
data class GameStopEvent(override val game: Game, var kickPlayers: Boolean = true) : GameEvent(game)