package de.c4vxl.gamemanager.gma.event.type

import de.c4vxl.gamemanager.gma.player.GMAPlayer

/**
 * A GMAEvent that is directly tied to a specific player
 */
open class GamePlayerEvent(open val player: GMAPlayer) : GMAEvent()