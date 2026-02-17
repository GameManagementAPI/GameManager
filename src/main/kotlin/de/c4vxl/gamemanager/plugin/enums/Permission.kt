package de.c4vxl.gamemanager.plugin.enums

enum class Permission(val string: String) {
    PREFIX("de.c4vxl.gamemanager.perms"),
    COMMAND_PREFIX("${PREFIX.string}.command"),
    COMMAND_API("${COMMAND_PREFIX.string}.api")
}