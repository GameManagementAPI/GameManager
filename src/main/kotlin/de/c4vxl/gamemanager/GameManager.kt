package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.plugin.commands.APICommand
import de.c4vxl.gamemanager.plugin.commands.JoinCommand
import de.c4vxl.gamemanager.plugin.commands.QuitCommand
import de.c4vxl.gamemanager.plugin.commands.StartCommand
import de.c4vxl.gamemanager.plugin.handlers.*
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.java.JavaPlugin

class GameManager : JavaPlugin() {
    companion object {
        val prefix: Component = Component.text("[").color(NamedTextColor.GRAY)
            .append(Component.text("GameManager").color(NamedTextColor.AQUA))
            .append(Component.text("] ").color(NamedTextColor.GRAY))

        lateinit var instance: JavaPlugin
    }

    override fun onLoad() {
        instance = this
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).silentLogs(true))
    }

    override fun onEnable() {
        // register commands
        CommandAPI.onEnable()
        APICommand
        StartCommand
        JoinCommand
        QuitCommand

        // register listeners
        PlayerRespawnHandler(this)
        PlayerVisibilityHandler(this)
        GameFinishHandler(this)
        QueueHandler(this)
        PlayerConnectionHandler(this)

        logger.info("[+] $name has been enabled! \n  -> using version ${pluginMeta.version}")
    }

    override fun onDisable() {
        CommandAPI.onDisable()

        logger.info("[-] $name has been disabled!")
    }
}