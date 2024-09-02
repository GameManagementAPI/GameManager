package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.game.GameID
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object SpectateCommand {
    init {
        val prefix: Component = GameManager.prefix
        val sorry: Component = Component.text("I am sorry! ").color(NamedTextColor.RED)

        commandTree("spec") {
            withFullDescription("Allows you to spectate a game")
            withUsage("/spec <player/game> <playername/gameid>")
            withAliases("s")

            literalArgument("player") {
                argument(StringArgument("playername").replaceSuggestions(ArgumentSuggestions.strings { sender ->
                    Bukkit.getOnlinePlayers().filter { it.asGamePlayer.game?.isRunning == true }.map { it.name }.filter { it != sender.sender.name }
                        .toTypedArray()
                })) {
                    playerExecutor { player, args ->
                        val playerName = args.get("playername").toString()
                        val target: Player? = Bukkit.getOnlinePlayers().firstOrNull { it.name.equals(playerName, ignoreCase = true) }
                        val game: Game? = target?.asGamePlayer?.game

                        if (target == null) {
                            player.sendMessage(
                                prefix.append(sorry).append(
                                    Component.text("But this player is not online right now!").color(
                                        NamedTextColor.WHITE
                                    )
                                )
                            )
                            return@playerExecutor
                        }

                        if (game == null) {
                            player.sendMessage(
                                prefix.append(sorry).append(
                                    Component.text("${target.name} is not a player in any game!").color(
                                        NamedTextColor.WHITE
                                    )
                                )
                            )
                            return@playerExecutor
                        }

                        player.asGamePlayer.spectate(game)
                    }
                }
            }

            literalArgument("game") {
                argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                    GameManagementAPI.games.filter { it.isRunning }.map { it.id.asString }.toTypedArray()
                })) {
                    playerExecutor { sender, args ->
                        val gameID = GameID.fromString(args.get("gameid").toString())
                        val game: Game? = GameManagementAPI.getGame(gameID)

                        if (game == null) {
                            sender.sendMessage(prefix.append(sorry).append(Component.text("But this game does not exist!").color(NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        if (game.isPrivate && sender.asGamePlayer.game != game) {
                            sender.sendMessage(prefix.append(sorry).append(Component.text("But you cannot spectate private games!").color(NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        sender.asGamePlayer.spectate(game)
                    }
                }
            }
        }
    }
}