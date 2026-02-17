package de.c4vxl.gamemanager.gma.event.game

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game

/**
 * Triggered when a game map gets forced
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 * @param map The map that was forced
 */
data class GameWorldForcemapEvent(override val game: Game, val map: String?) : GameEvent(game)