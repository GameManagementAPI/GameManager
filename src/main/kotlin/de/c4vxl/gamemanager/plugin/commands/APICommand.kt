package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.game.GameID
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object APICommand {
    fun getGameStateComponent(game: Game): Component {
        return Component.text(when {game.isQueuing -> "■ Queuing"; game.isRunning -> "■ Running"; else -> "■ Stopped" })
            .color(when {game.isQueuing -> NamedTextColor.YELLOW; game.isRunning -> NamedTextColor.GREEN; else -> NamedTextColor.RED })
    }

    init {
        val prefix: Component = GameManager.prefix
        val sorry: Component = Component.text("I am sorry! ").color(NamedTextColor.RED)


        /**
         * Subcommands:
         * /gamemanagementapi
         */

        commandTree("gamemanagementapi") {
            withFullDescription("Allows you to interact directly with the gamemanagement api")
            withPermission("c4vxl.gamemanager.perms.cmd.gamemanager")
            withUsage("/gamemanager <options>")
            withAliases("gma", "gamemanagementapi")

            literalArgument("games") {
                // games list
                // sends a list of all games
                literalArgument("list") {
                    anyExecutor { sender, _ ->
                        if (GameManagementAPI.games.isNotEmpty()) sender.sendMessage(prefix
                            .append(Component.text("Currently registered games:")))
                            .also {
                                GameManagementAPI.games.forEach { game ->
                                    sender.sendMessage(Component.text("- ${game.id.asString} | ${game.gameSize} | ")
                                        .append(getGameStateComponent(game))
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy game id")))
                                        .clickEvent(ClickEvent.copyToClipboard(game.id.asString)))
                                }
                            }
                        else {
                            sender.sendMessage(prefix.append(sorry).append(Component.text("But there are no registered games!").color(NamedTextColor.WHITE)))
                        }
                    }
                }

                // games create
                // creates and registers a game
                literalArgument("create") {
                    argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        GameManagementAPI.possibleGames.toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            if (!GameManagementAPI.possibleGames.contains(args.get("size"))) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this game size is not available!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }

                            val (teamAmount: Int, teamSize: Int) = args.get("size").toString().split("x").map { it.toInt() }

                            GameManagementAPI.createGame(teamAmount, teamSize) // create game
                            sender.sendMessage(prefix.append(Component.text("The game has been created successfully!").color(NamedTextColor.GREEN)))
                        }
                    }
                }

                // games start
                // starts a game
                literalArgument("start") {
                    argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        GameManagementAPI.games.filter { it.isQueuing }.map { it.id.asString }.toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val gameID = GameID.fromString(args.get("gameid").toString())
                            val game: Game? = GameManagementAPI.getGame(gameID)

                            if (game == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this game does not exist!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }


                            // TODO: uncomment this after testing
//                            if (game.players.size < game.teamSize + 1) {
//                                sender.sendMessage(prefix.append(sorry).append(Component.text("But there must be at least ").color(NamedTextColor.WHITE))
//                                    .append(Component.text(game.teamSize + 1).color(NamedTextColor.GRAY))
//                                    .append(Component.text(" players in this game in order to start it!").color(NamedTextColor.WHITE)))
//                                return@anyExecutor
//                            }

                            game.start()
                            sender.sendMessage(prefix.append(Component.text("The game has been started successfully!").color(NamedTextColor.GREEN)))
                        }
                    }
                }

                // games stop
                // stops a game
                literalArgument("stop") {
                    argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        GameManagementAPI.games.filter { it.isRunning }.map { it.id.asString }.toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val gameID = GameID.fromString(args.get("gameid").toString())
                            val game: Game? = GameManagementAPI.getGame(gameID)

                            if (game?.stop() == true)
                                sender.sendMessage(prefix.append(Component.text("The game has been stopped successfully!").color(NamedTextColor.GREEN)))
                            else
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this game does not exist!").color(NamedTextColor.WHITE)))
                        }
                    }
                }

                // games info
                // sends information about a game
                literalArgument("info") {
                    argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        GameManagementAPI.games.map { it.id.asString }.toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val gameID = GameID.fromString(args.get("gameid").toString())
                            val game: Game? = GameManagementAPI.getGame(gameID)

                            if (game == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this game does not exist!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }

                            sender.sendMessage(prefix.append(Component.text("Information about ").color(NamedTextColor.GREEN)
                                    .append(Component.text(gameID.asString).color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD)
                                            .hoverEvent(HoverEvent.showText(Component.text("Click to copy game id")))
                                            .clickEvent(ClickEvent.copyToClipboard(gameID.asString))))
                                    .appendNewline()
                                .append(Component.text("${game.gameSize} | ${game.players.size}/${game.maxPlayer} Players | ").append(getGameStateComponent(game))))
                        }
                    }
                }

                // games players
                // gives a list of all players and their teams
                literalArgument("listplayers") {
                    argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        GameManagementAPI.games.map { it.id.asString }.toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val gameID = GameID.fromString(args.get("gameid").toString())
                            val game: Game? = GameManagementAPI.getGame(gameID)

                            if (game == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this game does not exist!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }

                            if (game.players.isEmpty()) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("${game.id.asString} does not have any players!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }

                            sender.sendMessage(prefix.append(Component.text("Players of ${game.id.asString}"))).also {
                                game.players.forEach { player ->
                                    sender.sendMessage(Component.text("- ${player.bukkitPlayer.name} (Team: ${player.team?.id?.let { game.worldManager.mapConfig.getTeamName(it) } ?: "None"})"))
                                }
                            }
                        }
                    }
                }

                // games join
                // join into a game
                literalArgument("join") {
                    argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        GameManagementAPI.games.filter { it.isQueuing }.map { it.id.asString }.toTypedArray()
                    })) {
                        playerExecutor { player, args ->
                            val gameID = GameID.fromString(args.get("gameid").toString())
                            val game: Game? = GameManagementAPI.getGame(gameID)

                            if (game == null) {
                                player.sendMessage(prefix.append(sorry).append(Component.text("But this game does not exist!").color(NamedTextColor.WHITE)))
                                return@playerExecutor
                            }

                            if (player.asGamePlayer.joinGame(game))
                                player.sendMessage(prefix.append(Component.text("Successfully joined the game!").color(NamedTextColor.GREEN)))
                            else
                                player.sendMessage(prefix.append(sorry).append(Component.text("It seems like something went wrong!").color(NamedTextColor.WHITE)))
                        }
                    }
                }
            }

            literalArgument("player") {
                argument(StringArgument("playername").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                    Bukkit.getOnlinePlayers().map { it.name }.toTypedArray()
                })) {
                    // player getgame
                    // sends the game of a player
                    literalArgument("getgame") {
                        anyExecutor { sender, args ->
                            val player: Player? = Bukkit.getPlayer(args.get("playername").toString())
                            val game: Game? = player?.asGamePlayer?.game

                            if (player == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this player is not online right now!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }

                            if (game == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("${player.name} is not a player in any game!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }

                            player.sendMessage(prefix.append(Component.text("${player.name} currently plays in ${game.id.asString}")
                                .hoverEvent(HoverEvent.showText(Component.text("Click to copy game id")))
                                .clickEvent(ClickEvent.copyToClipboard(game.id.asString))
                                .color(NamedTextColor.GREEN)))
                        }
                    }

                    // player makequit
                    // forces a player to quit his game
                    literalArgument("makequit") {
                        anyExecutor { sender, args ->
                            val player: Player? = Bukkit.getPlayer(args.get("playername").toString())

                            if (player == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this player is not online right now!").color(NamedTextColor.WHITE)))
                                return@anyExecutor
                            }

                            if (player.asGamePlayer.quitGame())
                                player.sendMessage(prefix.append(Component.text("Successfully removed ${player.name} from the game!").color(NamedTextColor.GREEN)))
                            else
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But ${player.name} does currently not play any game!").color(NamedTextColor.WHITE)))
                        }
                    }

                    // player sendto
                    // makes a player join into a game
                    literalArgument("sendto") {
                        argument(StringArgument("gameid").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                            GameManagementAPI.games.filter { it.isQueuing }.map { it.id.asString }.toTypedArray()
                        })) {
                            anyExecutor { sender, args ->
                                val gameID = GameID.fromString(args.get("gameid").toString())
                                val game: Game? = GameManagementAPI.getGame(gameID)
                                val player: Player? = Bukkit.getPlayer(args.get("playername").toString())

                                if (player == null) {
                                    sender.sendMessage(prefix.append(sorry).append(Component.text("But this player is not online right now!").color(NamedTextColor.WHITE)))
                                    return@anyExecutor
                                }

                                if (game == null) {
                                    player.sendMessage(prefix.append(sorry).append(Component.text("But this game does not exist!").color(NamedTextColor.WHITE)))
                                    return@anyExecutor
                                }

                                if (player.asGamePlayer.game == game) {
                                    sender.sendMessage(prefix.append(Component.text("${player.name} is already in ${game.id.asString}").color(NamedTextColor.RED)))
                                    return@anyExecutor
                                }

                                player.asGamePlayer.quitGame()
                                if (player.asGamePlayer.joinGame(game))
                                    player.sendMessage(prefix.append(Component.text("Successfully moved ${player.name} to ${game.id.asString}").color(NamedTextColor.WHITE)))
                                else
                                    player.sendMessage(prefix.append(sorry).append(Component.text("But ${player.name} cannot join into this game!").color(NamedTextColor.WHITE)))
                            }
                        }
                    }

                    // player jump
                    // makes commandSender jump into the same game as target (if possible)
                    literalArgument("jump") {
                        playerExecutor { sender, args ->
                            val player: Player? = Bukkit.getPlayer(args.get("playername").toString())
                            val game: Game? = player?.asGamePlayer?.game

                            if (player == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But this player is not online right now!").color(NamedTextColor.WHITE)))
                                return@playerExecutor
                            }

                            if (game == null) {
                                sender.sendMessage(prefix.append(sorry).append(Component.text("${player.name} is not a player in any game!").color(NamedTextColor.WHITE)))
                                return@playerExecutor
                            }

                            if (sender.asGamePlayer.game == game) {
                                sender.sendMessage(prefix.append(Component.text("You are already in the same game as ${player.name}").color(NamedTextColor.RED)))
                                return@playerExecutor
                            }

                            // jump to game
                            sender.asGamePlayer.quitGame()
                            if (sender.asGamePlayer.joinGame(game))
                                player.sendMessage(prefix.append(Component.text("Joining game...").color(NamedTextColor.GREEN)))
                            else
                                sender.sendMessage(prefix.append(sorry).append(Component.text("But you cannot join ${game.id.asString}!").color(NamedTextColor.WHITE)))
                        }
                    }
                }
            }
        }
    }
}