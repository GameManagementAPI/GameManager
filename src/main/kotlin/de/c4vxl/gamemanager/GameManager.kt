package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.plugin.commands.*
import de.c4vxl.gamemanager.plugin.handler.*
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

/**
 * Plugin entry point
 */
class GameManager : JavaPlugin() {
    companion object {
        lateinit var instance: GameManager
        lateinit var logger: Logger
    }

    override fun onLoad() {
        instance = this
        GameManager.logger = this.logger

        // Load CommandAPI
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .silentLogs(true)
                .verboseOutput(false)
        )
    }

    override fun onEnable() {
        // Enable CommandAPI
        CommandAPI.onEnable()

        // Load languages
        Language.load()

        // Register commands
        APICommand
        JoinCommand
        QuitCommand
        StartCommand
        ForcemapCommand
        LanguageCommand
        SpectateCommand
        MapCommand

        // Register handlers
        QueueHandler()
        ConnectionHandler()
        GameEndHandler()
        VisibilityHandler()
        RespawnHandler()
        ScoreboardHandler()

        // Load config
        saveResource("config.yml", false)
        reloadConfig()

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        logger.info("[+] $name has been disabled!")
    }
}