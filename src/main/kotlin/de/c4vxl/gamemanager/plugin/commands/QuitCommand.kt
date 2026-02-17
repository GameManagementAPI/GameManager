package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor

/**
 * Command for leaving the current game
 */
object QuitCommand {
    val command = commandTree("quit") {
        withUsage("/quit")
        withAliases("q")
        withFullDescription(Language.default.get("command.quit.desc"))

        playerExecutor { player, _ ->
            if (player.gma.quit())
                player.sendMessage(player.language.getCmp("command.quit.success"))
            else
                player.sendMessage(player.language.getCmp("command.quit.failure.no_game"))
        }
    }
}