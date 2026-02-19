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
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Command for spectating a game
 */
object SpectateCommand {
    val command = commandTree("spectate") {
        withPermission(Permission.COMMAND_SPECTATE.string)
        withUsage("/spectate <game|player> <id>")
        withAliases("spec")
        withFullDescription(Language.default.get("command.spectate.desc"))

        // spectate player <player>
        literalArgument("player") {
            argument(StringArgument("player").replaceSuggestions(ArgumentSuggestions.strings {
                Bukkit.getOnlinePlayers().filter { it.gma.isInGame }.map { it.name }.toTypedArray()
            })) {
                playerExecutor { sender, args ->
                    val player: Player? = Bukkit.getPlayer(args.get("player").toString())
                    val game: Game? = player?.gma?.game

                    if (player == null) {
                        sender.sendMessage(sender.language.getCmp("command.spectate.player.failure.invalid_player"))
                        return@playerExecutor
                    }

                    if (game == null) {
                        sender.sendMessage(sender.language.getCmp("command.spectate.player.failure.no_game"))
                        return@playerExecutor
                    }

                    if (!game.isRunning) {
                        sender.sendMessage(sender.language.getCmp("command.spectate.player.failure.not_running"))
                        return@playerExecutor
                    }

                    // Start spectating
                    val success = sender.gma.spectate(game)

                    if (success)
                        sender.sendMessage(sender.language.getCmp("command.spectate.player.success", player.name))
                    else
                        sender.sendMessage(sender.language.getCmp("command.spectate.player.failure.playing"))
                }
            }
        }

        // spectate game <id>
        literalArgument("game") {
            argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings {
                GMA.registeredGames.filter { it.isRunning }.map { it.id.asString }.toTypedArray()
            })) {
                playerExecutor { sender, args ->
                    val id = GameID.fromString(args.get("id").toString())
                    val game: Game? = GMA.getGame(id)

                    if (game == null) {
                        sender.sendMessage(sender.language.getCmp("command.spectate.game.failure.invalid_id"))
                        return@playerExecutor
                    }

                    if (!game.isRunning) {
                        sender.sendMessage(sender.language.getCmp("command.spectate.game.failure.not_running"))
                        return@playerExecutor
                    }

                    // Start spectating
                    val success = sender.gma.spectate(game)

                    if (success)
                        sender.sendMessage(sender.language.getCmp("command.spectate.game.success", id.asString))
                    else
                        sender.sendMessage(sender.language.getCmp("command.spectate.game.failure.playing"))
                }
            }
        }
    }
}