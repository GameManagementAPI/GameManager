package de.c4vxl.gamemanager.language

import de.c4vxl.gamemanager.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.utils.ResourceUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

/**
 * Lookup table for translations based on keys
 * @param translations A map from key to translation
 * @param path The path to the language file
 * @param name The name of the language
 */
class Language(
    val translations: Map<String, String>,
    val path: Path,
    val name: String = path.nameWithoutExtension
) {
    companion object {
        /**
         * Loads a language from a file
         * @param path The path to the language file
         */
        fun fromFile(path: String): Language? {
            // Read language file
            val file = File(path)
            if (!file.isFile) return null

            val config = YamlConfiguration.loadConfiguration(file)

            // Load lookup table
            val translations = buildMap<String, String> {
                config.getKeys(true).forEach {
                    put(it, config.getString(it) ?: it)
                }
            }

            // Create language instance
            return Language(
                translations,
                Path.of(path)
            )
        }

        /**
         * Returns a list of all available languages
         */
        val availableLanguages: List<String>
            get() = translationsDirectory.toFile().listFiles()?.map { it.nameWithoutExtension } ?: listOf()

        /**
         * Get a language from its name
         * @param name The name of the language
         */
        fun get(name: String): Language? =
            fromFile(translationsDirectory.resolve("$name.yml").toString())

        /**
         * Path to directory where translation files will be stored in
         */
        val translationsDirectory: Path
            get() = Path.of(Main.instance.config.getString("language.translations-dir") ?: "./")

        val langsDB
            get() = File(Main.instance.config.getString("language.db") ?: "languages.yml")

        /**
         * Default fallback-language
         */
        val default: Language
            get() = get(Main.instance.config.getString("language.default") ?: "english")!!

        /**
         * Loads pre-packed translations onto disk
         * @param replace If set to {@code true} existing translation files will be replaced
         */
        fun load(replace: Boolean = false) {
            val langs = ResourceUtils.readResource("langs").split("\n")

            // Load all languages
            langs.forEach {
                ResourceUtils.saveResource(
                    "lang/$it.yml",
                    translationsDirectory.resolve("$it.yml").toString(),
                    replace
                )
            }
        }

        /**
         * Returns the language of a command sender
         */
        val CommandSender.language: Language
            get() =
                (this as? Player)?.gma?.language
                    ?: default

        /**
         * Returns the language preference of a player
         * @param player The player to look for
         */
        fun getPlayerLanguage(player: Player) =
            YamlConfiguration.loadConfiguration(langsDB).getString(player.uniqueId.toString()) ?: default.name

        /**
         * Sets the language preference for a player
         * @param player The player set the preference of
         * @param language The language to set it to
         */
        fun setPlayerLanguage(player: Player, language: String) {
            val config = YamlConfiguration.loadConfiguration(langsDB)
            config.set(player.uniqueId.toString(), language)
            config.save(langsDB)
        }

        /**
         * Provide a language extension for sub-plugins using Language#child
         * @param namespace The namespace used to load this extension
         * @param language The language this extension is made for
         * @param languageFileContent A yml-formatted string of the translations
         */
        fun provideLanguageExtension(namespace: String, language: String, languageFileContent: String) {
            val file = translationsDirectory.resolve("extensions/${namespace}/${language}.yml")

            // Create parent folder
            file.parent.toFile().mkdirs()

            // Save language
            file.toFile().writeText(languageFileContent)
        }
    }

    /**
     * Returns the translation of a key
     * @param key The translation key
     * @param args Arguments to the translation
     */
    fun get(key: String, vararg args: String): String {
        var value = resolveKey(key)

        // Handle arguments
        args.forEachIndexed { i, arg ->
            value = value.replace("$$i", arg)
        }

        return value
    }

    /**
     * Looks up the translation of a key
     * @param key The key to lookup
     * @param visited A list of previously visited keys to prevent circular key-references
     */
    private fun resolveKey(key: String, visited: MutableSet<String> = mutableSetOf()): String {
        // Key already visited
        // This prevents circular references leading to stack overflows
        if (!visited.add(key)) return key

        var value = translations.getOrDefault(key, key)

        // Resolve references
        value = Regex("""\$\{([^}]+)}""").replace(value) {
            resolveKey(
                it.groupValues[1],
                visited
            )
        }

        visited.remove(key)
        return value
    }

    /**
     * Returns a styled-component with the translation of a key
     * @param key The translation key
     * @param args Arguments to the translation
     */
    fun getCmp(key: String, vararg args: String): Component =
        MiniMessage.miniMessage().deserialize(get(key, *args))

    /**
     * Returns a language extension
     * This should be used by other plugins to load their own translations
     *
     * @param namespace The name of the other plugin
     */
    fun child(namespace: String): Language =
        fromFile(translationsDirectory.resolve("extensions/${namespace}/${this.name}.yml").toString())!!
}