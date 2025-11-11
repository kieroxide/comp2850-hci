package data

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

data class Task(
    val id: Int,
    var title: String,
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

    fun update(task: Task) {
        tasks.find { it.id == task.id }?.let { it.title = task.title }
        persist()
    }
}
