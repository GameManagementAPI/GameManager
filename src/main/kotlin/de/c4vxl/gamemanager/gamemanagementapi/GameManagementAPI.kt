package de.c4vxl.gamemanager.gamemanagementapi

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.game.GameID

object GameManagementAPI {
    private val games: MutableMap<String, MutableList<Game>> = mutableMapOf()
    private val gameIDs: MutableSet<GameID> = mutableSetOf()

    fun getGames(teamAmount: Int, teamSize: Int): List<Game> {
        val key = "${teamAmount}x$teamSize"
        return games[key]?.filter { !it.isOver } ?: emptyList()
    }

    fun getGame(id: GameID): Game? {
        return games.values.flatten().find { it.id == id }
    }

    fun getGame(teamAmount: Int, teamSize: Int): Game {
        return getGames(teamAmount, teamSize).filter { it.isQueuing }.randomOrNull()
            ?: registerGame(Game(teamAmount, teamSize))
    }

    fun registerGame(game: Game): Game {
        val key = "${game.teamAmount}x${game.teamSize}"
        games.getOrPut(key) { mutableListOf() }.apply {
            add(game)
        }
        gameIDs.add(game.id)
        return game
    }

    fun unregisterGame(game: Game): Boolean {
        val key = "${game.teamAmount}x${game.teamSize}"
        val removed = games[key]?.remove(game) ?: false
        if (removed && games[key].isNullOrEmpty()) {
            games.remove(key)
        }
        gameIDs.remove(game.id)
        return removed
    }
}