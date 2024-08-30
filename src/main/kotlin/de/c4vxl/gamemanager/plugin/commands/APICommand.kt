package de.c4vxl.gamemanager.plugin.commands

import org.bukkit.Bukkit
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin

class APICommand(plugin: Plugin) {
    init {
        Bukkit.getPluginCommand("gamemanager")?.let { pluginCommand: PluginCommand ->
            pluginCommand.setExecutor { sender, _, _, args ->
                sender.sendMessage("hey!")

                return@setExecutor false
            }

            pluginCommand.setTabCompleter { sender, _, _, args ->
                return@setTabCompleter mutableListOf<String?>().apply {

                }
            }
        }
    }
}