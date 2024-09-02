package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class PlayerVisibilityHandler(val plugin: Plugin): Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    companion object {
        private val plugin = GameManager.instance

        fun handleVisibility(player: GMAPlayer) {
            // Hide all players from each other if their game is not the same
            Bukkit.getOnlinePlayers().forEach { pl ->
                if (pl.asGamePlayer.game == player.game) {
                    // Ensure spectators can see players but players can't see them
                    if (pl.asGamePlayer.isSpectating) {
                        player.bukkitPlayer.hidePlayer(plugin, pl)
                    } else {
                        player.bukkitPlayer.showPlayer(plugin, pl)
                    }

                    if (player.isSpectating) {
                        pl.hidePlayer(plugin, player.bukkitPlayer)
                    } else {
                        pl.showPlayer(plugin, player.bukkitPlayer)
                    }
                } else {
                    // Hide players from each other if they are not in the same game
                    pl.hidePlayer(plugin, player.bukkitPlayer)
                    player.bukkitPlayer.hidePlayer(plugin, pl)
                }
            }
        }
    }

    @EventHandler
    fun onMessageEvent(event: AsyncChatEvent) {
        val message: Component = event.message()
        val plain = PlainTextComponentSerializer.plainText().serialize(message)
        event.isCancelled = true

        val game: Game? = event.player.asGamePlayer.game
        val isInGame: Boolean = game != null
        val allStarts = mutableListOf("@a", "@all", "@e", "@everyone")
        val isAtAll: Boolean = allStarts.map { plain.startsWith(it) }.contains(true) || game?.teamSize == 1

        // (in lobby) name: Hey
        // (in queue) [Queue] name: Hey
        // (in game without @a) [Team-1] name: Hey
        // (in game with @a) [@all] name: Hey
        val prefix = if (!isInGame) ""
        else if (event.player.asGamePlayer.isSpectating) "[Spectators] "
        else if (game!!.isQueuing) "[Queue] "
        else if (game.isRunning && isAtAll) "[@all:${event.player.asGamePlayer.team?.let {" " + it.name} ?: ""}] "
        else "[@team] "

        val finalMessage: Component = LegacyComponentSerializer.legacySection().deserialize(prefix)
            .append(Component.text("${event.player.name}: ")).append(
            Component.text(plain.apply { allStarts.forEach { this.removePrefix(it).removePrefix("$it ") } }))

        // send message
        Bukkit.getScheduler().callSyncMethod(plugin) {
            if (!isInGame) Bukkit.getOnlinePlayers().filter { !it.asGamePlayer.isInGame }.forEach { it.sendMessage(finalMessage) }
            else if (event.player.asGamePlayer.isSpectating) game!!.spectators.map { it.bukkitPlayer }.forEach { it.sendMessage(finalMessage) }
            else if (game!!.isQueuing || (game.isRunning && isAtAll)) game.broadcast(finalMessage)
            else event.player.asGamePlayer.team?.broadcast(finalMessage)
        }
    }
}