package de.c4vxl.gamemanager.gma.event.type

import de.c4vxl.gamemanager.gma.team.Team

/**
 * A GMAEvent that is directly tied to a specific team
 */
open class GameTeamEvent(open val team: Team) : GMAEvent()