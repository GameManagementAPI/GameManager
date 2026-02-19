package de.c4vxl.gamemanager.gma.world

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.event.game.GameWorldForcemapEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.game.type.GameState
import de.c4vxl.gamemanager.gma.world.type.Map
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.WorldCreator
import java.io.File

/**
 * Manages the loading and unloading of game-maps
 * @param game The game this manager is responsible for
 */
class WorldManager(
    val game: Game
) {
    companion object {
        /**
         * The directory containing the different maps
         */
        val mapsDirectory: File
            get() =
                File(
                    Main.instance.config.getString("maps.db")
                        ?: "./gma-maps/"
                ).also { it.mkdirs() }

        /**
         * Returns a list of all possible game sizes
         */
        val availableGameSizes: List<GameSize>
            get() = mapsDirectory.listFiles()
                ?.filter { it.isDirectory }
                ?.mapNotNull { GameSize.fromString(it.name) }
                ?: listOf()

        /**
         * Returns a prefix for game worlds
         */
        val worldPrefix: String
            get() = Main.instance.config.getString("maps.world-prefix") ?: "gma-"
    }

    /**
     * Returns a list of all maps available for this game
     */
    val availableMaps: List<String>
        get() = mapsDirectory.resolve(game.size.toString()).listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: listOf()

    /**
     * Holds the map of the game
     */
    lateinit var map: Map

    /**
     * Returns the name of the current map
     */
    val mapName: String?
        get() = if (::map.isInitialized) map.name
                else null

    /**
     * If set to the name of a map that map will be loaded
     */
    var forcemap: String? = null
        set(value) {
            // Call event
            GameWorldForcemapEvent(this.game, value).let {
                it.callEvent()
                if (it.isCancelled) return
            }

            field = value
        }

    /**
     * Loads a random map
     * @return Returns {@code true} upon success
     */
    fun loadRandom(): Boolean =
        availableMaps.randomOrNull()?.let { load(it) } ?: false

    /**
     * Loads a specific map
     * @param name The name of the map
     * @return Returns {@code true} upon success
     */
    fun load(name: String): Boolean {
        // No available maps
        if (availableMaps.isEmpty()) {
            Main.logger.warning("GameManager cannot find any maps for ${game.size}! Stopping game. Check your maps folder!")
            game.stop()
            return false
        }

        // Exit early
        if ((!game.isQueuing && game.state != GameState.STARTING) // Game running or over
            || ::map.isInitialized                                // Map already initialized
        )
            return false

        // Call load event
        GameWorldLoadEvent(this.game).let {
            it.callEvent()
            if (it.isCancelled) return false
        }

        // Map not found
        // -> Load random one
        if (!availableMaps.contains(name))
            return loadRandom()

        // Copy map
        val mapDir = mapsDirectory.resolve("${game.size}/$name")
        val dest = Bukkit.getWorldContainer().resolve(worldPrefix + game.id.asString)
        mapDir.copyRecursively(dest, true)

        // Create world
        val world = Bukkit.createWorld(WorldCreator(dest.name)) ?: return false

        // Update game-rules
        world.let {
            it.setGameRule(GameRules.RANDOM_TICK_SPEED, 0)
            it.setGameRule(GameRules.ADVANCE_TIME, false)
            it.setGameRule(GameRules.ADVANCE_WEATHER, false)
            it.setGameRule(GameRules.MOB_DROPS, false)
            it.setGameRule(GameRules.SPAWN_MOBS, false)
            it.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false)
            it.setGameRule(GameRules.KEEP_INVENTORY, false)
            it.setGameRule(GameRules.IMMEDIATE_RESPAWN, true)
        }

        // Initialize map object
        this.map = Map(this, name, dest)

        // Call loaded event
        GameWorldLoadedEvent(this.game, this.map)
            .callEvent()

        return true
    }
}