package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.plugin.commands.*
import de.c4vxl.gamemanager.plugin.handler.*
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger
    }

    override fun onLoad() {
        instance = this
        Main.logger = this.logger

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

        // Register handlers
        QueueHandler()
        ConnectionHandler()
        GameEndHandler()
        VisibilityHandler()
        RespawnHandler()

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