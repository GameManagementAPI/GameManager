package de.c4vxl.gamemanager.plugin.commands

import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor

/**
 * A command for players to adjust their language preferences
 */
object LanguageCommand {
    val command = commandTree("language") {
        withUsage("/language <language>")
        withAliases("lang")
        withFullDescription(Language.default.get("command.language.desc"))

        argument(StringArgument("language").replaceSuggestions(ArgumentSuggestions.strings {
            Language.availableLanguages.toTypedArray()
        })) {
            playerExecutor { player, args ->
                // Get language
                val languageName = args.get("language")?.toString() ?: Language.default.name
                val language = Language.get(languageName)

                // Language doesn't exist
                if (language == null) {
                    player.sendMessage(player.language.getCmp("command.language.failure.invalid_language"))
                    return@playerExecutor
                }

                // Update language
                player.gma.language = language
                player.sendMessage(player.language.getCmp("command.language.success", languageName))
            }
        }
    }
}