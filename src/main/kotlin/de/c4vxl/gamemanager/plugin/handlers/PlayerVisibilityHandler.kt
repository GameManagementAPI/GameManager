package de.c4vxl.gamemanager.plugin.handlers

import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerJoinEvent
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class PlayerVisibilityHandler(val plugin: Plugin): Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onGameJoin(event: GamePlayerJoinEvent) {
        // hide all players from each other if their game is not the same
        Bukkit.getOnlinePlayers().forEach { pl ->
            if (pl.asGamePlayer.game == event.game) {
                pl.showPlayer(plugin, event.player.bukkitPlayer)
                event.player.bukkitPlayer.showPlayer(plugin, pl)
            } else {
                pl.hidePlayer(plugin, event.player.bukkitPlayer)
                event.player.bukkitPlayer.hidePlayer(plugin, pl)
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
        else if (game!!.isQueuing) "[Queue] "
        else if (game.isRunning && isAtAll) "[@all] "
        else "[${game.worldManager.mapConfig.getTeamName(event.player.asGamePlayer.team?.id ?: -1)}] "

        val finalMessage: Component = Component.text(prefix).append(Component.text("${event.player.name}: ")).append(
            Component.text(plain.apply { allStarts.forEach { this.removePrefix(it).removePrefix("$it ") } }))

        // send message
        Bukkit.getScheduler().callSyncMethod(plugin) {
            if (!isInGame) Bukkit.getOnlinePlayers().filter { !it.asGamePlayer.isInGame }.forEach { it.sendMessage(finalMessage) }
            else if (game!!.isQueuing || (game.isRunning && isAtAll)) game.broadcast(finalMessage)
            else event.player.asGamePlayer.team?.broadcast(finalMessage)
        }
    }
}