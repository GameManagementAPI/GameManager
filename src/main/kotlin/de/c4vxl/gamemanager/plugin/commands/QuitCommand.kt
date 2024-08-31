package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object QuitCommand {
    init {
        val prefix: Component = GameManager.prefix

        commandTree("quit") {
            withFullDescription("Allows you to quit your current game")
            withUsage("/quit")

            playerExecutor { player, _ ->
                val game: Game? = player.asGamePlayer.game

                if (game == null) {
                    player.sendMessage(prefix.append(
                        Component.text("You must be in a game to perform this command!").color(
                        NamedTextColor.RED)))
                    return@playerExecutor
                }

                if (game.quit(player.asGamePlayer))
                    player.sendMessage(prefix.append(
                        Component.text("Leaving game...").color(
                        NamedTextColor.GREEN)))
                else
                    player.sendMessage(prefix.append(
                        Component.text("It seems like something went wrong!").color(
                        NamedTextColor.RED)))
            }
        }
    }
}