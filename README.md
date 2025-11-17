# COMP2850 HCI Starter Repository

**Server-first task manager** with Kotlin, Ktor, Pebble templates, and HTMX progressive enhancement.

[![WCAG 2.2 AA](https://img.shields.io/badge/WCAG-2.2%20AA-green)](https://www.w3.org/WAI/WCAG22/quickref/)
[![Server-First](https://img.shields.io/badge/Architecture-Server--First-blue)](https://hypermedia.systems/)
[![HTMX](https://img.shields.io/badge/HTMX-1.9.12-orange)](https://htmx.org/)

---

## Quick Start

### Local with IDE of Your Choice (Recommended)
**IntelliJ IDEA, VSCode, Eclipse, or any Kotlin-compatible IDE**

```bash
git clone [this-repo-url]
cd comp2850-hci-starter
./gradlew run
```
Open http://localhost:8080/tasks

**Requirements**: JDK 21+ installed locally

### RHEL9 Lab Machines / FENG with VSCode
**Lab machines** (local install in physical labs) or **FENG** (remote network access to lab machines):
1. Open VSCode (pre-installed with Java 21)
2. Clone repo
3. Run: `./gradlew run`

### GitHub Codespaces (Not Recommended)
While functional, Codespaces is not our preferred option:
1. Click **Code** → **Create codespace on main**
2. Wait for devcontainer to build (~2 minutes)
3. Server starts automatically on port 8080
4. Click "Open in Browser" when prompted

---

## What You Get

### ✅ Working Features
- **Task CRUD**: Add, toggle complete, delete tasks
- **Search**: Filter tasks by title (live search with HTMX)
- **Dual-mode**: Works with JavaScript ON or OFF
- **CSV storage**: Tasks persist to `data/tasks.csv`
- **Session tracking**: Anonymous session IDs for metrics (Week 9)

### ✅ Accessibility (WCAG 2.2 AA)
- Keyboard navigation (Tab, Enter, Escape)
- Screen reader support (ARIA live regions, semantic HTML)
- Skip link for quick navigation
- Focus indicators (3px outline, 3:1 contrast)
- Validation errors linked to inputs
- Works at 200% zoom

### ✅ Progressive Enhancement
- **Baseline**: Standard HTML forms, POST-Redirect-GET pattern
- **Enhanced**: HTMX fragments, out-of-band updates, no page reload
- **Detection**: Server checks `HX-Request` header and responds accordingly

---

## Architecture

### Server-First Pattern

```
┌─────────────────────────────────────────────┐
│ Browser (JavaScript OPTIONAL)              │
│  ├─ Semantic HTML baseline works           │
│  └─ HTMX adds smooth UX on top             │
└─────────────────────────────────────────────┘
                    ↕ HTTP
┌─────────────────────────────────────────────┐
│ Ktor Server (Kotlin)                        │
│  ├─ Routes check HX-Request header          │
│  ├─ Full page OR fragment response          │
│  └─ Pebble templates render HTML            │
└─────────────────────────────────────────────┘
                    ↕ Read/Write
┌─────────────────────────────────────────────┐
│ CSV Storage (Local File)                    │
│  └─ data/tasks.csv                          │
└─────────────────────────────────────────────┘
```

### Key Components

| Component | Purpose | Location |
|-----------|---------|----------|
| **Main.kt** | Server entry point, Pebble config | `src/main/kotlin/` |
| **TaskRoutes.kt** | CRUD operations, dual-mode logic | `src/main/kotlin/routes/` |
| **Task.kt** | Data model, validation | `src/main/kotlin/model/` |
| **TaskStore.kt** | CSV persistence | `src/main/kotlin/storage/` |
| **Logger.kt** | Added in Week 9 lab (students create) | `src/main/kotlin/utils/` |
| **base.peb** | HTML layout template | `src/main/resources/templates/_layout/` |
| **tasks/index.peb** | Full page view | `src/main/resources/templates/tasks/` |
| **tasks/_item.peb** | Single task partial | `src/main/resources/templates/tasks/` |

---

## Usage Guide

### Adding a Task

**No-JS mode**:
1. Enter title in form
2. Click "Add Task"
3. Page reloads with new task in list

**HTMX mode**:
1. Enter title in form
2. Click "Add Task" (or press Enter)
3. New task appears instantly, no reload
4. Status message announces "Task added successfully"

**Route**: `POST /tasks`
```kotlin
post("/tasks") {
    val title = call.receiveParameters()["title"]
    // Validate...
    if (call.isHtmxRequest()) {
        // Return HTML fragment + OOB status
    } else {
        // Redirect (PRG pattern)
    }
}
```

### Search/Filter Tasks

**HTMX live search**:
- Type in search box
- Results filter automatically (500ms debounce)
- `hx-trigger="keyup changed delay:500ms"`

**No-JS search**:
- Type query, click "Search"
- Full page reload with filtered results

**Route**: `GET /tasks/search?q={query}`

### Toggle Task Completion

**HTMX mode**:
- Click "To Do" / "Done" button
- Task updates in-place
- Status message announces change

**No-JS mode**:
- Click button, page reloads
- Task status updated in CSV

**Route**: `POST /tasks/{id}/toggle`

### Delete Task

**HTMX mode**:
- Click "Delete"
- Confirm dialog (`hx-confirm`)
- Task fades out, removed from DOM
- Status message confirms deletion

**No-JS mode**:
- Click "Delete"
- Browser confirm dialog
- Page reloads, task gone

**Route**: `POST /tasks/{id}/delete`

---

## File Structure

```
starter-repo/
├── .devcontainer/
│   ├── devcontainer.json    # Codespaces config
│   └── Dockerfile            # Java 21 + Gradle
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── Main.kt                 # Server entry point
│   │   │   ├── model/
│   │   │   │   └── Task.kt             # Data model + validation
│   │   │   ├── routes/
│   │   │   │   ├── HealthCheck.kt      # /health endpoint
│   │   │   │   └── TaskRoutes.kt       # CRUD operations
│   │   │   ├── storage/
│   │   │   │   └── TaskStore.kt        # CSV persistence
│   │   │   └── utils/
│   │   │       └── SessionUtils.kt     # Anonymous sessions (Week 6 baseline)
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── _layout/
│   │       │   │   └── base.peb        # HTML layout
│   │       │   └── tasks/
│   │       │       ├── index.peb       # Full page
│   │       │       ├── _list.peb       # Task list partial
│   │       │       └── _item.peb       # Single task partial
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── custom.css      # WCAG-compliant styles
│   │       │   └── js/
│   │       │       └── htmx-1.9.12.min.js
│   │       └── logback.xml             # Logging config
│   └── test/
│       └── kotlin/
│           └── (JUnit tests - to be added)
├── data/
│   └── tasks.csv                       # Persistent storage
├── build.gradle.kts                    # Dependencies & build config
├── settings.gradle.kts                 # Project name
├── gradlew                             # Gradle wrapper (Unix)
├── gradlew.bat                         # Gradle wrapper (Windows)
└── README.md                           # This file
```

---

## Roadmap for Labs

| Week | Feature you will implement | Files to modify |
|------|-----------------------------|-----------------|
| 7 | Inline edit (view ↔ edit mode) | `src/main/resources/templates/tasks/_edit.peb`, new routes in `routes/` |
| 8 | Pagination & filtering refinements | `utils/Pagination.kt`, `_pager.peb`, task routes |
| 9 | Instrumentation & metrics logging | `utils/Logger.kt`, `utils/Timing.kt`, routes |
| 10 | Analysis scripts & redesign packaging | `wk10/` lab pack + `templates/` updates |
| 11 | Portfolio wrap-up assets | `wk11/` lab pack |

## Development

### Running Locally

```bash
# Start server (port 8080)
./gradlew run

# Build project
./gradlew build

# Run tests (when added)
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Code Quality (Optional)

**Run all checks** (tests + linters):
```bash
./gradlew check
```

**Run individually**:
```bash
./gradlew test          # Integration tests
./gradlew detekt        # Static analysis (reports warnings)
./gradlew ktlintCheck   # Code style (reports warnings)
```

**Auto-fix formatting**:
```bash
./gradlew ktlintFormat
```

**Note**: Linters are configured to report violations as **warnings** rather than errors. Your build will succeed even with linting issues, but you'll see helpful feedback in the output. This helps you learn good practices without blocking your development during labs.

**Linting rules configured for Ktor**:
- Wildcard imports allowed for Ktor DSL packages (framework requirement)
- `TooManyFunctions` threshold raised for route files
- See `detekt.yml` and `.editorconfig` for full configuration

### Hot Reload

**Templates**: Disabled in dev (see `Main.kt: cacheActive(false)`)
- Change Pebble file → Refresh browser (no restart)

**Kotlin code**: Requires server restart
- Stop server (Ctrl+C) → `./gradlew run` again

### Testing Dual-Mode

**Test with JavaScript OFF**:
1. Browser DevTools → Settings → Debugger → Disable JavaScript
2. Navigate to http://localhost:8080/tasks
3. Verify all CRUD operations work via traditional form submission

**Test with JavaScript ON**:
1. Re-enable JavaScript
2. Open Network tab, watch for AJAX requests
3. Verify fragments returned (not full pages)
4. Check console for `HX-Request: true` header

---

## Accessibility Testing

### Keyboard Navigation
```
Tab       - Navigate between form inputs and buttons
Enter     - Submit focused button
Shift+Tab - Navigate backwards
Escape    - Close confirmation dialogs (HTMX)
```

### Screen Reader Testing

**NVDA (Windows)**:
```bash
# Download: https://www.nvaccess.org/download/
# Insert+Down Arrow - Read next line
# Insert+F7 - Elements list (links, headings, form fields)
```

**VoiceOver (macOS)**:
```bash
# Enable: System Preferences → Accessibility → VoiceOver
# Cmd+F5 - Toggle VoiceOver
# Ctrl+Option+A - Read page from top
```

**What to verify**:
- [ ] Page title announced ("COMP2850 Task Manager")
- [ ] Heading hierarchy logical (h1 → h2)
- [ ] Form labels read before inputs
- [ ] Required fields announced
- [ ] Status messages announced after actions
- [ ] Button purpose clear ("Add Task", not just "Submit")

### WCAG 2.2 AA Checklist

**1.3.1 Info & Relationships**:
- [ ] Form labels programmatically associated (`for` + `id`)
- [ ] Headings used for structure
- [ ] Lists used for task list

**2.1.1 Keyboard**:
- [ ] All functionality available via keyboard
- [ ] No keyboard traps

**2.4.1 Bypass Blocks**:
- [ ] Skip link present and functional

**2.4.7 Focus Visible**:
- [ ] Focus indicator visible (3px blue outline)

**3.3.1 Error Identification**:
- [ ] Validation errors clear and specific
- [ ] Errors linked to inputs (`aria-describedby`)

**4.1.3 Status Messages**:
- [ ] ARIA live region announces changes
- [ ] `role="status"` for success messages
- [ ] `role="alert"` for errors

---

## HTMX Patterns

### Basic Enhancement

```html
<!-- Traditional form -->
<form action="/tasks" method="post">
  <input name="title" required>
  <button type="submit">Add</button>
</form>

<!-- With HTMX enhancement -->
<form action="/tasks" method="post"
      hx-post="/tasks"              <!-- AJAX POST -->
      hx-target="#task-list"        <!-- Where to insert response -->
      hx-swap="beforeend">          <!-- Append to list -->
  <input name="title" required>
  <button type="submit">Add</button>
</form>
```

### Out-of-Band (OOB) Updates

Update multiple page areas in one response:

```html
<!-- Response HTML from server -->
<li id="task-123">...</li>              <!-- Main target -->
<div id="status" hx-swap-oob="true">    <!-- OOB update -->
  Task added successfully.
</div>
```

### Server-Side Detection

```kotlin
fun ApplicationCall.isHtmxRequest(): Boolean {
    return request.headers["HX-Request"] == "true"
}

post("/tasks") {
    if (call.isHtmxRequest()) {
        // Return fragment
        call.respondText(htmlFragment, ContentType.Text.Html)
    } else {
        // Traditional redirect
        call.respondRedirect("/tasks")
    }
}
```

---

## Privacy & Ethics

### Data Collection (Week 9 onwards)

**Logger.kt** records:
- `session_id` - Anonymous UUID (e.g., `7a9f2c`)
- `request_id` - Request trace ID
- `task_code` - Task identifier (e.g., `T1_filter`)
- `ms` - Time on task
- `js_mode` - `htmx` or `nojs`

**What is NOT collected**:
- ❌ Names, emails, IP addresses
- ❌ Browser fingerprints
- ❌ Geolocation
- ❌ Task content (titles not logged)

**Compliance**:
- UK GDPR (Data Protection Act 2018)
- Informed consent required for peer pilots
- Participants can opt out at any time
- Data stored locally only (no cloud)

---

## Troubleshooting

### Port 8080 already in use

```bash
# Find process using port 8080
lsof -i :8080
# Kill process (replace PID)
kill -9 <PID>
# Or use different port
PORT=8081 ./gradlew run
```

### Codespaces port not forwarding

1. Go to **Ports** tab in Codespaces
2. Right-click port 8080 → **Port Visibility** → **Public**
3. Click globe icon to open in browser

### HTMX not working

**Check**:
1. Browser console for JS errors
2. Network tab: Look for `HX-Request: true` header
3. Response is HTML fragment (not full page)
4. HTMX script loaded (`/static/js/htmx-1.9.12.min.js`)

**Debug**:
```html
<!-- Add to base.peb for debugging -->
<script>
  htmx.logAll(); // Logs all HTMX activity to console
</script>
```

### CSV file corrupted

```bash
# Backup current file
cp data/tasks.csv data/tasks.csv.bak

# Delete and recreate (will lose data)
rm data/tasks.csv
# Restart server (creates new file with header)
```

---

## Next Steps (Week by Week)

### Week 6: Setup & Basics
- ✅ Clone this repo
- ✅ Verify runs in IDE of choice
- ✅ Add 3 tasks, toggle completion, delete
- ✅ Test with JavaScript OFF
- ✅ Run keyboard-only test
- 📝 **Deliverable**: Screenshot showing dual-mode working

### Week 7: Accessibility Audit
- Use axe DevTools to scan `/tasks`
- Test with screen reader (NVDA/VoiceOver)
- Document findings in `a11y/audit.md`
- Fix critical issues (missing labels, contrast, keyboard traps)
- 📝 **Deliverable**: Audit report + fixes

### Week 8: Pagination & Filtering
- Add pagination (10 tasks per page)
- Implement filter by completion status
- Create template partials for reusability
- Document design decisions
- 📝 **Deliverable**: Working pagination + design doc

### Week 9: Evaluation & Metrics
- Enable Logger & Timing utilities (already present)
- Define 3-4 evaluation tasks
- Write usability test protocol
- Run peer pilots (n=4-5)
- 📝 **Deliverable**: Task 1 evidence pack

### Week 10: Analysis & Redesign
- Run `Analyse.kt` script on pilot data
- Prioritize issues using (Impact + Inclusion) - Effort
- Implement redesign (top 3 issues)
- Re-verify accessibility
- 📝 **Deliverable**: Task 2 submission package

### Week 11: Studio Critique & Portfolio
- Present redesign to peers (15min)
- Incorporate feedback
- Write self-reflection (400-600 words)
- Map evidence to learning outcomes
- 📝 **Deliverable**: Final portfolio README

---

## Resources

### HTMX
- [Official Docs](https://htmx.org/docs/)
- [Examples](https://htmx.org/examples/)
- [Hypermedia Systems Book](https://hypermedia.systems/) (Free online)

### Accessibility
- [WCAG 2.2 Quick Reference](https://www.w3.org/WAI/WCAG22/quickref/)
- [WebAIM: Screen Reader Testing](https://webaim.org/articles/screenreader_testing/)
- [GOV.UK Design System](https://design-system.service.gov.uk/) (Best practices)

### Kotlin/Ktor
- [Ktor Documentation](https://ktor.io/docs/)
- [Pebble Templates](https://pebbletemplates.io/)

### Module Resources
- `references/privacy-by-design.md` - Privacy guidance
- `references/evaluation-metrics-quickref.md` - Metrics formulas
- `references/assistive-testing-checklist.md` - Step-by-step a11y tests

---

## License

**HTMX**: BSD 2-Clause License
**Pico CSS**: MIT License
**Ktor**: Apache License 2.0

---

**Questions?** See module staff in lab or check md-book documentation in parent repository.
