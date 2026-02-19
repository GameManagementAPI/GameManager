package de.c4vxl.gamemanager.gma.event.privateGame

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer

/**
 * Triggered when a player is invited to a private game
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 * @param owner The owner of the private game
 * @param player The player invited
 */
data class PrivateGamePlayerInviteEvent(override val game: Game, val owner: GMAPlayer, val player: GMAPlayer) : GameEvent(game)