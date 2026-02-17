package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*

/**
 * Command for interacting with the API directly through the game
 */
object APICommand {
    val command = commandTree("gamemanagementapi") {
        withUsage("/gamemanagementapi <options>")
        withPermission(Permission.COMMAND_API.string)
        withAliases("gma")
        withFullDescription(Language.default.get("command.api.desc"))

        literalArgument("games") {
            // games list
            literalArgument("list") {
                anyExecutor { sender, _ ->
                    if (GMA.registeredGames.isEmpty()) {
                        sender.sendMessage(sender.language.getCmp("command.api.games.list.failure.empty"))
                        return@anyExecutor
                    }

                    // Build list
                    var component = sender.language.getCmp("command.api.games.list.msg.title")
                    GMA.registeredGames.forEach {
                        component = component
                            .appendNewline()
                            .append(sender.language.getCmp("command.api.games.list.msg.entry", it.id.asString, it.size.toString()))
                    }

                    sender.sendMessage(component)
                }
            }

            // games register <size>
            literalArgument("register") {
                argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings { GMA.possibleGameSizes.toTypedArray() })) {
                    anyExecutor { sender, args ->
                        val sizeString = args.get("size").toString()
                        val size = GameSize.fromString(sizeString)

                        if (size == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.register.failure.invalid_format", sizeString))
                            return@anyExecutor
                        }

                        if (!GMA.possibleGameSizes.contains(sizeString)) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.register.failure.invalid_size", sizeString))
                            return@anyExecutor
                        }

                        val game = GMA.createGame(size.teamAmount, size.teamSize)
                        sender.sendMessage(sender.language.getCmp("command.api.games.register.success", game.id.toString()))
                    }
                }
            }

            // games start <id>
            literalArgument("start") {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings {
                    GMA.registeredGames.filter { it.isQueuing }.map { it.id.asString }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val id = GameID.fromString(args.get("id").toString())
                        val game: Game? = GMA.getGame(id)

                        if (game == null || !game.isQueuing) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.start.failure.invalid_id"))
                            return@anyExecutor
                        }

                        game.start()
                        sender.sendMessage(sender.language.getCmp("command.api.games.start.success", game.id.asString, game.size.toString()))
                    }
                }
            }

            // games stop <id>
            literalArgument("stop") {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings {
                    GMA.registeredGames.filter { it.isRunning }.map { it.id.asString }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val id = GameID.fromString(args.get("id").toString())
                        val game: Game? = GMA.getGame(id)

                        if (game == null || game.isStopped) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.stop.failure.invalid_id"))
                            return@anyExecutor
                        }

                        game.stop()
                        sender.sendMessage(sender.language.getCmp("command.api.games.stop.success", game.id.asString))
                    }
                }
            }

            // games info <id>
            literalArgument("info") {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings {
                    GMA.registeredGames.map { it.id.asString }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val id = GameID.fromString(args.get("id").toString())
                        val game: Game? = GMA.getGame(id)

                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.info.failure.invalid_id"))
                            return@anyExecutor
                        }

                        sender.sendMessage(
                            sender.language.getCmp("command.api.games.info.l1", game.id.asString)
                                .appendNewline()
                                .append(sender.language.getCmp("command.api.games.info.l2", game.id.asString))
                                .appendNewline()
                                .append(sender.language.getCmp("command.api.games.info.l3", game.state.label))
                                .appendNewline()
                                .append(sender.language.getCmp("command.api.games.info.l4", game.size.toString()))
                                .appendNewline()
                                .append(sender.language.getCmp("command.api.games.info.l5", game.players.size.toString(), game.size.maxPlayers.toString()))

                            // TODO: Show map
                        )
                    }
                }
            }

            // games list-players <id>
            literalArgument("list-players") {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings {
                    GMA.registeredGames.map { it.id.asString }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val id = GameID.fromString(args.get("id").toString())
                        val game: Game? = GMA.getGame(id)

                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.list-players.failure.invalid_id"))
                            return@anyExecutor
                        }

                        if (game.players.isEmpty()) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.list-players.failure.empty"))
                            return@anyExecutor
                        }

                        // Build list
                        var component = sender.language.getCmp("command.api.games.list-players.msg.title", game.id.asString)
                        game.players.forEach {
                            component = component
                                .appendNewline()
                                .append(sender.language.getCmp(
                                    "command.api.games.list-players.msg.entry",
                                    it.bukkitPlayer.name,
                                    // TODO: Display team here
                                ))
                        }

                        sender.sendMessage(component)
                    }
                }
            }

            // games join <id>
            literalArgument("join") {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings {
                    GMA.registeredGames.filter { it.isQueuing }.map { it.id.asString }.toTypedArray()
                })) {
                    playerExecutor { player, args ->
                        val id = GameID.fromString(args.get("id").toString())
                        val game: Game? = GMA.getGame(id)

                        if (game == null) {
                            player.sendMessage(player.language.getCmp("command.api.games.join.failure.invalid_id"))
                            return@playerExecutor
                        }

                        if (player.gma.isInGame) {
                            player.sendMessage(player.language.getCmp("command.api.games.join.failure.in_game"))
                            return@playerExecutor
                        }

                        val success = player.gma.join(game)

                        if (success)
                            player.sendMessage(player.language.getCmp("command.api.games.join.success", game.id.asString))
                        else
                            player.sendMessage(player.language.getCmp("command.api.games.join.failure.general"))
                    }
                }
            }
        }

        // TODO: Add player management options
    }
}