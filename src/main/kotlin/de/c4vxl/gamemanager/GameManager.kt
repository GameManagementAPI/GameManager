package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.plugin.commands.APICommand
import org.bukkit.plugin.java.JavaPlugin

class GameManager : JavaPlugin() {
    override fun onEnable() {
        logger.info("[+] $name has been enabled! \n  -> using version ${pluginMeta.version}")

        APICommand(this) // register API Command
    }

    override fun onDisable() {
        logger.info("[-] $name has been disabled!")
    }
}