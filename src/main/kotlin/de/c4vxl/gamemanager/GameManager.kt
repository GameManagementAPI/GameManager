package de.c4vxl.gamemanager

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.world.WorldManager
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

    private fun isEnabled(name: String): Boolean =
        this.config.getBoolean("features.$name", true)

    override fun onEnable() {
        // Enable CommandAPI
        CommandAPI.onEnable()

        // Load config
        saveResource("config.yml", false)
        reloadConfig()

        // Load languages
        Language.load()

        // Register commands
        if (isEnabled("commands.api"))          APICommand
        if (isEnabled("commands.join"))         JoinCommand
        if (isEnabled("commands.quit"))         QuitCommand
        if (isEnabled("commands.start"))        StartCommand
        if (isEnabled("commands.forcemap"))     ForcemapCommand
        if (isEnabled("commands.language"))     LanguageCommand
        if (isEnabled("commands.spectate"))     SpectateCommand
        if (isEnabled("commands.map"))          MapCommand
        if (isEnabled("commands.privategame"))  PrivateGameCommand

        // Register handlers
        if (isEnabled("handlers.queue"))       QueueHandler()
        if (isEnabled("handlers.connection"))  ConnectionHandler()
        if (isEnabled("handlers.gameEnd"))     GameEndHandler()
        if (isEnabled("handlers.visibility"))  VisibilityHandler()
        if (isEnabled("handlers.respawn"))     RespawnHandler()
        if (isEnabled("handlers.scoreboard"))  ScoreboardHandler()

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        // Unregister all games
        GMA.registeredGames.forEach {
            GMA.unregisterGame(it, true)
        }

        logger.info("[+] $name has been disabled!")
    }
}