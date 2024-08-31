package de.c4vxl.gamemanager.gamemanagementapi.event

import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.game.GameState
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.team.Team
import de.c4vxl.gamemanager.gamemanagementapi.world.MapConfig
import org.bukkit.World

// game-handling based events
data class GamePreStopEvent(val game: Game): GameEvent()
data class GameStopEvent(val game: Game, var kickPlayers: Boolean = true): GameEvent()
data class GameStartEvent(val game: Game): GameEvent()
data class GameForceStopEvent(val game: Game): GameEvent()
data class GameStateChangeEvent(val game: Game, val oldState: GameState, val newState: GameState): GameEvent()

// game-player based events
data class GamePlayerQuitEvent(val player: GMAPlayer, val game: Game): GameEvent()
data class GamePlayerJoinEvent(val player: GMAPlayer, val game: Game): GameEvent()
data class GamePlayerEliminateEvent(val player: GMAPlayer, val game: Game): GameEvent()
data class GamePlayerReviveEvent(val player: GMAPlayer, val game: Game): GameEvent()
data class GamePlayerWinEvent(val player: GMAPlayer, val game: Game): GameEvent()
data class GamePlayerLooseEvent(val player: GMAPlayer, val game: Game): GameEvent()

// team based events
data class GamePlayerTeamJoinEvent(val player: GMAPlayer, val game: Game, val team: Team): GameEvent()
data class GamePlayerTeamQuitEvent(val player: GMAPlayer, val game: Game, val team: Team): GameEvent()
data class GameTeamWinEvent(val player: GMAPlayer, val game: Game): GameEvent()
data class GameTeamLooseEvent(val player: GMAPlayer, val game: Game): GameEvent()

// world based events
data class GameWorldMapForceEvent(val game: Game, var forceTo: String?): GameEvent()
data class GameWorldLoadEvent(val game: Game, val world: World, val config: MapConfig): GameEvent()
data class GameWorldUnloadEvent(val game: Game): GameEvent()