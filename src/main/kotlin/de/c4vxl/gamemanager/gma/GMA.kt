package de.c4vxl.gamemanager.gma

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.game.type.GameState

/**
 * Central access point for creating, registering and handling games
 */
object GMA {
    private val gameRegistry: MutableMap<GameID, Game> = mutableMapOf()

    /**
     * A list of all registered games
     */
    val registeredGames
        get() = gameRegistry.values.toList()

    /**
     * Returns a list for possible game sizes
     */
    // TODO: Return a list of possible games based on available maps
    val possibleGameSizes: MutableList<String>
        get() = mutableListOf("2x1", "2x2", "4x1")

    /**
     * Get a game from registry by its id
     * @param id The id
     */
    fun getGame(id: GameID) =
        gameRegistry[id]

    /**
     * Returns a list of all games in registry of a certain size
     * @param teamAmount The amount of teams
     * @param teamSize The size of a team
     * @param state The state of the game (Default: Queuing)
     */
    fun getGames(teamAmount: Int, teamSize: Int, state: GameState = GameState.QUEUING) =
        gameRegistry.values.filter { it.state == state && it.size.equals(teamAmount, teamSize) }

    /**
     * Returns a list of all games in registry of a certain size
     * @param size The size of the game
     * @param state The state of the game (Default: Queuing)
     */
    fun getGames(size: GameSize, state: GameState = GameState.QUEUING) =
        getGames(size.teamAmount, size.teamSize, state)

    /**
     * Finds a game in queuing state in registry or creates one if it doesn't exist
     * @param teamAmount The amount of teams
     * @param teamSize The size of a team
     */
    fun getOrCreate(teamAmount: Int, teamSize: Int): Game =
        gameRegistry.values.firstOrNull { it.isQueuing && it.size.equals(teamAmount, teamSize) }
            ?: createGame(teamAmount, teamSize)

    /**
     * Finds a game in queuing state in registry or creates one if it doesn't exist
     * @param size The size of the game
     */
    fun getOrCreate(size: GameSize) =
        getOrCreate(size.teamAmount, size.teamSize)


    /**
     * Creates a game and registers it
     * @param teamAmount The amount of teams
     * @param teamSize The size of a team
     */
    fun createGame(teamAmount: Int, teamSize: Int) =
        createGame(GameSize(teamAmount, teamSize))

    /**
     * Creates a game and registers it
     * @param size The size of the game
     */
    fun createGame(size: GameSize): Game {
        return Game(size).also {
            // Register game
            registerGame(it)
        }
    }

    /**
     * Save a game to the registry
     * @param game The game to save
     * @param force If set to 'true' an already existing game with the same id will be overwritten
     */
    fun registerGame(game: Game, force: Boolean = false) {
        // A game with the same id already exists
        if (gameRegistry.containsKey(game.id)) {
            // force-flag disabled
            if (!force) return

            // Let GMA.unregisterGame take care of properly disposing of the game
            unregisterGame(game)
        }

        // Save to registry
        gameRegistry[game.id] = game
    }

    /**
     * Unregisters a game
     * @param game The game to unregister
     * @param stop If set to 'true' the game will be stopped before unregistering
     */
    fun unregisterGame(game: Game, stop: Boolean = true) {
        // Gracefully shutdown game
        if (stop)
            game.stop()

        // Remove from registry
        gameRegistry.remove(game.id)
    }
}