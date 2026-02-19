package de.c4vxl.gamemanager.gma.event.game

import de.c4vxl.gamemanager.gma.event.type.GameEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.team.Team

/**
 * Triggered when a game ends
 * @see de.c4vxl.gamemanager.gma.event.type.GameEvent
 *
 * @param game The game that triggered the event
 */
data class GameEndEvent(override val game: Game, val winnerTeam: Team?, val teamsLost: List<Team>) : GameEvent(game)