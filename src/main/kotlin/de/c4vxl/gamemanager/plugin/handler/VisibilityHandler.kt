package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage
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
            Bukkit.getScheduler().runTaskTimer(GameManager.instance, Runnable {
                Bukkit.getOnlinePlayers().forEach { handle(it) }
            }, 0, 10)

            isInitialized = true
        }

        Bukkit.getPluginManager().registerEvents(this, GameManager.instance)
    }

    /**
     * Handles visibility for one player
     */
    private fun handle(self: Player) {
        Bukkit.getOnlinePlayers().forEach { other ->
            // Different game
            // always hide both ways
            if (other.gma.game != self.gma.game) {
                other.hidePlayer(GameManager.instance, self)
                self.hidePlayer(GameManager.instance, other)
                return@forEach
            }

            fun handleSpectator(a: Player, b: Player) =
                // A is spectating
                // Hide a
                if (a.gma.isSpectating)
                    b.hidePlayer(GameManager.instance, a)

                // Self is not spectating
                // Show a
                else
                    b.showPlayer(GameManager.instance, a)



            // Same game
            handleSpectator(self, other)
            handleSpectator(other, self)
        }
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        // Stop from sending message
        event.isCancelled = true

        // Get gma data
        val player = event.player.gma
        val game = player.game

        // Get the message
        val message = event.message()
        val plain = PlainTextComponentSerializer.plainText().serialize(message)

        // Get the channel the message was sent in
        val allTags = GameManager.instance.config.getStringList("visibility.public")
        val channel = if (game == null) "no-game" // No game

                        // Player is spectator
                        else if (player.isSpectating) "spectator"

                        // Game is queuing
                        else if (game.isQueuing) "queue"

                        // Message contains public tag or team is one player only
                        else if (allTags.any { plain.contains(it) } || game.size.teamSize == 1) "public"

                        // Config allows team chat
                        else if (GameManager.instance.config.getBoolean("visibility.allow-team-chat", true)) "team"

                        // Fallback to public chat if config prohibits team chat
                        else "public"

        // Build the final message
        val translationKey = "chat.message.$channel"
        val translationArgs = arrayOf(
            player.bukkitPlayer.name,
            player.team?.label ?: "/",
            MiniMessage.miniMessage().serialize(message)
        )

        // Send message
        Bukkit.getScheduler().callSyncMethod(GameManager.instance) {
            when (channel) {
                "no-game"         -> Bukkit.getOnlinePlayers().filter { !it.gma.isInGame }.forEach { it.sendMessage(it.language.getCmp(translationKey, *translationArgs)) }
                "spectator"       -> game?.playerManager?.spectators?.forEach { it.bukkitPlayer.sendMessage(it.language.getCmp(translationKey, *translationArgs)) }
                "queue", "public" -> game?.broadcastMessage(translationKey, *translationArgs)
                "team"            -> player.team?.broadcastMessage(translationKey, *translationArgs)
                else              -> GameManager.logger.warning("Tried to send message in invalid channel") // should never be reached
            }
        }
    }
}