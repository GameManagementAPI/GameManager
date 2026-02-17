package de.c4vxl.gamemanager.gma.event.team

import de.c4vxl.gamemanager.gma.event.type.GameTeamEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.team.Team

/**
 * Triggered when a player joins a team
 * @see de.c4vxl.gamemanager.gma.event.type.GameTeamEvent
 *
 * @param team The team the player joined
 * @param player The player that triggered the event
 * @param game The game the player is currently in
 */
data class GamePlayerTeamJoinEvent(override val team: Team, val player: GMAPlayer, val game: Game) : GameTeamEvent(team)