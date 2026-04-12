package de.c4vxl.gamemanager.gma.event.team

import de.c4vxl.gamemanager.gma.event.type.GameTeamEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.team.Team
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Triggered when a player tries to damage a team member
 * @see de.c4vxl.gamemanager.gma.event.type.GameTeamEvent
 *
 * @param team The team the player joined
 * @param player The player that damaged a team member in the event
 * @param damaged The player that was damaged
 * @param allow If set to true, the event will be allowed
 * @param game The game the player is currently in
 */
data class GamePlayerFriendlyFireEvent(
    override val team: Team,
    val player: GMAPlayer,
    val damaged: GMAPlayer,
    val game: Game,
    var allow: Boolean,
    val damageEvent: EntityDamageByEntityEvent
) : GameTeamEvent(team)