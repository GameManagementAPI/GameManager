package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Handles player visibility
 * Stops players of different games from seeing each other
 */
class VisibilityHandler : Listener {
    private var isInitialized: Boolean = false

    init {
        // Initialize visibility task
        if (!isInitialized) {
            Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
                Bukkit.getOnlinePlayers().forEach { handle(it) }
            }, 0, 10)

            isInitialized = true
        }

        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    /**
     * Handles visibility for one player
     */
    private fun handle(self: Player) {
        Bukkit.getOnlinePlayers().forEach { other ->
            // Different game
            // always hide both ways
            if (other.gma.game != self.gma.game) {
                other.hidePlayer(Main.instance, self)
                self.hidePlayer(Main.instance, other)
                return@forEach
            }

            // TODO: Handle custom visibility for spectators

            // Same game
            other.showPlayer(Main.instance, self)
            self.showPlayer(Main.instance, other)
        }
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        // TODO: Custom spectator chat

        // Stop from sending message
        event.isCancelled = true

        // Get gma data
        val player = event.player.gma
        val game = player.game

        // Get the message
        val message = event.message()
        val plain = PlainTextComponentSerializer.plainText().serialize(message)

        // Get the channel the message was sent in
        val allTags = Main.instance.config.getStringList("visibility.public")
        val channel = if (game == null) "no-game" // No game

                        // Game is queuing
                        else if (game.isQueuing) "queue"

                        // Message contains public tag or team is one player only
                        else if (allTags.any { plain.contains(it) } || game.size.teamSize == 1) "public"

                        // Config allows team chat
                        else if (Main.instance.config.getBoolean("visibility.allow-team-chat", true)) "team"

                        // Fallback to public chat if config prohibits team chat
                        else "public"

        // Build the final message
        val finalMessage = player.language.getCmp(
            "chat.message.$channel",
            player.team?.label ?: "/",
            player.bukkitPlayer.name
        ).append(message)

        // Send message
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            when (channel) {
                "no-game"         -> Bukkit.getOnlinePlayers().filter { !it.gma.isInGame }.forEach { it.sendMessage(finalMessage) }
                "queue", "public" -> game?.broadcastMessage(finalMessage)
                "team"            -> player.team?.broadcastMessage(finalMessage)
                else              -> Main.logger.warning("Tried to send message in invalid channel") // should never be reached
            }
        }
    }
}