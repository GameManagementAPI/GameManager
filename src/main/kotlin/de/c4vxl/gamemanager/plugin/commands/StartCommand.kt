package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object StartCommand {
    init {
        val prefix: Component = GameManager.prefix

        commandTree("gamemanagementapi") {
            withFullDescription("Allows you to start a game as soon as enough players are found to fill at least two teams!")
            withPermission("c4vxl.gamemanager.perms.cmd.start")
            withUsage("/start")

            playerExecutor { player, _ ->
                val game: Game? = player.asGamePlayer.game

                if (game == null) {
                    player.sendMessage(prefix.append(Component.text("You must be in a game to perform this command!").color(
                        NamedTextColor.RED)))
                    return@playerExecutor
                }

                if (game.isQueuing) {
                    player.sendMessage(prefix.append(Component.text("The game cannot be running!").color(
                        NamedTextColor.RED)))
                    return@playerExecutor
                }

                if (game.players.size < game.teamSize + 1) {
                    player.sendMessage(prefix.append(Component.text("But there must be at least ").color(NamedTextColor.RED))
                        .append(Component.text(game.teamSize + 1).color(NamedTextColor.WHITE))
                        .append(Component.text(" players in this game in order to start it!").color(NamedTextColor.RED)))
                    return@playerExecutor
                }

                if (game.start())
                    player.sendMessage(prefix.append(Component.text("Starting game...").color(
                        NamedTextColor.GREEN)))
                else
                    player.sendMessage(prefix.append(Component.text("It seems like something went wrong!").color(
                        NamedTextColor.RED)))
            }
        }
    }
}