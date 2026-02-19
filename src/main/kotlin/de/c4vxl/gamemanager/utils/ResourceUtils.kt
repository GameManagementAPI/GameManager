package de.c4vxl.gamemanager.utils

import de.c4vxl.gamemanager.GameManager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

object ResourceUtils {
    /**
     * Reads the content of a jar-packed resource
     * @param path The path to the resource
     */
    fun readResource(path: String): String =
        ResourceUtils.javaClass.getResourceAsStream("/$path")?.bufferedReader()?.readText() ?: ""

    /**
     * Saves a jar-packed resource to disk
     * @param path The path on disk
     */
    fun saveResource(path: String, destination: String? = null, replace: Boolean = false) {
        val destPath = Path.of(destination ?: GameManager.instance.dataPath.resolve(Path.of(path).name).toString())

        destPath.parent?.toFile()?.mkdirs()

        if (replace) Files.deleteIfExists(destPath)

        if (destPath.exists())
            return

        ResourceUtils.javaClass.getResourceAsStream("/$path")
            ?.use { Files.copy(it, destPath) }
            ?: error("Resource /$path not found!")
    }
}