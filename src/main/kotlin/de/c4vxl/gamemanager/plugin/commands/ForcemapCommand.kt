package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import de.c4vxl.gamemanager.gamemanagementapi.world.WorldManager
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.io.File

object ForcemapCommand {
    init {
        val prefix: Component = GameManager.prefix

        commandTree("forcemap") {
            withFullDescription("Allows you to set the map of a game")
            withUsage("/forcemap <map>")
            withPermission("c4vxl.gamemanager.perms.cmd.forcemap")
            withAliases("fm")

            argument(StringArgument("map").replaceSuggestions(ArgumentSuggestions.strings { sender ->
                // Cast sender to Player and retrieve the game
                val player = sender as? Player ?: return@strings arrayOf<String>()
                val game: Game = player.asGamePlayer.game ?: return@strings arrayOf()
                return@strings File(WorldManager.mapsContainerPath, game.gameSize).list()
            })) {
                playerExecutor { player, args ->
                    val game: Game? = player.asGamePlayer.game

                    if (game == null) {
                        player.sendMessage(prefix.append(
                            Component.text("You must be in a game to perform this command!").color(
                                NamedTextColor.RED)))
                        return@playerExecutor
                    }

                    if (!game.isQueuing) {
                        player.sendMessage(prefix.append(
                            Component.text("You cannot run this command outside of the game queue!").color(
                                NamedTextColor.RED)))
                        return@playerExecutor
                    }

                    val map: String = args.get("map").toString()

                    if (game.worldManager.forcemap != null) {
                        player.sendMessage(prefix.append(
                            Component.text("It seems like this game has already a map forced!").color(
                                NamedTextColor.RED)))
                        return@playerExecutor
                    }

                    if (!game.worldManager.availableMaps.contains(map)) {
                        player.sendMessage(prefix.append(
                            Component.text("This map does not exist!").color(
                                NamedTextColor.RED)))
                        return@playerExecutor
                    }

                    game.worldManager.forcemap = map
                    player.sendMessage(prefix.append(
                        Component.text("Successfully set the map.").color(
                            NamedTextColor.GREEN)))
                }
            }
        }
    }
}