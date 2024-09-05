package de.c4vxl.gamemanager.gamemanagementapi.team

import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerTeamJoinEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerTeamQuitEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameTeamMessageBroadcastEvent
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.world.WorldManager
import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class Team(val manager: TeamManager, val id: Int, val players: MutableList<GMAPlayer> = mutableListOf()) {
    val maxSize: Int = manager.game.teamSize
    val quitPlayers: MutableList<GMAPlayer> = mutableListOf()
    val isFull: Boolean get() = players.size >= maxSize

    val name: String get() {
        val config = YamlConfiguration.loadConfiguration(File(WorldManager.mapsContainerPath, "teams.yml"))
        return config.getString("$id.name") ?: "#$id"
    }

    /**
     * Makes a player join this team if he is in this game
     * @return Boolean
      */
    fun join(player: GMAPlayer): Boolean {
        if (!manager.game.players.contains(player) || player.isInTeam) return false

        // call event
        GamePlayerTeamJoinEvent(player, manager.game, this).callEvent()

        players.add(player)

        return true
    }

    /**
     * Makes a player quit this team
      */
    fun quit(player: GMAPlayer): Boolean {
        if (!players.contains(player)) return false

        // call event
        GamePlayerTeamQuitEvent(player, manager.game, this).callEvent()

        players.remove(player)
        quitPlayers.add(player)

        return true
    }

    // broadcasting to all members
    fun broadcast(message: Component) {
        GameTeamMessageBroadcastEvent(this.manager.game, this, message).let {
            it.callEvent()
            if (it.isCancelled) return // stop if event has been canceled
        }

        players.forEach { it.bukkitPlayer.sendMessage(message) }
    }
}