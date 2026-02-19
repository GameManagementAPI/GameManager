package de.c4vxl.gamemanager.gma.event.game

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer

/**
 * Triggered when a message gets broadcast to a game
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 * @param translationKey The translation key of the message that was sent
 * @param translationArgs The arguments to the translation
 * @param audience The audience that received the message
 */
data class GameMessageBroadcastEvent(override val game: Game, val translationKey: String, val translationArgs: List<String>, val audience: List<GMAPlayer>) : GameEvent(game)