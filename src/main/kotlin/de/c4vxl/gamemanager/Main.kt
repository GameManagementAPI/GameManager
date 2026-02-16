package de.c4vxl.gamemanager

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

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        logger.info("[+] $name has been disabled!")
    }
}