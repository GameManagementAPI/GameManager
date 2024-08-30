package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.plugin.commands.APICommand
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import org.bukkit.plugin.java.JavaPlugin


class GameManager : JavaPlugin() {
    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).silentLogs(true))
    }

    override fun onEnable() {
        CommandAPI.onEnable()
        APICommand

        logger.info("[+] $name has been enabled! \n  -> using version ${pluginMeta.version}")
    }

    override fun onDisable() {
        CommandAPI.onDisable()

        logger.info("[-] $name has been disabled!")
    }
}