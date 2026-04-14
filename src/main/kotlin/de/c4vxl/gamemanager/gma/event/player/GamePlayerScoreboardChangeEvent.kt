package de.c4vxl.gamemanager.gma.event.player

import de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import org.bukkit.scoreboard.Scoreboard

/**
 * Triggered when a players scoreboard changes
 * @see de.c4vxl.gamemanager.gma.event.type.GamePlayerEvent
 *
 * @param player The player that triggered the event
 * @param game The game the player is currently in
 * @param from The initial scoreboard
 * @param to The new scoreboard
 */
data class GamePlayerScoreboardChangeEvent(
    override val player: GMAPlayer,
    val game: Game?,
    val from: Scoreboard,
    val to: Scoreboard
) : GamePlayerEvent(player)