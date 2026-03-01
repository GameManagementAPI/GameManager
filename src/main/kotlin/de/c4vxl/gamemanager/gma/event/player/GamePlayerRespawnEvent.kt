package de.c4vxl.gamemanager.gma.event.player

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import org.bukkit.Location

/**
 * Triggered when a player respawns in a game
 * @see de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
 *
 * @param player The player that triggered the event
 * @param game The game the player is currently in
 * @param killer The player that killed the player
 * @param spawnLocation The respawn location of the player
 * @param respawnEvent The parent bukkit-event
 */
data class GamePlayerRespawnEvent(override val player: GMAPlayer, val game: Game, val killer: GMAPlayer?, val spawnLocation: Location, val respawnEvent: PlayerPostRespawnEvent) : GamePlayerEvent(player)