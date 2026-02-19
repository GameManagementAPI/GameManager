package de.c4vxl.gamemanager.gma.event.player

import de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import org.bukkit.event.entity.PlayerDeathEvent

/**
 * Triggered when a player dies in a game
 * @see de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
 *
 * @param player The player that triggered the event
 * @param game The game the player is currently in
 * @param deathEvent The bukkit-triggered death event
 */
data class GamePlayerDeathEvent(override val player: GMAPlayer, val game: Game, val deathEvent: PlayerDeathEvent) : GamePlayerEvent(player)