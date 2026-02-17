package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import org.bukkit.entity.Player

/**
 * Command for force-setting the map of a game
 */
object ForcemapCommand  {
    val command = commandTree("forcemap") {
        withPermission(Permission.COMMAND_START.string)
        withUsage("/forcemap <map>")
        withAliases("fm")
        withFullDescription(Language.default.get("command.forcemap.desc"))

        argument(StringArgument("map").replaceSuggestions(ArgumentSuggestions.strings {
            (it.sender as? Player)?.gma?.game?.worldManager?.availableMaps?.toTypedArray() ?: arrayOf()
        })) {
            playerExecutor { player, args ->
                val game: Game? = player.gma.game
                val map = args.get("map").toString()

                // Player not in a game
                if (game == null) {
                    player.sendMessage(player.language.getCmp("command.forcemap.failure.no_game"))
                    return@playerExecutor
                }

                // Game already running
                if (!game.isQueuing) {
                    player.sendMessage(player.language.getCmp("command.forcemap.failure.already_running"))
                    return@playerExecutor
                }

                // Invalid map
                if (!game.worldManager.availableMaps.contains(map)) {
                    player.sendMessage(player.language.getCmp("command.forcemap.failure.invalid_map"))
                    return@playerExecutor
                }

                // Already forced
                if (game.worldManager.forcemap != null) {
                    player.sendMessage(player.language.getCmp("command.forcemap.failure.already", game.worldManager.forcemap!!))
                    return@playerExecutor
                }

                // Force map
                game.worldManager.forcemap = map
                player.sendMessage(player.language.getCmp("command.forcemap.success", map))
            }
        }
    }
}