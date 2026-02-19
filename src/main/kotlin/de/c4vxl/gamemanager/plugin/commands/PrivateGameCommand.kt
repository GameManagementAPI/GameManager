package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.event.privateGame.PrivateGamePlayerInviteEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Command for creating private games
 */
object PrivateGameCommand {
    /**
     * Holds a registry of invites to a game
     * Format: owner -> invites
     */
    val invites: MutableMap<GMAPlayer, MutableList<GMAPlayer>> = mutableMapOf()

    val command = commandTree("privategame") {
        withUsage("/privategame <options>")
        withAliases("pg")
        withFullDescription(Language.default.get("command.privategame.desc"))

        // privategame create <size>
        literalArgument("create") {
            withRequirement { it.hasPermission(Permission.COMMAND_PRIVATE_GAME.string) }

            argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings { GMA.possibleGameSizes.toTypedArray() })) {
                playerExecutor { sender, args ->
                    val sizeString = args.get("size").toString()
                    val size = GameSize.fromString(sizeString)

                    // Invalid size format
                    if (size == null) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.create.failure.invalid_format", sizeString))
                        return@playerExecutor
                    }

                    // Game size not supported
                    if (!GMA.possibleGameSizes.contains(sizeString)) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.create.failure.invalid_size", sizeString))
                        return@playerExecutor
                    }

                    // Already in a game
                    if (sender.gma.isInGame) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.create.failure.already"))
                        return@playerExecutor
                    }

                    // Create game
                    val game = GMA.createGame(size, sender.gma)

                    if (sender.gma.join(game))
                        sender.sendMessage(sender.language.getCmp("command.privategame.create.success"))
                    else
                        sender.sendMessage(sender.language.getCmp("command.privategame.failure.general"))
                }
            }
        }

        // privategame invite <player>
        literalArgument("invite") {
            withRequirement { it.hasPermission(Permission.COMMAND_PRIVATE_GAME.string) }

            argument(StringArgument("player").replaceSuggestions(ArgumentSuggestions.strings { s ->
                Bukkit.getOnlinePlayers().filter { !it.gma.isInGame && s.sender != it }.map { it.name }.toTypedArray()
            })) {
                playerExecutor { sender, args ->
                    val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                    val game: Game? = GMA.privateGames.find { it.owner == sender.gma }

                    // Invalid player
                    if (player == null) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.invite.failure.invalid_player"))
                        return@playerExecutor
                    }

                    // No private game
                    if (game == null) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.invite.failure.no_game"))
                        return@playerExecutor
                    }

                    // Already in another game
                    if (player.gma.isInGame) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.invite.failure.player_in_game"))
                        return@playerExecutor
                    }

                    // Player already invited
                    if (invites[sender.gma]?.contains(player.gma) == true) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.invite.failure.already"))
                        return@playerExecutor
                    }

                    // Invite player
                    invites.getOrPut(sender.gma) { mutableListOf() }
                        .add(player.gma)

                    // Call event
                    PrivateGamePlayerInviteEvent(game, sender.gma, player.gma).callEvent()

                    // Send invite
                    player.sendMessage(
                        player.language.getCmp("command.privategame.invite.msg.l1")
                            .appendNewline()
                            .append(player.language.getCmp("command.privategame.invite.msg.l2", sender.name))
                            .appendNewline()
                            .append(player.language.getCmp("command.privategame.invite.msg.l3", sender.name))
                    )

                    sender.sendMessage(sender.language.getCmp("command.privategame.invite.success", player.name))
                }
            }
        }

        // privategame join <player>
        literalArgument("join") {
            argument(StringArgument("player").replaceSuggestions(ArgumentSuggestions.strings { sender ->
                (sender.sender as? Player)?.gma?.let { gmaPlayer ->
                    invites.filter { it.value.contains(gmaPlayer) }.map { it.key.bukkitPlayer.name }.toTypedArray()
                }
            })) {
                playerExecutor { sender, args ->
                    val player: Player? = Bukkit.getPlayer(args.get("player").toString())

                    // Invalid player
                    if (player == null) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.join.failure.invalid_player"))
                        return@playerExecutor
                    }

                    // Get game
                    val wasInvited = invites.getOrDefault(player.gma, mutableListOf()).contains(sender.gma)
                    val game: Game? = if (!wasInvited) null
                                      else GMA.privateGames.find { it.owner == player.gma }

                    // No private game
                    if (game == null) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.join.failure.no_invite", player.name))
                        return@playerExecutor
                    }

                    // Already in another game
                    if (sender.gma.isInGame && sender.gma.game != game) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.join.failure.already"))
                        return@playerExecutor
                    }

                    // Already joined
                    if (sender.gma.game == game) {
                        sender.sendMessage(sender.language.getCmp("command.privategame.join.failure.already_joined"))
                        return@playerExecutor
                    }

                    // Join
                    val success = sender.gma.join(game)
                    if (success)
                        sender.sendMessage(sender.language.getCmp("command.privategame.join.success", player.name))
                    else
                        sender.sendMessage(sender.language.getCmp("command.privategame.join.failure.general"))
                }
            }
        }
    }
}