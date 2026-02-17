package de.c4vxl.gamemanager.gma.event.game

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game

/**
 * Triggered when a game map gets unloaded
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 */
data class GameWorldUnloadEvent(override val game: Game) : GameEvent(game)