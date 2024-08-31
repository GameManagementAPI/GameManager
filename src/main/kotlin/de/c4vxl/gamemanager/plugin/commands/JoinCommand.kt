package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object JoinCommand {
    init {
        val prefix: Component = GameManager.prefix

        commandTree("join") {
            withFullDescription("Allows you to join into a game")
            withUsage("/join")
            withAliases("j")

            argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                GameManagementAPI.possibleGames.toTypedArray()
            })) {
                playerExecutor { player, args ->
                    if (!GameManagementAPI.possibleGames.contains(args.get("size"))) {
                        player.sendMessage(prefix.append(Component.text("This game size is not available!").color(NamedTextColor.RED)))
                        return@playerExecutor
                    }

                    val (teamAmount: Int, teamSize: Int) = args.get("size").toString().split("x").map { it.toInt() }

                    if (player.asGamePlayer.isInGame) {
                        player.sendMessage(prefix.append(
                            Component.text("You are already in a game!").color(
                                NamedTextColor.RED)).append(Component.text(" Use /quit to leave it!").color(NamedTextColor.WHITE)))
                        return@playerExecutor
                    }

                    val game = GameManagementAPI.getGame(teamAmount, teamSize)

                    if (player.asGamePlayer.joinGame(game))
                        player.sendMessage(prefix.append(
                            Component.text("Joining game...").color(
                                NamedTextColor.GREEN)))
                    else
                        player.sendMessage(prefix.append(
                            Component.text("It seems like something went wrong!").color(
                                NamedTextColor.RED)))
                }
            }
        }
    }
}