package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor

/**
 * Command for displaying the currently loaded map
 */
object MapCommand {
    val command = commandTree("map") {
        withUsage("/map")
        withAliases("m")
        withFullDescription(Language.default.get("command.map.desc"))

        playerExecutor { player, _ ->
            val game: Game? = player.gma.game
            val map = game?.worldManager?.map?.name ?: game?.worldManager?.forcemap
            val builders = game?.worldManager?.map?.builders

            // Player not connected to a game
            if (game == null) {
                player.sendMessage(player.language.getCmp("command.map.failure.no_game"))
                return@playerExecutor
            }

            // Game not running
            if (!game.isRunning) {
                player.sendMessage(player.language.getCmp("command.map.failure.not_running"))
                return@playerExecutor
            }

            // Map null (somehow)
            if (map == null) {
                player.sendMessage(player.language.getCmp("command.map.failure.no_map"))
                return@playerExecutor
            }

            // Send info
            player.sendMessage(
                player.language.getCmp("command.map.msg.l1")
                    .appendNewline()
                    .append(player.language.getCmp("command.map.msg.l2", map))
                    .appendNewline()
                    .append(player.language.getCmp("command.map.msg.l3", builders?.joinToString(", ") ?: "/"))
            )
        }
    }
}