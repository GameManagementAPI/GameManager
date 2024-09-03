package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import de.c4vxl.gamemanager.plugin.commands.*
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
        ForcemapCommand
        PrivateGameCommand
        SpectateCommand

        // register handlers
        PlayerRespawnHandler(this)
        PlayerVisibilityHandler(this)
        GameFinishHandler(this)
        QueueHandler(this)
        PlayerConnectionHandler(this)
        PlayerPrefixHandler(this)

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        CommandAPI.onDisable()

        // stop and unregister all games
        GameManagementAPI.games.takeIf { it.isNotEmpty() }?.let {
            logger.warning("Game manager detected registered games! Unregistering them now...")

            it.forEach { game ->
                game.forceStop()
                GameManagementAPI.unregisterGame(game)
            }
        }

        logger.info("[-] $name has been disabled!")
    }
}