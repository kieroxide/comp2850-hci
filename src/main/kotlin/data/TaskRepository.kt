package data

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * NOTE FOR NON-INTELLIJ IDEs (VSCode, Eclipse, etc.):
 * IntelliJ IDEA automatically adds imports as you type. If using a different IDE,
 * you may need to manually add imports. The commented imports below show what you'll need
 * for future weeks. Uncomment them as needed when following the lab instructions.
 *
 * When using IntelliJ: You can ignore the commented imports below - your IDE will handle them.
 */

// Week 7+ imports (no new imports needed for find/update methods)

// Week 8+ imports (search functionality added, no new imports needed)

// Week 10+ evolution note:
// In the solution repo, this file is split into two separate files:
//
// 1. model/Task.kt (data class with validation):
//    import java.time.LocalDateTime
//    import java.time.format.DateTimeFormatter
//    import java.util.UUID
//
// 2. storage/TaskStore.kt (CSV persistence using Apache Commons CSV):
//    import model.Task
//    import org.apache.commons.csv.CSVFormat
//    import org.apache.commons.csv.CSVParser
//    import org.apache.commons.csv.CSVPrinter
//    import java.io.FileReader
//    import java.io.FileWriter
//    import java.time.format.DateTimeParseException

data class Task(
    val id: Int,
    var title: String,
)

data class Page<T>(
    val items: List<T>,
    val total: Int,
    val size: Int,
    val number: Int,
    val totalPages: Int
)

object TaskRepository {
    private val file = File("data/tasks.csv")
    private val tasks = mutableListOf<Task>()
    private val idCounter = AtomicInteger(1)

    init {
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            file.writeText("id,title\n")
        } else {
            file.readLines().drop(1).forEach { line ->
                val parts = line.split(",", limit = 2)
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull() ?: return@forEach
                    tasks.add(Task(id, parts[1]))
                    idCounter.set(maxOf(idCounter.get(), id + 1))
                }
            }
        }
    }

    fun all(): List<Task> = tasks.toList()

    fun add(title: String): Task {
        val task = Task(idCounter.getAndIncrement(), title)
        tasks.add(task)
        persist()
        return task
    }

    fun delete(id: Int): Boolean {
        val removed = tasks.removeIf { it.id == id }
        if (removed) persist()
        return removed
    }

    private fun persist() {
        file.writeText("id,title\n" + tasks.joinToString("\n") { "${it.id},${it.title}" })
    }

    fun find(id: Int): Task? = tasks.find { it.id == id }


    fun search(query: String = "", page: Int = 1, size: Int = 10): Page<Task> {
        val filtered = if (query.isBlank()) tasks else tasks.filter { it.title.contains(query, ignoreCase = true) }

        val total = filtered.size
        val pageSize = maxOf(1, size)
        val totalPages = maxOf(1, (total + pageSize - 1) / pageSize)
        val pageNumber = page.coerceIn(1, totalPages)

        val fromIndex = (pageNumber - 1) * pageSize
        val toIndex = minOf(fromIndex + pageSize, total)
        val pageItems = if (fromIndex >= total) emptyList() else filtered.subList(fromIndex, toIndex)

        return Page(items = pageItems, total = total, size = pageSize, number = pageNumber, totalPages = totalPages)
    }

    fun update(task: Task) {
        tasks.find { it.id == task.id }?.let { it.title = task.title }
        persist()
    }
}
