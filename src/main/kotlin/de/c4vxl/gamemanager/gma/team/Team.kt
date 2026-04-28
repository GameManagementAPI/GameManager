package de.c4vxl.gamemanager.gma.team

import de.c4vxl.gamemanager.gma.event.team.GameTeamMessageBroadcastEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.language.Language
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Object that holds information about one team of a game
 * @param manager The manager managing this team
 * @param id The id of this team
 * @param size The maximum amount of players that can join this team
 */
data class Team(
    val manager: TeamManager,
    val id: Int,
    val size: Int = manager.game.size.teamSize
) {
    companion object {
        private val prefixes = mutableMapOf<String, Map<Int, String>>()

        /**
         * Registers a team label
         * @param language The language to register the prefix for
         * @param translations The translations of team ids to prefixes
         */
        fun registerLabelTranslation(language: String, translations: Map<Int, String>) {
            prefixes[language] = translations
        }

        /**
         * Returns a label of a specific team in a target language
         * @param teamId The id of the team
         * @param language The language to translate in to
         */
        fun getLabel(teamId: Int, language: Language): String {
            val prefixes = prefixes[language.name] ?: emptyMap()

            // Try to get from registry
            return prefixes[teamId]

                    // Or fallback to default translation
                    ?: language.get("team.label.default.format", (teamId + 1).toString())
        }
    }

    /**
     * Holds a list of all players in this team
     */
    val players: MutableList<GMAPlayer> = mutableListOf()

    /**
     * Returns {@code true} if the team is full
     */
    val isFull: Boolean get() = players.size >= this.size

    /**
     * Returns the label of this team in a given language
     */
    fun label(language: Language) = MiniMessage.miniMessage().deserialize(labelStr(language))

    /**
     * Returns the label of this team in a given language as a string
     */
    fun labelStr(language: Language) = getLabel(this.id, language)

    /**
     * List of players that have left the team
     */
    val playersLeft = mutableSetOf<GMAPlayer>()

    /**
     * Broadcasts a message to the entire team
     * @param key The language key of the message
     * @param args The arguments of the translation
     * @param child The language-child the message comes from
     */
    fun broadcastMessage(key: String, vararg args: String, child: String? = null) {
        val audience = this.players.distinct()

        // Call event
        GameTeamMessageBroadcastEvent(this, this.manager.game, child, key, args.toList(), audience).let {
            it.callEvent()
            if (it.isCancelled) return
        }

        // Send message
        audience.forEach { player -> player.bukkitPlayer.sendMessage(
            (child?.let { player.language.child(child) } ?: player.language)
                .getCmp(key, *args)
        ) }
    }

    override fun toString(): String { return "Team { label=${this.label(Language.default)} }" }
    override fun hashCode(): Int { return this.id.hashCode() }
    override fun equals(other: Any?): Boolean { return this.id == (other as? Team)?.id && this.manager.game == other.manager.game }
}