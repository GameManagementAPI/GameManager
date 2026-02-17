package de.c4vxl.gamemanager.gma.event.game

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.world.type.Map

/**
 * Triggered after a game map has been loaded
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 * @param map The map of the game
 */
data class GameWorldLoadedEvent(override val game: Game, val map: Map) : GameEvent(game)