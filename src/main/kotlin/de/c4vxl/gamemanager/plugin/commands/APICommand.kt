package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player

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
                    // No games registered
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

                        // Invalid size format
                        if (size == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.register.failure.invalid_format", sizeString))
                            return@anyExecutor
                        }

                        // Game size not supported
                        if (!GMA.possibleGameSizes.contains(sizeString)) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.register.failure.invalid_size", sizeString))
                            return@anyExecutor
                        }

                        // Create game
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

                        // Invalid game id
                        // or game is already running or over
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

                        // Invalid game id
                        // or game is already stopped
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

                        // Invalid game id
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

                        // Invalid game id
                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.games.list-players.failure.invalid_id"))
                            return@anyExecutor
                        }

                        // No players
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
                                    it.team?.label ?: "/"
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

                        // Invalid game id
                        if (game == null) {
                            player.sendMessage(player.language.getCmp("command.api.games.join.failure.invalid_id"))
                            return@playerExecutor
                        }

                        // Player already in a game
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

        literalArgument("player") {
            argument(StringArgument("player").replaceSuggestions(ArgumentSuggestions.strings {
                Bukkit.getOnlinePlayers().map { it.name }.toTypedArray()
            })) {
                // player <name> game
                literalArgument("game") {
                    anyExecutor { sender, args ->
                        val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                        val game: Game? = player?.gma?.game

                        // Player not online
                        if (player == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.game.failure.invalid_player"))
                            return@anyExecutor
                        }

                        // Not connected to a game
                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.game.failure.no_game"))
                            return@anyExecutor
                        }

                        sender.sendMessage(sender.language.getCmp("command.api.player.game.msg", player.name, game.id.asString))
                    }
                }

                // player <name> team
                literalArgument("team") {
                    literalArgument("get") {
                        anyExecutor { sender, args ->
                            val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                            val game: Game? = player?.gma?.game

                            // Player not online
                            if (player == null) {
                                sender.sendMessage(sender.language.getCmp("command.api.player.team.get.failure.invalid_player"))
                                return@anyExecutor
                            }

                            // Not connected to a game
                            if (game == null) {
                                sender.sendMessage(sender.language.getCmp("command.api.player.team.get.failure.no_game"))
                                return@anyExecutor
                            }

                            val team = player.gma.team
                            if (team == null) {
                                sender.sendMessage(sender.language.getCmp("command.api.player.team.get.failure.no_team"))
                                return@anyExecutor
                            }

                            sender.sendMessage(sender.language.getCmp("command.api.player.team.get.msg", player.name, team.id.toString(), team.label))
                        }
                    }

                    // player <name> team join <team>
                    literalArgument("join") {
                        argument(StringArgument("team").replaceSuggestions(ArgumentSuggestions.strings {
                            (it.sender as? Player)?.gma
                                ?.game
                                ?.teamManager
                                ?.teams?.values
                                ?.map { team -> "t${team.id}" }
                                ?.toTypedArray() ?: arrayOf()
                        })) {
                            playerExecutor { player, args ->
                                val game: Game? = player.gma.game

                                // Invalid game id
                                if (game == null) {
                                    player.sendMessage(player.language.getCmp("command.api.player.team.join.failure.no_game"))
                                    return@playerExecutor
                                }

                                // Game is not in queuing state
                                if (!game.isQueuing) {
                                    player.sendMessage(player.language.getCmp("command.api.player.team.join.failure.already_running"))
                                    return@playerExecutor
                                }

                                // Get team
                                val teamName = args.get("team").toString()
                                val teamId = teamName.removePrefix("t").toIntOrNull() ?: -1
                                val team: Team? = game.teamManager.teams[teamId]

                                // Team not found
                                if (team == null) {
                                    player.sendMessage(player.language.getCmp("command.api.player.team.join.failure.invalid_team", teamName))
                                    return@playerExecutor
                                }

                                // Join team
                                // Using force = true to make player quit old team
                                val success = game.teamManager.join(player.gma, teamId, true)

                                if (success)
                                    player.sendMessage(player.language.getCmp("command.api.player.team.join.success", team.label))
                                else
                                    player.sendMessage(player.language.getCmp("command.api.player.team.join.failure.general"))
                            }
                        }
                    }

                    // team quit
                    literalArgument("quit") {
                        playerExecutor { player, _ ->
                            val game: Game? = player.gma.game

                            // Invalid game id
                            if (game == null) {
                                player.sendMessage(player.language.getCmp("command.api.player.team.quit.failure.no_game"))
                                return@playerExecutor
                            }

                            // Game is not in queuing state
                            if (!game.isQueuing) {
                                player.sendMessage(player.language.getCmp("command.api.player.team.quit.failure.already_running"))
                                return@playerExecutor
                            }

                            // Get team
                            val team: Team? = player.gma.team

                            // Team not found
                            if (team == null) {
                                player.sendMessage(player.language.getCmp("command.api.player.team.quit.failure.invalid_team"))
                                return@playerExecutor
                            }

                            // Leave team
                            // Using force = true to make player quit old team
                            val success = game.teamManager.quit(player.gma)

                            if (success)
                                player.sendMessage(player.language.getCmp("command.api.player.team.quit.success", team.label))
                            else
                                player.sendMessage(player.language.getCmp("command.api.player.team.quit.failure.general"))
                        }
                    }
                }

                // player <name> quit
                literalArgument("quit") {
                    anyExecutor { sender, args ->
                        val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                        val game: Game? = player?.gma?.game

                        // Player not online
                        if (player == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.quit.failure.invalid_player"))
                            return@anyExecutor
                        }

                        // Not connected to a game
                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.quit.failure.no_game"))
                            return@anyExecutor
                        }

                        // Quit the game
                        val success = player.gma.quit()
                        if (success)
                            sender.sendMessage(sender.language.getCmp("command.api.player.quit.success", player.name, game.id.asString))
                        else
                            sender.sendMessage(sender.language.getCmp("command.api.player.quit.failure.general"))
                    }
                }

                // player <name> send <id>
                literalArgument("send") {
                    argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings {
                        GMA.registeredGames.filter { it.isQueuing }.map { it.id.asString }.toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                            val id = GameID.fromString(args.get("id").toString())
                            val game: Game? = GMA.getGame(id)

                            // Invalid game id
                            if (game == null) {
                                sender.sendMessage(sender.language.getCmp("command.api.player.send.failure.invalid_id"))
                                return@anyExecutor
                            }

                            // Player not online
                            if (player == null) {
                                sender.sendMessage(sender.language.getCmp("command.api.player.send.failure.invalid_player"))
                                return@anyExecutor
                            }

                            // Player already in a game
                            if (player.gma.game == game) {
                                sender.sendMessage(sender.language.getCmp("command.api.player.send.failure.same_game"))
                                return@anyExecutor
                            }

                            // Make player join game
                            // using force to make the player quit his old game
                            val success = player.gma.join(game, true)

                            if (success)
                                sender.sendMessage(sender.language.getCmp("command.api.player.send.success", player.name, game.id.asString))
                            else
                                sender.sendMessage(sender.language.getCmp("command.api.player.send.failure.general"))
                        }
                    }
                }

                // player <name> jump
                literalArgument("jump") {
                    playerExecutor { sender, args ->
                        val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                        val game: Game? = player?.gma?.game

                        // Player not online
                        if (player == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.jump.failure.invalid_player"))
                            return@playerExecutor
                        }

                        if (player.uniqueId == sender.uniqueId) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.jump.failure.self"))
                            return@playerExecutor
                        }

                        // Not connected to any game
                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.jump.failure.no_game"))
                            return@playerExecutor
                        }

                        // Already in same game
                        if (sender.gma.game == game) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.jump.failure.same_game", player.name))
                            return@playerExecutor
                        }

                        // Make player join game
                        // using force to make the sender quit his current game
                        val success = sender.gma.join(game, true)
                        if (success)
                            sender.sendMessage(sender.language.getCmp("command.api.player.jump.success", player.name, game.id.asString))
                        else
                            sender.sendMessage(sender.language.getCmp("command.api.player.jump.failure.general"))
                    }
                }

                // player <name> eliminate
                literalArgument("eliminate") {
                    anyExecutor { sender, args ->
                        val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                        val game: Game? = player?.gma?.game

                        // Player not online
                        if (player == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.eliminate.failure.invalid_player"))
                            return@anyExecutor
                        }

                        // Player not in game
                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.eliminate.failure.no_game"))
                            return@anyExecutor
                        }

                        // Player already eliminated
                        // TODO: Implement check if player is eliminated
                        if (false) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.eliminate.failure.already"))
                            return@anyExecutor
                        }

                        // TODO: implement elimination logic

                        sender.sendMessage(sender.language.getCmp("command.api.player.eliminate.success", player.name, game.id.asString))
                    }
                }

                // player <name> revive
                literalArgument("revive") {
                    anyExecutor { sender, args ->
                        val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                        val game: Game? = player?.gma?.game

                        // Player not online
                        if (player == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.revive.failure.invalid_player"))
                            return@anyExecutor
                        }

                        // Player not in game
                        if (game == null) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.revive.failure.no_game"))
                            return@anyExecutor
                        }

                        // Player not eliminated
                        // TODO: Implement check if player actually eliminated
                        if (false) {
                            sender.sendMessage(sender.language.getCmp("command.api.player.revive.failure.already"))
                            return@anyExecutor
                        }

                        // TODO: implement elimination logic

                        sender.sendMessage(sender.language.getCmp("command.api.player.eliminate.success", player.name, game.id.asString))
                    }
                }
            }
        }
    }
}