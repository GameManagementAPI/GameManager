package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.plugin.commands.APICommand
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
    }

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