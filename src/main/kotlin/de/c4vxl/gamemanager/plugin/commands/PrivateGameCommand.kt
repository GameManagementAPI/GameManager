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
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PrivateGameCommand {
    val invites: MutableMap<String, MutableList<Player>> = mutableMapOf()

    init {
        val prefix: Component = GameManager.prefix
        val sorry: Component = Component.text("I am sorry! ").color(NamedTextColor.RED)

        commandTree("privategame") {
            withFullDescription("Allows you to create a private game, where only people you invite can join!")
            withUsage("/privategame <size>")
            withAliases("pg")

            literalArgument("create") {
                withRequirement { sender ->
                    sender.hasPermission("c4vxl.gamemanager.perms.cmd.privategame")
                }

                argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                    GameManagementAPI.possibleGames.toTypedArray()
                })) {
                    playerExecutor { player, args ->
                        if (!GameManagementAPI.possibleGames.contains(args.get("size"))) {
                            player.sendMessage(prefix.append(sorry).append(Component.text("But this game size is not available!").color(
                                NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        if (player.asGamePlayer.isInGame) {
                            player.sendMessage(prefix.append(sorry).append(Component.text("But you must not be in a game in order to execute this command!").color(
                                NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        val (teamAmount: Int, teamSize: Int) = args.get("size").toString().split("x").map { it.toInt() }

                        val game = GameManagementAPI.createPrivateGame(teamAmount, teamSize, player.asGamePlayer)

                        if (game.join(player.asGamePlayer)) player.sendMessage(prefix.append(Component.text("Successfully created private game!").color(
                            NamedTextColor.GREEN)))
                        else player.sendMessage(prefix.append(sorry).append(Component.text("It seems like something went wrong!").color(
                            NamedTextColor.WHITE)))
                    }
                }
            }

            literalArgument("invite") {
                withRequirement { sender ->
                    sender.hasPermission("c4vxl.gamemanager.perms.cmd.privategame")
                }

                argument(StringArgument("target").replaceSuggestions(ArgumentSuggestions.strings { sender ->
                    Bukkit.getOnlinePlayers().map { it.name }.filter { it != sender.sender.name }.toTypedArray()
                })) {
                    playerExecutor { player, args ->
                        val target: Player? = Bukkit.getPlayer(args.get("target").toString())
                        val game: Game? = player.asGamePlayer.game

                        if (target == null) {
                            player.sendMessage(prefix.append(sorry).append(Component.text("But this player is not online right now!").color(NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        if (target == player) {
                            player.sendMessage(prefix.append(sorry).append(Component.text("But you cannot invite yourself!").color(NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        if (game == null) {
                            player.sendMessage(prefix.append(sorry).append(Component.text("But you must be in a game to run this command!").color(NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        if (!game.isPrivate && game.owner == player.asGamePlayer) {
                            player.sendMessage(prefix.append(sorry).append(Component.text("You can only run this command in a private game you own!").color(NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        // add to invites map
                        invites[game.id.asString] = invites.getOrPut(game.id.asString) { mutableListOf() }.apply {
                            this.add(target)
                        }

                        target.sendMessage(Component.text("")
                            .append(
                                Component.text("${player.name} ")
                                    .color(NamedTextColor.GRAY)
                                    .decorate(TextDecoration.BOLD)
                            )
                            .append(
                                Component.text("has invited you to his/her private game! ")
                                    .color(NamedTextColor.GREEN)
                            )
                            .append(
                                Component.text("[CLICK HERE TO JOIN]")
                                    .color(NamedTextColor.WHITE)
                                    .clickEvent(ClickEvent.runCommand("/privategame join ${game.id.asString}"))
                            ))

                        player.sendMessage(prefix.append(Component.text("Successfully invited ${target.name}").color(NamedTextColor.GREEN)))
                    }
                }
            }

            literalArgument("join") {
                argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { sender ->
                    val player = sender.sender as? Player ?: return@strings mutableListOf<String>().toTypedArray()

                    val out = mutableListOf<String>()
                    invites.forEach { t, u -> if (u.contains(player)) out.add(t) }
                    return@strings out.toTypedArray()
                })) {
                    playerExecutor { player, args ->
                        val gameID = GameID.fromString(args.get("gameid").toString())
                        val game: Game? = GameManagementAPI.getGame(gameID)

                        if (!invites.getOrDefault(gameID.asString, mutableListOf()).contains(player)) {
                            player.sendMessage(prefix.append(sorry).append(Component.text("But you are not invited to this game!").color(NamedTextColor.WHITE)))
                            return@playerExecutor
                        }

                        // join
                        game?.join(player.asGamePlayer)
                    }
                }
            }
        }
    }
}