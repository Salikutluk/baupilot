package de.baupilot.backend

/**
 * Appwrite adds system attributes (`$id`, `$updatedAt`, `$permissions`, ...) to every row's data. They are
 * not columns of the sync schema: the id and the timestamp come from the row itself, and the permissions are the
 * table's business. Everything else passes through.
 */
internal object AppwriteMapping {
    private val dropped = setOf("\$id", "\$updatedAt", "\$permissions")

    fun columns(data: Map<String, Any?>): Map<String, Any?> = data.filterKeys { it !in dropped }
}
