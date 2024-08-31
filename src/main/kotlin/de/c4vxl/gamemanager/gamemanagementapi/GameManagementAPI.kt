package de.c4vxl.gamemanager.gamemanagementapi

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.game.GameID
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.world.WorldManager
import java.io.File

object GameManagementAPI {
    val games: MutableList<Game> = mutableListOf()
    private val gameIDs: MutableList<GameID> get() = games.map { it.id }.toMutableList()

    val possibleGames: MutableList<String> = File(WorldManager.mapsContainerPath).list()?.toMutableList() ?: mutableListOf()

    fun getGames(teamAmount: Int, teamSize: Int): List<Game> = games.filter { it.teamAmount == teamAmount && it.teamSize == teamSize && !it.isPrivate }

    fun getGame(id: GameID): Game? = games.find { it.id.asString == id.asString }

    fun getGame(teamAmount: Int, teamSize: Int): Game {
        return getGames(teamAmount, teamSize).filter { it.isQueuing }.randomOrNull()
            ?: createGame(teamAmount, teamSize)
    }

    fun createPrivateGame(teamAmount: Int, teamSize: Int, owner: GMAPlayer): Game = registerGame(Game(teamAmount, teamSize, owner=owner))
    fun createGame(teamAmount: Int, teamSize: Int): Game = registerGame(Game(teamAmount, teamSize))

    fun registerGame(game: Game): Game = game.also { games.add(game) }
    fun unregisterGame(game: Game): Boolean = games.remove(game)
}