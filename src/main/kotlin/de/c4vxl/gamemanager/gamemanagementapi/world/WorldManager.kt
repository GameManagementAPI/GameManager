package de.c4vxl.gamemanager.gamemanagementapi.world

import de.c4vxl.gamemanager.gamemanagementapi.event.GameWorldLoadEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameWorldMapForceEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameWorldUnloadEvent
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File

class WorldManager(val game: Game) {
    companion object {
        val mapsContainerPath: String = "gamemanager/"
    }

    val availableMaps: MutableList<String>
        get() = File(mapsContainerPath, game.gameSize).listFiles { file -> file.isDirectory }?.map { it.name }?.toMutableList() ?: mutableListOf()

    // using random if null
    var forcemap: String? = null
        set(value) {
            // run through GameWorldMapForceEvent event
            GameWorldMapForceEvent(game, value).let {
                it.callEvent()
                field = it.forceTo
            }
        }

    val world: World? get() = Bukkit.getWorld(game.id.asString)
    lateinit var mapConfig: MapConfig

    fun loadMap(mapName: String? = forcemap): Boolean {
        // force-stop if no maps are available
        if (availableMaps.isEmpty()) {
            Bukkit.getLogger().warning("GameManager cannot find any maps for ${game.gameSize}! Stopping game...")
            game.forceStop()
        }

        if (!game.isStarting && !game.isQueuing && world == null) return false
        if (!availableMaps.contains(mapName)) return loadRandomMap() // load random map if selected cannot be found

        val mapFolder = File("$mapsContainerPath/${game.gameSize}/$mapName/")
        mapFolder.copyRecursively(File(Bukkit.getWorldContainer(), game.id.asString), true)

        val world: World = Bukkit.createWorld(WorldCreator(game.id.asString)) ?: return false
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_MOB_LOOT, false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.DO_FIRE_TICK, false)
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)

        mapConfig = MapConfig(mapFolder, world)

        GameWorldLoadEvent(game, world, mapConfig).callEvent()

        return true
    }

    fun loadRandomMap(): Boolean = availableMaps.randomOrNull()?.let { loadMap(it) } ?: false

    fun removeWorld(): Boolean {
        // call event
        GameWorldUnloadEvent(game).callEvent()
        val folder = world?.worldFolder
        return Bukkit.unloadWorld(game.id.asString, false) && folder?.deleteRecursively() == true
    }
}