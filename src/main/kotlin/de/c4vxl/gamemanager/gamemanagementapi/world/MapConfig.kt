package de.c4vxl.gamemanager.gamemanagementapi.world

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class MapConfig(private val mapFolder: File, val world: World) {
    val config: YamlConfiguration = YamlConfiguration.loadConfiguration(File(mapFolder, "mapdata.yml"))

    fun getTeamSpawn(teamID: Int): Location? {
        config.getDoubleList("team.$teamID.spawn").let {
            if (it.size < 5) return world.spawnLocation
            else return Location(
                world,
                it.getOrNull(0) ?: return null,
                it.getOrNull(1) ?: return null,
                it.getOrNull(2) ?: return null,
                it.getOrNull(3)?.toFloat() ?: 90F,
                it.getOrNull(4)?.toFloat() ?: -3F,
            )
        }
    }

    val mapBuilder: String? get() = config.getString("map.builder")
}