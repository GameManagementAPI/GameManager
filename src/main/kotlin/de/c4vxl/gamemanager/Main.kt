package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.plugin.commands.APICommand
import de.c4vxl.gamemanager.plugin.commands.JoinCommand
import de.c4vxl.gamemanager.plugin.commands.QuitCommand
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
    }

    override fun onLoad() {
        instance = this

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

        // Register commands
        APICommand
        JoinCommand
        QuitCommand

        // Load config
        saveResource("config.yml", false)
        reloadConfig()

        // Load languages
        Language.load()

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        logger.info("[+] $name has been disabled!")
    }
}