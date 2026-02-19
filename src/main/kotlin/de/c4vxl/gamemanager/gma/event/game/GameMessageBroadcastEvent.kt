package de.c4vxl.gamemanager.gma.event.game

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import net.kyori.adventure.text.Component

/**
 * Triggered when a message gets broadcast to a game
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 * @param message The message that was sent
 * @param audience The audience that received the message
 */
data class GameMessageBroadcastEvent(override val game: Game, val message: Component, val audience: List<GMAPlayer>) : GameEvent(game)