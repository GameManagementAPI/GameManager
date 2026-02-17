package de.c4vxl.gamemanager.gma.world.type

import de.c4vxl.gamemanager.gma.event.game.GameWorldUnloadEvent
import de.c4vxl.gamemanager.gma.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Path

/**
 * Object holding metadata for a map
 * @param manager The world manager this map was created from
 * @param name The name of the map
 * @param dir The directory of the game world
 * @param configPath Path to the metadata file of this map
 */
data class Map(
    val manager: WorldManager,
    val name: String,
    val dir: File,
    val configPath: Path = dir.toPath().resolve("metadata.yml")
) {
    /**
     * Returns the corresponding game-world if this map is loaded
     */
    val world: World?
        get() = Bukkit.getWorld(WorldManager.worldPrefix + manager.game.id.asString)

    /**
     * Returns {@code true} if the map is loaded
     */
    val isLoaded: Boolean
        get() = world != null

    /**
     * Unloads the world
     */
    fun unload(): Boolean {
        if (!isLoaded) return true

        // Call unload event
        GameWorldUnloadEvent(this.manager.game).let {
            it.callEvent()
            if (it.isCancelled) return false
        }

        // Cache world directory
        val folder = world?.worldFolder

        // Unload world
        world?.let { Bukkit.unloadWorld(it, false) }

        // Delete world directory
        return folder?.deleteRecursively() == true
    }

    /**
     * The config file holding this maps metadata
     */
    val metadata: YamlConfiguration
        get() = YamlConfiguration.loadConfiguration(configPath.toFile())

    /**
     * Returns the metadata of a specific scope
     * @param scope The name of the scope (Usually your plugin name)
     */
    fun getMetadata(scope: String): ConfigurationSection? =
        metadata.getConfigurationSection(scope)

    /**
     * Returns the spawn location of a given team
     * @param team The id of the team to look for
     */
    fun getSpawnLocation(team: Int): Location? =
        metadata.getIntegerList("team.$team.spawn").let {
            return Location(
                world,
                (it.getOrNull(0) ?: return@let null) + 0.5,
                (it.getOrNull(1) ?: return@let null).toDouble(),
                (it.getOrNull(2) ?: return@let null) + 0.5,
                it.getOrNull(3)?.toFloat() ?: 90F,
                it.getOrNull(4)?.toFloat() ?: 0F
            )
        } ?: world?.spawnLocation

    /**
     * Returns a list of the builders of the map
     */
    val builders: List<String>
        get() = metadata.getStringList("builders")
}