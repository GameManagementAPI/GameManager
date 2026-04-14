package de.c4vxl.gamemanager.gma.data

import de.c4vxl.gamemanager.gma.game.Game

/**
 * A game specific data storage
 * @param game The game the data is used for
 * @param data The initial data stored
 */
class GameDataStore(val game: Game, private val data: MutableMap<String, Any> = mutableMapOf()) {
    /**
     * Allows for storing extra data that persists over the entire game
     * @param key The key to the data object
     * @param element The object to store
     */
    operator fun set(key: String, element: Any?) {
        if (element == null)
            this.data.remove(key)
        else
            this.data[key] = element
    }

    /**
     * Allows for retrieving previously stored data
     * @param key The key to the data object
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: String): T? {
        return this.data[key] as? T
    }
}