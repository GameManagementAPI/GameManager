package de.c4vxl.gamemanager.gamemanagementapi.world

import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class MapConfig(private val worldManager: WorldManager) {
    lateinit var config: YamlConfiguration

    init {
        worldManager.world?.worldFolder?.let { folder ->
            config = YamlConfiguration.loadConfiguration(File(folder, "mapdata.yml"))
        }
    }

    fun getTeamSpawn(teamID: Int): Location? {
        config.getDoubleList("team.$teamID.spawn").let {
            if (it.size < 5) return worldManager.world?.spawnLocation
            else return Location(
                worldManager.world ?: return null,
                it.getOrNull(0) ?: return null,
                it.getOrNull(1) ?: return null,
                it.getOrNull(2) ?: return null,
                it.getOrNull(3)?.toFloat() ?: 90F,
                it.getOrNull(4)?.toFloat() ?: -3F,
            )
        }
    }

    fun getTeamName(teamID: Int): String {
        return config.getString("team.$teamID.name") ?: "#$teamID"
    }

    val mapBuilder: String? get() = config.getString("map.builder")
}