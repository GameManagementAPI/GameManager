package de.c4vxl.gamemanager.gma.event.type

import de.c4vxl.gamemanager.gma.game.Game

/**
 * A GMAEvent that is directly tied to a specific game
 */
open class GameEvent(open val game: Game) : GMAEvent()