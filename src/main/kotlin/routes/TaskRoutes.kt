package routes

import data.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.pebbletemplates.pebble.PebbleEngine
import java.io.StringWriter

fun Route.taskRoutes() {
    val pebble = PebbleEngine.Builder()
        .loader(io.pebbletemplates.pebble.loader.ClasspathLoader().apply {
            prefix = "templates/"
        })
        .build()

    /**
     * Helper: Check if request is from HTMX
     */
    fun ApplicationCall.isHtmx(): Boolean =
        request.headers["HX-Request"]?.equals("true", ignoreCase = true) == true

    /**
     * GET /tasks - List all tasks
     */
    get("/tasks") {
        val query = call.request.queryParameters["q"].orEmpty()
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val data = TaskRepository.search(query = query, page = page, size = 10)
        val model = mapOf("title" to "Tasks", "page" to data, "query" to query)

        // Render using local pebble engine
        val template = pebble.getTemplate("tasks/index.peb")
        val writer = StringWriter()
        template.evaluate(writer, model)
        call.respondText(writer.toString(), ContentType.Text.Html)
    }

    get("/tasks/fragment") {
        val query = call.request.queryParameters["q"].orEmpty()
        val pageNum = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val data = TaskRepository.search(query = query, page = pageNum, size = 10)

        // Render list fragment
        val listWriter = StringWriter()
        val listTemplate = pebble.getTemplate("tasks/_list.peb")
        listTemplate.evaluate(listWriter, mapOf("page" to data, "query" to query))

        // Render pager fragment
        val pagerWriter = StringWriter()
        val pagerTemplate = pebble.getTemplate("tasks/_pager.peb")
        pagerTemplate.evaluate(pagerWriter, mapOf("page" to data, "query" to query))

        val status = """<div id="status" hx-swap-oob="true">Found ${data.total} tasks.</div>"""
        call.respondText(listWriter.toString() + pagerWriter.toString() + status, ContentType.Text.Html)
    }

    /**
     * POST /tasks - Add new task (HTMX fragment or PRG)
     */
    post("/tasks") {
        // receiveParameters() returns Parameters; extract the "title" string explicitly
        val params = call.receiveParameters()
        val title = params["title"].orEmpty().trim()

        if (title.isBlank()) {
            if (call.isHtmx()) {
                val error = """<div id="status" hx-swap-oob="true" role="alert" aria-live="assertive">
                    Title is required. Please enter at least one character.
                </div>"""
                return@post call.respondText(error, ContentType.Text.Html, HttpStatusCode.BadRequest)
            } else {
                call.response.headers.append("Location", "/tasks")
                return@post call.respond(HttpStatusCode.SeeOther)
            }
        }

        val task = TaskRepository.add(title)

        if (call.isHtmx()) {
            val fragment = """<li id="task-${task.id}">
                <span>${task.title}</span>
                <form action="/tasks/${task.id}/delete" method="post" style="display: inline;"
                      hx-post="/tasks/${task.id}/delete"
                      hx-target="#task-${task.id}"
                      hx-swap="outerHTML">
                  <button type="submit" aria-label="Delete task: ${task.title}">Delete</button>
                </form>
            </li>"""
            val status = """<div id="status" hx-swap-oob="true">Task "${task.title}" added successfully.</div>"""
            return@post call.respondText(fragment + status, ContentType.Text.Html, HttpStatusCode.Created)
        }

        call.response.headers.append("Location", "/tasks")
        call.respond(HttpStatusCode.SeeOther)
    }

    /**
     * POST /tasks/{id}/delete - Delete task
     * Dual-mode: HTMX empty response or PRG redirect
     */
    post("/tasks/{id}/delete") {
        val id = call.parameters["id"]?.toIntOrNull()
        val removed = id?.let { TaskRepository.delete(it) } ?: false

        if (call.isHtmx()) {
            val message = if (removed) "Task deleted." else "Could not delete task."
            val status = """<div id="status" hx-swap-oob="true">$message</div>"""
            // Return empty content to trigger outerHTML swap (removes the <li>)
            return@post call.respondText(status, ContentType.Text.Html)
        }

        // No-JS: POST-Redirect-GET pattern (303 See Other)
        call.response.headers.append("Location", "/tasks")
        call.respond(HttpStatusCode.SeeOther)
    }

    get("/tasks/{id}/edit") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
        val task = TaskRepository.find(id) ?: return@get call.respond(HttpStatusCode.NotFound)

        if (call.isHtmx()) {
            // HTMX path: return edit fragment
            val template = pebble.getTemplate("tasks/_edit.peb")
            val model = mapOf("task" to task, "error" to null)
            val writer = StringWriter()
            template.evaluate(writer, model)
            call.respondText(writer.toString(), ContentType.Text.Html)
        } else {
            // No-JS path: full-page render with editingId
            val model =
                mapOf(
                    "title" to "Tasks",
                    "tasks" to TaskRepository.all(),
                    "editingId" to id,
                    "errorMessage" to null,
                )
            val template = pebble.getTemplate("tasks/index.peb")
            val writer = StringWriter()
            template.evaluate(writer, model)
            call.respondText(writer.toString(), ContentType.Text.Html)
        }
    }

    post("/tasks/{id}/edit") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.NotFound)
        val task = TaskRepository.find(id) ?: return@post call.respond(HttpStatusCode.NotFound)

        val newTitle = call.receiveParameters()["title"].orEmpty().trim()

        // Validation
        if (newTitle.isBlank()) {
            if (call.isHtmx()) {
                // HTMX path: return edit fragment with error
                val template = pebble.getTemplate("tasks/_edit.peb")
                val model =
                    mapOf(
                        "task" to task,
                        "error" to "Title is required. Please enter at least one character.",
                    )
                val writer = StringWriter()
                template.evaluate(writer, model)
                return@post call.respondText(writer.toString(), ContentType.Text.Html, HttpStatusCode.BadRequest)
            } else {
                // No-JS path: redirect with error flag
                return@post call.respondRedirect("/tasks/$id/edit?error=blank")
            }
        }

        // Update task
        task.title = newTitle
        TaskRepository.update(task)

        if (call.isHtmx()) {
            // HTMX path: return view fragment + OOB status
            val viewTemplate = pebble.getTemplate("tasks/_item.peb")
            val viewWriter = StringWriter()
            viewTemplate.evaluate(viewWriter, mapOf("task" to task))

            val status = """<div id="status" hx-swap-oob="true">Task "${task.title}" updated successfully.</div>"""

            return@post call.respondText(viewWriter.toString() + status, ContentType.Text.Html)
        }

        // No-JS path: PRG redirect
        call.respondRedirect("/tasks")
    }

    get("/tasks/{id}/edit") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
        val task = TaskRepository.find(id) ?: return@get call.respond(HttpStatusCode.NotFound)
        val errorParam = call.request.queryParameters["error"]

        val errorMessage =
            when (errorParam) {
                "blank" -> "Title is required. Please enter at least one character."
                else -> null
            }

        if (call.isHtmx()) {
            val template = pebble.getTemplate("tasks/_edit.peb")
            val model = mapOf("task" to task, "error" to errorMessage)
            val writer = StringWriter()
            template.evaluate(writer, model)
            call.respondText(writer.toString(), ContentType.Text.Html)
        } else {
            val model =
                mapOf(
                    "title" to "Tasks",
                    "tasks" to TaskRepository.all(),
                    "editingId" to id,
                    "errorMessage" to errorMessage,
                )
            val template = pebble.getTemplate("tasks/index.peb")
            val writer = StringWriter()
            template.evaluate(writer, model)
            call.respondText(writer.toString(), ContentType.Text.Html)
        }
    }
    get("/tasks/{id}/view") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
        val task = TaskRepository.find(id) ?: return@get call.respond(HttpStatusCode.NotFound)

        // HTMX path only (cancel is just a link to /tasks in no-JS)
        val template = pebble.getTemplate("tasks/_item.peb")
        val model = mapOf("task" to task)
        val writer = StringWriter()
        template.evaluate(writer, model)
        call.respondText(writer.toString(), ContentType.Text.Html)
    }
}