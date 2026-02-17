package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor

/**
 * Command for starting games
 */
object StartCommand {
    val command = commandTree("start") {
        withPermission(Permission.COMMAND_START.string)
        withUsage("/start")
        withAliases("s")
        withFullDescription(Language.default.get("command.start.desc"))

        playerExecutor { player, _ ->
            val game: Game? = player.gma.game

            // Player not in a game
            if (game == null) {
                player.sendMessage(player.language.getCmp("command.start.failure.no_game"))
                return@playerExecutor
            }

            // Game already running
            if (!game.isQueuing) {
                player.sendMessage(player.language.getCmp("command.start.failure.already"))
                return@playerExecutor
            }

            // To little players
            val minPlayers = game.size.teamSize + 1
            if (game.players.size < minPlayers) {
                player.sendMessage(player.language.getCmp("command.start.failure.not_enough", minPlayers.toString()))
                return@playerExecutor
            }

            // Start game
            val success = game.start()
            if (success)
                player.sendMessage(player.language.getCmp("command.start.success"))
            else
                player.sendMessage(player.language.getCmp("command.start.failure.general"))
        }
    }
}