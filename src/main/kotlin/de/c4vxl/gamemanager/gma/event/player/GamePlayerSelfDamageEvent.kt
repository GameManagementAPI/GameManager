package de.c4vxl.gamemanager.gma.event.player

import de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Triggered when a player damages himself whilst in a game
 * @see de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
 *
 * @param player The player that triggered the event
 * @param game The game the player is currently in
 * @param allow If set to true, the player will be allowed to damage himself
 * @param damageEvent The bukkit-triggered damage event
 */
data class GamePlayerSelfDamageEvent(
    override val player: GMAPlayer,
    val game: Game,
    var allow: Boolean,
    val damageEvent: EntityDamageByEntityEvent
) : GamePlayerEvent(player)