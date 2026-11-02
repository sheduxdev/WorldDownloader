package dev.desco.worlddownloader.downloads

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object SaveDestination {
    private val timestampFormat = SimpleDateFormat("H'h'mm'm'_MMMM_dd_yyyy")
    private val invalidChars = Regex("[<>:\"/\\\\|?*\\p{Cntrl}]")

    fun timestamp(): String = timestampFormat.format(Date())

    fun directory(root: String, subfolder: String, fallbackRoot: String): File {
        val rootPath = cleanPath(root, fallbackRoot)
        val subPath = cleanPath(subfolder, "")
        return File(rootPath).resolvePath(subPath).also { it.mkdirs() }
    }

    fun worldDirectory(root: File, name: String, overwrite: Boolean, clearExisting: Boolean): File {
        val folderName = cleanSegment(name.ifBlank { timestamp() }, timestamp())
        val target = if (overwrite) root.resolve(folderName) else unique(root.resolve(folderName), "")
        if (target.exists() && clearExisting) {
            target.deleteRecursively()
        }
        target.mkdirs()
        return target
    }

    fun file(root: File, name: String, extension: String, overwrite: Boolean): File {
        val fileName = cleanSegment(name.ifBlank { timestamp() }, timestamp())
        val target = root.resolve("$fileName.$extension")
        return if (overwrite || !target.exists()) target else unique(target, extension)
    }

    private fun File.resolvePath(path: String): File {
        if (path.isBlank()) return this
        return path.split('/', '\\').fold(this) { file, segment -> file.resolve(segment) }
    }

    private fun cleanPath(path: String, fallback: String): String {
        val cleaned = path.split('/', '\\')
            .mapNotNull { raw ->
                val segment = cleanSegment(raw, "")
                segment.takeIf { it.isNotBlank() && it != "." && it != ".." }
            }
            .joinToString(File.separator)
        return cleaned.ifBlank { fallback }
    }

    fun cleanSegment(segment: String, fallback: String): String {
        val cleaned = segment
            .replace(invalidChars, "_")
            .trim()
            .trim('.')
        return cleaned.ifBlank { fallback }
    }

    private fun unique(target: File, extension: String): File {
        if (!target.exists()) return target
        val parent = target.parentFile ?: File(".")
        val baseName = if (extension.isBlank()) target.name else target.name.removeSuffix(".$extension")
        var index = 1
        while (true) {
            val candidateName = if (extension.isBlank()) "$baseName ($index)" else "$baseName ($index).$extension"
            val candidate = parent.resolve(candidateName)
            if (!candidate.exists()) return candidate
            index++
        }
    }
}
