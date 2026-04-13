package de.c4vxl.gamemanager.plugin.handler

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import de.c4vxl.gamemanager.GameManager
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * "Removes" advancements from a game
 */
class AdvancementHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, GameManager.instance)
    }

    @EventHandler
    fun onAdvancement(event: PlayerAdvancementCriterionGrantEvent) {
        val world = event.player.world

        // Not a game world
        if (!event.player.gma.isInGame)
            return

        // Only if advancement announcement is specifically disabled
        if (world.getGameRuleValue(GameRules.SHOW_ADVANCEMENT_MESSAGES) != false)
            return

        event.isCancelled = true
    }
}