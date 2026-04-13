package de.c4vxl.gamemanager.plugin.handler

import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import io.papermc.paper.configuration.constraint.Constraints.Min
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

/**
 * "Removes" advancements from a game
 */
class AdvancementHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, GameManager.instance)
    }

    @EventHandler
    fun onAdvancement(event: PlayerAdvancementDoneEvent) {
        val game = event.player.gma.game ?: return
        val world = game.worldManager.map?.world ?: return

        // Only if advancement announcement is specifically disabled
        if (world.getGameRuleValue(GameRules.SHOW_ADVANCEMENT_MESSAGES) != false)
            return

        val progress = event.player.getAdvancementProgress(event.advancement)

        // Return if advancement isn't done
        if (!progress.isDone)
            return

        // Advancement doesn't show
        if (event.advancement.display?.doesShowToast() != true)
            return

        // Hacky way to disable the advancement popup in the top right corner
        for (criterion in progress.awardedCriteria) {
            progress.revokeCriteria(criterion)
        }

        // Hacky way to reset the xp a challenge advancement might have given
        val currentLevel = event.player.level
        val currentXP = event.player.exp
        Bukkit.getScheduler().runTask(GameManager.instance, Runnable {
            event.player.level = currentLevel
            event.player.exp = currentXP
        })
    }
}