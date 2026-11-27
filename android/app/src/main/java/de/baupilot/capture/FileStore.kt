package de.baupilot.capture

import java.io.File
import java.security.MessageDigest

/**
 * App-private storage for every photo, document and protocol file.
 * Layout: `<root>/projects/<projectId>/{photos,documents,protocols}/<id>.<ext>`.
 */
class FileStore(private val root: File) {
    private val canonicalRoot: File = root.canonicalFile

    fun projectDir(projectId: String): File = child(projectsDir(), projectId)

    fun newPhotoFile(projectId: String, photoId: String): File =
        fileIn(projectId, "photos", "$photoId.jpg")

    fun documentFile(projectId: String, docId: String): File =
        fileIn(projectId, "documents", "$docId.pdf")

    fun protocolFile(projectId: String, protocolId: String): File =
        fileIn(projectId, "protocols", "$protocolId.pdf")

    /** Path of [file] relative to the root, with `/` separators. Throws when [file] lies outside the root. */
    fun relative(file: File): String {
        val canonical = file.canonicalFile
        require(canonical.toPath().startsWith(canonicalRoot.toPath()) && canonical != canonicalRoot) {
            "Path escapes the file store: $file"
        }
        return canonicalRoot.toPath().relativize(canonical.toPath()).joinToString("/")
    }

    /** Inverse of [relative]. Throws [IllegalArgumentException] for absolute or escaping paths. */
    fun resolve(relative: String): File {
        require(relative.isNotEmpty() && !File(relative).isAbsolute) { "Not a relative path: $relative" }
        val resolved = File(canonicalRoot, relative).canonicalFile
        require(resolved.toPath().startsWith(canonicalRoot.toPath()) && resolved != canonicalRoot) {
            "Path escapes the file store: $relative"
        }
        return resolved
    }

    /** Lowercase hex SHA-256 of the file content. */
    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun deleteProject(projectId: String): Boolean {
        val dir = projectDir(projectId)
        return !dir.exists() || dir.deleteRecursively()
    }

    /**
     * Removes what no record points to and returns the number of deleted entries.
     * [known] holds the ids of existing projects and the relative paths (see [relative]) of the files
     * they reference. Directories of projects not in [known] are deleted as a whole; inside known
     * projects, files that are not in [known] and were last modified before `now - olderThanMillis`
     * are deleted. Fresh files survive, so a photo still being captured is never swept.
     */
    fun sweepOrphans(known: Set<String>, olderThanMillis: Long, now: Long): Int {
        val projects = projectsDir().listFiles { f -> f.isDirectory } ?: return 0
        var deleted = 0
        for (dir in projects) {
            if (dir.name !in known) {
                if (dir.deleteRecursively()) deleted++
                continue
            }
            for (file in dir.walkBottomUp().filter { it.isFile }.toList()) {
                val stale = now - file.lastModified() > olderThanMillis
                if (stale && relative(file) !in known && file.delete()) deleted++
            }
        }
        return deleted
    }

    private fun projectsDir(): File = File(canonicalRoot, "projects")

    private fun fileIn(projectId: String, sub: String, name: String): File {
        val file = child(child(projectDir(projectId), sub), name)
        file.parentFile!!.mkdirs()
        return file
    }

    private fun child(parent: File, name: String): File {
        require(name.isNotEmpty() && '/' !in name && '\\' !in name && name != "." && name != "..") {
            "Invalid path segment: $name"
        }
        return File(parent, name)
    }

    private companion object {
        const val BUFFER_SIZE = 8192
    }
}
