package de.c4vxl.gamemanager.gamemanagementapi.world

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File

class WorldManager(val game: Game) {
    companion object {
        val mapsContainerPath: String = "gamemanager_maps/"
    }

    val availableMaps: MutableList<String> = File(mapsContainerPath, game.gameSize).apply {
        if (!this.isDirectory) this.mkdirs()
    }.list()?.toMutableList() ?: mutableListOf()

    init {
        // force-stop if no maps are available
        if (availableMaps.isEmpty()) {
            Bukkit.getLogger().warning("GameManager cannot find any maps for ${game.gameSize}! Stopping game...")
            game.forceStop()
        }
    }

    // using random if null
    var forcemap: String? = null

    val world: World? get() = Bukkit.getWorld(game.id.asString)
    lateinit var mapConfig: MapConfig

    fun loadMap(mapName: String? = forcemap): Boolean {
        if (!game.isStarting && !game.isQueuing && world == null) return false

        if (!availableMaps.contains(mapName)) return loadRandomMap() // load random map if selected cannot be found

        val mapFolder = File("$mapsContainerPath/${game.gameSize}/$mapName/")
        mapFolder.copyRecursively(Bukkit.getWorldContainer())

        return (WorldCreator(game.id.asString)
            .createWorld() != null).also {
                mapConfig = MapConfig(this)
            }
    }

    fun loadRandomMap(): Boolean = availableMaps.randomOrNull()?.let { loadMap(it) } ?: false

    fun removeWorld(): Boolean = world?.worldFolder?.deleteRecursively() == true && Bukkit.unloadWorld(game.id.asString, false)
}