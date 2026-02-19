package de.c4vxl.gamemanager.gma.event.team

import de.c4vxl.gamemanager.gma.event.type.GameTeamEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.team.Team
import net.kyori.adventure.text.Component

/**
 * Triggered when a message gets broadcast to a team
 * @see de.c4vxl.gamemanager.gma.event.type.GameTeamEvent
 *
 * @param team The team the message got sent to
 * @param game The game the team belongs to
 * @param message The message sent
 * @param audience The audience that received the message
 */
data class GameTeamMessageBroadcastEvent(override val team: Team, val game: Game, val message: Component, val audience: List<GMAPlayer>) : GameTeamEvent(team)