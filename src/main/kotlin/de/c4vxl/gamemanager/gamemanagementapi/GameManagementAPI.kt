package de.c4vxl.gamemanager.gamemanagementapi

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.game.GameID

object GameManagementAPI {
    val games: MutableList<Game> = mutableListOf()
    private val gameIDs: MutableList<GameID> get() = games.map { it.id }.toMutableList()

    fun getGames(teamAmount: Int, teamSize: Int): List<Game> = games.filter { it.teamAmount == teamAmount && it.teamSize == teamSize }

    fun getGame(id: GameID): Game? = games.find { it.id.asString == id.asString }

    fun getGame(teamAmount: Int, teamSize: Int): Game {
        return getGames(teamAmount, teamSize).filter { it.isQueuing }.randomOrNull()
            ?: registerGame(Game(teamAmount, teamSize))
    }

    fun registerGame(game: Game): Game = game.also { games.add(game) }
    fun unregisterGame(game: Game): Boolean = games.remove(game)
}