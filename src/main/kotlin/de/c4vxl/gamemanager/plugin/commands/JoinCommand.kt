package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor

/**
 * Command for joining into a game
 */
object JoinCommand {
    val command = commandTree("join") {
        withUsage("/join <size>")
        withAliases("j")
        withFullDescription(Language.default.get("command.join.desc"))

        // <size>
        argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings { GMA.possibleGameSizes.toTypedArray() })) {
            playerExecutor { player, args ->
                val sizeString = args.get("size").toString()
                val size = GameSize.fromString(sizeString)

                // Invalid size format
                if (size == null) {
                    player.sendMessage(player.language.getCmp("command.join.failure.invalid_format", sizeString))
                    return@playerExecutor
                }

                // Game size not supported
                if (!GMA.possibleGameSizes.contains(sizeString)) {
                    player.sendMessage(player.language.getCmp("command.join.failure.invalid_size", sizeString))
                    return@playerExecutor
                }

                if (player.gma.isInGame) {
                    player.sendMessage(player.language.getCmp("command.join.failure.already"))
                    return@playerExecutor
                }

                // Get
                val game = GMA.getOrCreate(size)

                // Join
                val success = player.gma.join(game)

                if (success)
                    player.sendMessage(player.language.getCmp("command.join.success"))
                else
                    player.sendMessage(player.language.getCmp("command.join.failure.general"))
            }
        }
    }
}