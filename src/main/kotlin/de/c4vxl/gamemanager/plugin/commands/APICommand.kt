package de.c4vxl.gamemanager.plugin.commands

import dev.jorel.commandapi.kotlindsl.commandTree

object APICommand {
    init {
        commandTree("gamemanagementapi") {
            withFullDescription("Allows you to interact directly with the gamemanagement api")
            withPermission("c4vxl.gamemanager.perms.cmd.gamemanager")
            withUsage("/gamemanager <options>")
        }
    }
}