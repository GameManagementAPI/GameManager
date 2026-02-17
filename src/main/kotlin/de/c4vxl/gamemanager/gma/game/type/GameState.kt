package de.c4vxl.gamemanager.gma.game.type

/**
 * A list of different states a game could be in
 */
enum class GameState(val label: String) {
    QUEUING("<gold>■ Queuing</gold>"),
    STARTING("<dark_green>■ Starting</dark_green>"),
    RUNNING("<green>■ Running</green>"),
    STOPPING("<dark_red>■ Stopping</dark_red>"),
    STOPPED("<red>■ Stopped</red>")
}