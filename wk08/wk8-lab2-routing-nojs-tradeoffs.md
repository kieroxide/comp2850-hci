# Week 8 • Lab 2 — Wire Routes, Verify No-JS Parity, Capture Trade-offs

<div class="badges">
  <span class="badge lo">LO1</span>
  <span class="badge lo">LO5</span>
  <span class="badge lo">LO6</span>
  <span class="badge lo">LO7</span>
  <span class="badge lo">LO9</span>
  <span class="badge lo">LO10</span>
  <span class="badge acm">HCI-Design</span>
  <span class="badge acm">System Design</span>
  <span class="badge acm">Evaluation</span>
  <span class="badge wcag">WCAG 2.2 AA</span>
</div>

> **Terminology Note**: Avoid "user." Prefer **person**, **participant**, **customer**.

---

## Before Lab: Required Reading (15 mins)

📖 **Essential**:
- [HTMX: Server Errors](https://htmx.org/essays/handling-server-errors/)
- [HTMX: hx-confirm](https://htmx.org/attributes/hx-confirm/)
- [WebAIM: Forms - Accessible Error Identification](https://webaim.org/articles/formvalidation/)

📖 **Contextual**:
- Review `../../references/htmx-pattern-cheatsheet.md` (OOB pattern)
- Review `../../references/assistive-testing-checklist.md`
- Review Week 7 Lab 1 inline edit implementation

---

## Introduction: From Prototype to Production-Ready

Yesterday (Week 8 Lab 1) you added pagination and filtering. The **happy path** works: people can browse, filter, and page through tasks.

**Today's reality check**: What happens when:
- Someone submits an empty form?
- JavaScript is disabled or fails to load?
- A person uses only keyboard navigation?
- Network drops mid-request?

**Production systems must handle failure gracefully**. This lab hardens your prototype:

1. **Validation**: Server-side validation with accessible error messaging
2. **Parity**: Identical functionality with/without JavaScript
3. **Testing**: Repeatable verification scripts
4. **Documentation**: Trade-offs, risks, mitigations

**HCI Connection**: Robust error handling is an **inclusion imperative**—validation errors disproportionately affect people with cognitive disabilities, screen reader users, and those with unreliable connectivity. WCAG 3.3 (Input Assistance) requires clear, accessible error identification.

---

## Learning Outcomes

By end of lab:
1. **Validate** form inputs with accessible error messaging (LO5, LO9)
2. **Ensure** create/edit/delete work identically with and without JavaScript (LO6, LO10)
3. **Test** no-JS parity with repeatable scripts (LO7)
4. **Document** design trade-offs and mitigation strategies (LO1, LO10)
5. **Verify** accessibility of error states with keyboard and screen reader (LO9)

Maps to ACM: HCI-Design, System Design, Evaluation
Maps to WCAG: 3.3.1 (Error Identification), 3.3.3 (Error Suggestion), 4.1.3 (Status Messages)

---

## Key Concepts

### Server-Side Validation

> **Server-Side Validation** [GLOSSARY]
>
> Validation logic executed on the server (not browser). **Always required**, even with client-side validation.
>
> **Why**:
> - Client-side can be bypassed (disabled JS, malicious requests)
> - Security boundary: never trust client input
> - Accessibility: server can return appropriate error responses for HTMX and no-JS paths
>
> **Pattern**:
> ```kotlin
> val title = call.receiveParameters()["title"].orEmpty().trim()
> if (title.isBlank()) {
>     // Return error response (HTML for HTMX, redirect for no-JS)
> }
> ```
>
> **HCI Connection**: Server validation enables **progressive enhancement**—same validation logic serves both enhanced (HTMX) and baseline (HTML-only) experiences.
>
> 🔗 [OWASP: Input Validation](https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html)

### Post-Redirect-Get (PRG) Pattern

> **Post-Redirect-Get (PRG)** [GLOSSARY]
>
> Respond to successful POST with redirect (303) instead of HTML. Prevents duplicate submissions on browser refresh.
>
> **Flow**:
> 1. Person submits form → `POST /tasks`
> 2. Server validates, saves task
> 3. Server responds `303 See Other` → `Location: /tasks`
> 4. Browser follows redirect → `GET /tasks`
> 5. Person sees updated task list
>
> **Why**:
> - Refresh reloads `GET /tasks` (safe), not `POST /tasks` (duplicate)
> - Back button works predictably
> - URL reflects current state
>
> **Kotlin (Ktor)**:
> ```kotlin
> post("/tasks") {
>     // Validate and save...
>     if (!call.isHtmx()) {
>         call.respondRedirect("/tasks", permanent = false)
>     }
> }
> ```
>
> **HCI Connection**: PRG reduces cognitive load—back/forward/refresh do what people expect. Critical for accessibility (people often use back button to recover from errors).
>
> 🔗 [Wikipedia: Post/Redirect/Get](https://en.wikipedia.org/wiki/Post/Redirect/Get)

### Dual-Path Architecture

> **Dual-Path Architecture** [GLOSSARY]
>
> Single server route handles both HTMX (enhanced) and no-JS (baseline) requests with different responses.
>
> **Detection**:
> ```kotlin
> fun ApplicationCall.isHtmx(): Boolean =
>     request.headers["HX-Request"]?.equals("true", ignoreCase = true) == true
> ```
>
> **Response Strategy**:
> - **HTMX path**: Return HTML fragment + OOB status
> - **No-JS path**: Return full page or redirect (PRG)
>
> **Example**:
> ```kotlin
> post("/tasks") {
>     val title = call.receiveParameters()["title"].orEmpty().trim()
>     if (title.isBlank()) {
>         if (call.isHtmx()) {
>             val status = """<div id="status" hx-swap-oob="true">Title is required.</div>"""
>             return@post call.respondText(status, ContentType.Text.Html, HttpStatusCode.BadRequest)
>         } else {
>             return@post call.respondRedirect("/tasks?error=title")
>         }
>     }
>     // Success path...
> }
> ```
>
> **HCI Connection**: Dual-path ensures **functional parity** across technical contexts—equivalent experience regardless of JavaScript availability.
>
> 🔗 [hypermedia.systems: Progressive Enhancement](https://hypermedia.systems/progressive-enhancement/)

### Accessible Error Identification

> **Accessible Error Identification** [GLOSSARY]
>
> WCAG 3.3.1 (Level A): Errors must be identified in text and associated with the problematic input.
>
> **Requirements**:
> 1. Error message in text (not just colour/icon)
> 2. Programmatic association (`aria-describedby` or `aria-errormessage`)
> 3. Focus management (keyboard users land on/near error)
> 4. Screen reader announcement (live region or summary)
>
> **Pattern (inline)**:
> ```html
> <label for="title">Task title</label>
> <input id="title" name="title" aria-describedby="title-error" aria-invalid="true">
> <p id="title-error" role="alert">Title is required. Please enter at least one character.</p>
> ```
>
> **Pattern (summary)**:
> ```html
> <div role="alert" aria-live="assertive" tabindex="-1" id="error-summary">
>   <h2>There is a problem</h2>
>   <ul>
>     <li><a href="#title">Title is required</a></li>
>   </ul>
> </div>
> ```
>
> **HCI Connection**: Clear error identification reduces cognitive load and is critical for screen reader users who can't see visual error states.
>
> 🔗 [WCAG 3.3.1: Error Identification](https://www.w3.org/WAI/WCAG22/Understanding/error-identification.html)
> 🔗 [GOV.UK: Error Message Pattern](https://design-system.service.gov.uk/components/error-message/)

### No-JS Parity Testing

> **No-JS Parity Testing** [GLOSSARY]
>
> Verifying that all functionality works identically with JavaScript disabled.
>
> **Why**:
> - JavaScript may fail to load (network, CDN, blocker)
> - Some organisations disable JS for security
> - Testing baseline ensures server-first architecture is sound
> - Legal requirement in some jurisdictions (accessibility standards)
>
> **Methods**:
> 1. **Browser DevTools**: Settings → Disable JavaScript
> 2. **Command-line**: `curl`, `wget`, `httpie`
> 3. **Automated**: Playwright with JS disabled, or server-side rendering tests
>
> **Checklist items**:
> - Forms submit and validate
> - Navigation works (links, pagination)
> - Error messages appear and are accessible
> - Success feedback is visible
> - Browser history behaves predictably
>
> **HCI Connection**: No-JS parity is **progressive enhancement** verification—ensures you're truly building server-first, not HTMX-first.
>
> 🔗 [GOV.UK: Building a resilient frontend](https://www.gov.uk/service-manual/technology/using-progressive-enhancement)

---

## Activity A: Harden Create Flow (35 min)

Yesterday you built `POST /tasks` for the happy path. Now add validation and dual-path error handling.

### Step 1: Add server-side validation (10 min)

**Current code** (Week 8 Lab 1):
```kotlin
post("/tasks") {
    val title = call.receiveParameters()["title"].orEmpty().trim()
    val task = repo.add(title)
    if (call.isHtmx()) {
        val item = PebbleRender.render("tasks/_item.peb", mapOf("t" to task))
        val status = """<div id="status" hx-swap-oob="true">Added "${task.title}".</div>"""
        return@post call.respondText(item + status, ContentType.Text.Html)
    }
    call.respondRedirect("/tasks")
}
```

**Problem**: No validation. Empty/whitespace titles are accepted.

**Add validation**:
```kotlin
post("/tasks") {
    val title = call.receiveParameters()["title"].orEmpty().trim()

    // Validation
    if (title.isBlank()) {
        if (call.isHtmx()) {
            val status = """<div id="status" hx-swap-oob="true">Title is required.</div>"""
            return@post call.respondText(status, ContentType.Text.Html, HttpStatusCode.BadRequest)
        } else {
            // No-JS: redirect with error query param
            return@post call.respondRedirect("/tasks?error=title")
        }
    }

    if (title.length > 200) {
        if (call.isHtmx()) {
            val status = """<div id="status" hx-swap-oob="true">Title too long (max 200 chars).</div>"""
            return@post call.respondText(status, ContentType.Text.Html, HttpStatusCode.BadRequest)
        } else {
            return@post call.respondRedirect("/tasks?error=title&msg=too_long")
        }
    }

    // Success path
    val task = repo.add(title)
    if (call.isHtmx()) {
        val item = PebbleRender.render("tasks/_item.peb", mapOf("t" to task))
        val status = """<div id="status" hx-swap-oob="true">Added "${task.title}".</div>"""
        return@post call.respondText(item + status, ContentType.Text.Html)
    }
    call.respondRedirect("/tasks")
}
```

**Key points**:
- Always validate on server (never trust client)
- Return `400 Bad Request` for HTMX errors (semantic HTTP)
- Use query parameters for no-JS errors (preserve form state)

### Step 2: Update template to show no-JS errors (15 min)

**Update `templates/tasks/index.peb`**:

Add error summary at top (before form):
```twig
{% if error %}
<div role="alert" aria-live="assertive" class="error-summary" id="error-summary" tabindex="-1">
  <h2>There is a problem</h2>
  <ul>
    {% if error == "title" %}
    <li><a href="#title">{% if msg == "too_long" %}Title is too long (max 200 characters){% else %}Title is required{% endif %}</a></li>
    {% endif %}
  </ul>
</div>
{% endif %}
```

**Update form input**:
```twig
<label for="title">Task title</label>
<input id="title" name="title" type="text"
       {% if error == "title" %}aria-invalid="true" aria-describedby="title-error"{% endif %}
       required>
{% if error == "title" %}
<p id="title-error" class="error-message">
  {% if msg == "too_long" %}Title is too long (max 200 characters){% else %}Title is required{% endif %}
</p>
{% endif %}
```

**Update GET /tasks route** to pass error params:
```kotlin
get("/tasks") {
    val error = call.request.queryParameters["error"]
    val msg = call.request.queryParameters["msg"]
    val q = call.request.queryParameters["q"].orEmpty()
    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
    val data = repo.search(q, page)

    val model = mapOf(
        "title" to "Tasks",
        "page" to data,
        "q" to q,
        "error" to error,
        "msg" to msg
    )

    val html = PebbleRender.render("tasks/index.peb", model)
    call.respondText(html, ContentType.Text.Html)
}
```

**Add CSS for error states** (`static/style.css` or Pico CSS variables):
```css
.error-summary {
  background: #fef7f7;
  border: 2px solid #d32f2f;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}

.error-summary h2 {
  color: #d32f2f;
  font-size: 1.2rem;
  margin-top: 0;
}

.error-message {
  color: #d32f2f;
  font-size: 0.9rem;
  margin-top: 0.25rem;
}

input[aria-invalid="true"] {
  border-color: #d32f2f;
}
```

### Step 3: Test both paths (10 min)

**HTMX path**:
1. With JS enabled, submit empty form
2. Expect: Status message "Title is required." appears in `#status` live region
3. Form remains on page, input keeps focus
4. Try typing and submitting—should succeed

**No-JS path**:
1. Disable JS (DevTools → Settings → Disable JavaScript)
2. Submit empty form
3. Expect: Full page reload with error summary at top
4. Click error link → focus moves to `#title` input
5. Fill form, submit → redirect to `/tasks` with new task visible

✋ **Stop and check**:
- [ ] Both paths show validation errors
- [ ] HTMX errors use live region (announced by screen reader)
- [ ] No-JS errors use summary with focusable link
- [ ] Error messages are specific and actionable
- [ ] Success path still works for both

---

## Activity B: Harden Delete Flow (25 min)

Delete is destructive—must confirm and handle both JS/no-JS.

### Step 1: Add confirmation dialog (10 min)

**Update `templates/tasks/_item.peb`**:

Current button:
```twig
<button type="button" hx-delete="/tasks/{{ t.id }}" hx-target="#task-{{ t.id }}" hx-swap="outerHTML">
  Delete
</button>
```

**Add confirmation + accessible label**:
```twig
<button type="button"
        hx-delete="/tasks/{{ t.id }}"
        hx-target="#task-{{ t.id }}"
        hx-swap="outerHTML"
        hx-confirm="Delete the task '{{ t.title }}'?"
        aria-label="Delete task: {{ t.title }}">
  Delete
</button>
```

**Why**:
- `hx-confirm` prompts before request (HTMX feature)
- `aria-label` provides context for screen readers (otherwise just "Delete Delete Delete..." in a list)

### Step 2: Add no-JS delete support (15 min)

**Problem**: `hx-delete` requires JavaScript. No-JS needs a form.

**Dual-path solution**:

Replace button with **form** that works both ways:
```twig
<form action="/tasks/{{ t.id }}/delete" method="post" style="display: inline;">
  <button type="submit"
          hx-delete="/tasks/{{ t.id }}"
          hx-target="#task-{{ t.id }}"
          hx-swap="outerHTML"
          hx-confirm="Delete the task '{{ t.title }}'?"
          aria-label="Delete task: {{ t.title }}">
    Delete
  </button>
</form>
```

**Explanation**:
- **No-JS**: Form submits `POST /tasks/{id}/delete`
- **HTMX**: `hx-delete` intercepts, sends `DELETE /tasks/{id}` instead
- Both paths work!

**Update routes** (`src/main/kotlin/routes/Tasks.kt`):

```kotlin
// HTMX path (HTTP DELETE)
delete("/tasks/{id}") {
    val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
    val task = repo.get(id)
    repo.delete(id)

    val status = """<div id="status" hx-swap-oob="true">Deleted "${task?.title ?: "task"}".</div>"""
    // Return empty string (outerHTML swap removes the <li>)
    call.respondText(status, ContentType.Text.Html)
}

// No-JS path (POST fallback)
post("/tasks/{id}/delete") {
    val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
    repo.delete(id)
    call.respondRedirect("/tasks")
}
```

**Trade-off to document**: No-JS delete has no confirmation (browser POST doesn't prompt). Options:
1. Accept the trade-off (document in constraints doc)
2. Add intermediate confirmation page (`GET /tasks/{id}/delete/confirm`)
3. Use client-side confirmation (but that requires JS...)

For this lab, **accept and document** the trade-off.

✋ **Stop and check**:
- [ ] HTMX delete shows confirmation dialog
- [ ] After confirm, task disappears with status message
- [ ] No-JS delete submits form and redirects
- [ ] Screen reader announces "Deleted [title]" (HTMX) or reads updated page (no-JS)

---

## Activity C: Script No-JS Verification (20 min)

Create a repeatable test script so anyone (teammate, tutor, future you) can verify parity.

### Step 1: Create verification document (15 min)

**Create `wk08/lab-w8/scripts/nojs-check.md`**:

```markdown
# No-JS Parity Verification Script — Week 8

**Purpose**: Verify all task flows work identically with JavaScript disabled.

**Setup**:
1. Open Chrome DevTools → Settings → Preferences → Disable JavaScript
2. Alternatively: Use Firefox → Settings → Privacy & Security → Permissions → Uncheck JavaScript
3. Hard refresh (Ctrl+Shift+R / Cmd+Shift+R) to clear cached JS

---

## Test 1: Add Task (Happy Path)

**Steps**:
1. Navigate to `/tasks`
2. Enter title "No-JS test task"
3. Click "Add Task"

**Expected**:
- Full page reload (check Network tab: only HTML request)
- New task appears in list
- URL remains `/tasks` (PRG redirect)
- No error messages

**Evidence**: Screenshot of task list with new task visible.

**Result**: [ ] Pass  [ ] Fail

---

## Test 2: Add Task (Validation Error)

**Steps**:
1. Leave title field empty
2. Click "Add Task"

**Expected**:
- Full page reload
- URL shows `/tasks?error=title`
- Error summary appears at top: "There is a problem"
- Link to "#title" input
- Input has red border, error message below
- Focus can navigate to error link and then to input

**Evidence**: Screenshot of error summary and highlighted input.

**Result**: [ ] Pass  [ ] Fail

---

## Test 3: Filter Tasks

**Steps**:
1. Add tasks "Alpha", "Bravo", "Charlie"
2. Enter "ra" in filter box
3. Click "Apply" or submit form

**Expected**:
- Full page reload
- URL shows `/tasks?q=ra&page=1`
- Only "Bravo" and "Charlie" visible (partial match)
- Result count updates: "Showing 2 tasks"

**Evidence**: Screenshot of filtered results with URL visible.

**Result**: [ ] Pass  [ ] Fail

---

## Test 4: Pagination

**Steps**:
1. Add 15 tasks (assuming page size = 10)
2. Navigate to page 2 using "Next" link

**Expected**:
- Full page reload
- URL shows `/tasks?page=2`
- Tasks 11-15 visible
- "Previous" link appears
- "Next" link disabled or hidden

**Evidence**: Screenshot of page 2 with navigation controls.

**Result**: [ ] Pass  [ ] Fail

---

## Test 5: Edit Task (inline)

**Steps**:
1. Click "Edit" on a task
2. Change title to "Updated via no-JS"
3. Submit

**Expected**:
- Full page reload (or inline form renders in server response)
- URL remains `/tasks` or shows task context
- Updated title visible
- No JavaScript errors (none should exist)

**Evidence**: Screenshot of updated task.

**Result**: [ ] Pass  [ ] Fail

---

## Test 6: Delete Task

**Steps**:
1. Click "Delete" button on a task

**Expected**:
- Form submits to `POST /tasks/{id}/delete`
- Full page reload
- Task removed from list
- URL redirects to `/tasks`
- **No confirmation** (documented trade-off)

**Evidence**: Screenshot of task list with item removed.

**Result**: [ ] Pass  [ ] Fail

---

## Test 7: Keyboard Navigation

**Steps**:
1. Tab through entire page (skip link → form → tasks → pagination)
2. Ensure visible focus indicator at each stop
3. Activate "Add Task" with Enter
4. Navigate to error link with Tab, activate with Enter

**Expected**:
- Focus order matches visual order
- All interactive elements reachable
- Enter activates links/buttons
- Focus visible on all elements

**Evidence**: Notes on tab order, screenshot of visible focus.

**Result**: [ ] Pass  [ ] Fail

---

## Test 8: Browser History

**Steps**:
1. Start at `/tasks`
2. Add task (redirects to `/tasks`)
3. Filter to "test" (reloads to `/tasks?q=test`)
4. Go to page 2 (reloads to `/tasks?q=test&page=2`)
5. Click browser Back button twice

**Expected**:
- Back #1 → `/tasks?q=test&page=1` (filtered, page 1)
- Back #2 → `/tasks?q=test` (filtered, page 1 implicit)
- Back #3 → `/tasks` (no filter)
- History stack matches user intent

**Evidence**: Notes on history behaviour.

**Result**: [ ] Pass  [ ] Fail

---

## Summary

**Passed**: _____ / 8
**Failed**: _____ / 8

**Issues found**: (log in `backlog/backlog.csv` with IDs wk8-XX)

**Notes**:
- Attach screenshots to `evidence/wk8/nojs-parity/`
- Update `docs/prototyping-constraints.md` with any new trade-offs discovered
- Re-run this script after any route or template changes

---

**Verified by**: __________
**Date**: __________
```

### Step 2: Run the script (5 min)

Execute Tests 1-6 yourself. Capture screenshots in `evidence/wk8/nojs-parity/`.

Log any failures in `backlog/backlog.csv`:
```csv
id,week,priority,category,description,wcag,status
wk8-01,8,high,functionality,"Delete has no confirmation in no-JS mode",,open
wk8-02,8,medium,ux,"Filter form submits on Enter but no visual feedback",,open
```

✋ **Stop and check**:
- [ ] Script documents all major flows
- [ ] Script is repeatable by someone else
- [ ] Evidence captured in structured folder
- [ ] Failures logged in backlog

---

## Activity D: Document Trade-offs and Constraints (15 min)

Update `wk08/docs/prototyping-constraints.md` with a section on **dual-path architecture trade-offs**.

**Create or extend the file**:

```markdown
# Prototyping Constraints and Trade-offs — Week 8

## Dual-Path Architecture

### Design Decision
Every route handles both HTMX (enhanced) and no-JS (baseline) requests.

### Benefits
- **Inclusion**: Works for everyone regardless of JS availability
- **Resilience**: Graceful degradation if JS fails to load
- **Testing**: Baseline path proves server-first architecture is sound
- **Progressive enhancement**: Start with accessible baseline, enhance with HTMX

### Costs
- **Code complexity**: Each route has conditional logic (`if (call.isHtmx())`)
- **Response duplication**: Must generate both fragments and full pages
- **Testing burden**: Every feature requires two test paths
- **Performance**: No-JS path triggers full page reloads (slower perceived performance)

### Risks
- **Divergence**: HTMX and no-JS paths could drift if not tested regularly
- **Error handling**: Easy to forget no-JS error path during rapid development
- **Template maintenance**: Changes to page structure must update both full templates and fragments

### Mitigations
- ✅ **Verification script**: `scripts/nojs-check.md` provides repeatable tests
- ✅ **Shared partials**: `_item.peb`, `_list.peb` used by both paths (single source of truth)
- ✅ **Backlog tracking**: Log parity issues immediately (see `backlog/backlog.csv`)
- ✅ **Weekly retesting**: Run no-JS script before each commit
- ⚠️ **Automated tests** (future): Playwright tests with JS disabled

---

## Validation Strategy

### Design Decision
All validation on server. No client-side validation (no `<input required>` enforcement).

### Benefits
- **Security**: Client-side validation can be bypassed (view source, disable JS, curl)
- **Consistency**: Same validation rules for HTMX and no-JS paths
- **Accessibility**: Server returns appropriate error format for each context

### Costs
- **Latency**: Must wait for server round-trip to see validation errors
- **UX**: No instant feedback on typos (could add client-side hints later)

### Risks
- **Frustration**: People might repeatedly submit invalid forms before reading error
- **Network dependency**: Offline users can't get any feedback

### Mitigations
- ✅ **Clear error messages**: Specific, actionable text ("Title is required" not "Invalid input")
- ✅ **Accessible error identification**: WCAG 3.3.1 compliance (aria-invalid, aria-describedby, role=alert)
- ✅ **Focus management**: Error link navigates directly to problematic field
- 🔮 **Future enhancement**: Add client-side hints (maxlength indicator, real-time char count) as progressive enhancement

---

## Delete Confirmation

### Design Decision
HTMX path uses `hx-confirm` (browser confirmation dialog). No-JS path has no confirmation.

### Benefits
- **HTMX**: Prevents accidental deletions for JS-enabled users
- **Implementation simplicity**: No intermediate confirmation page needed

### Costs
- **Inconsistency**: Different UX depending on JS availability
- **Accessibility**: Browser confirm dialogs are not customisable (can't improve copy)

### Risks
- **Accidental deletion**: No-JS users might delete tasks by mistake
- **Compliance**: Depending on context, irreversible actions might require confirmation (WCAG 2.2.1 Timing Adjustable, 3.3.4 Error Prevention)

### Mitigations
- ✅ **Documentation**: Trade-off explicitly noted in constraints doc
- ✅ **Backlog item**: Consider adding `/tasks/{id}/delete/confirm` page for no-JS (low priority)
- ⚠️ **User research** (Week 9): Test whether delete accidents occur in pilots
- 🔮 **Future option**: Add "Undo" feature (restore from soft-delete within 30s)

---

## State Management

### Design Decision
Use query parameters for filter and page state (`?q=search&page=2`).

### Benefits
- **Shareable**: URL captures full state (can bookmark or share filtered view)
- **History**: Back/forward buttons work predictably
- **No-JS compatible**: Query params work without JavaScript
- **Stateless server**: No session state needed for pagination

### Costs
- **URL pollution**: Long query strings for complex filters
- **Encoding**: Must properly encode/decode special characters
- **Analytics**: Harder to track "unique pages" if many query variations exist

### Risks
- **Drift**: If fragment requests use different query params than full page, state can desync
- **Limits**: Some servers/proxies have URL length limits (~2000 chars)

### Mitigations
- ✅ **Consistent param names**: Use `q` and `page` everywhere
- ✅ **Validation**: Sanitise and bound page numbers (reject negative, exceeds max)
- ✅ **Encoding**: Use `call.request.queryParameters` (Ktor handles encoding)
- 🔮 **Future**: If filters grow complex, consider POST with session state

---

## Performance Considerations

### Active Search Debounce
- **Decision**: 300ms debounce on `hx-trigger="keyup changed delay:300ms"`
- **Benefit**: Reduces server load (doesn't fire on every keystroke)
- **Cost**: 300ms perceived latency before filter applies
- **Mitigation**: Show loading indicator (`hx-indicator`) during request

### Page Size
- **Decision**: 10 tasks per page
- **Benefit**: Fast page loads, manageable scroll
- **Cost**: More pagination clicks for large datasets
- **Mitigation**: Could add page size selector (10/25/50) in future

### Fragment Size
- **Decision**: Return `_list + _pager + status` (~2-5KB) instead of full page (~15KB)
- **Benefit**: 70% bandwidth reduction on filter/pagination
- **Cost**: Requires dual-path logic
- **Measurement**: Use browser DevTools Network tab to verify savings

---

## Evidence and Testing

**Verification scripts**: `wk08/lab-w8/scripts/nojs-check.md`
**Evidence folder**: `evidence/wk8/nojs-parity/`
**Backlog references**: IDs `wk8-01` to `wk8-XX`

**Review schedule**: Re-run parity tests after:
- Any route changes
- Template structure updates
- Before Week 9 instrumentation (ensure baseline is solid)
- Before Gradescope Task 1 submission

**Ownership**: Entire team responsible for verifying parity. Pair on changes: one person tests HTMX, another tests no-JS.
```

✋ **Stop and check**:
- [ ] Document includes design decisions, benefits, costs, risks, mitigations
- [ ] Each trade-off links to evidence or backlog items
- [ ] Future improvements noted with 🔮 prefix
- [ ] Clear ownership and review schedule

---

## Activity E: Retest with Assistive Technology (20 min)

Now that error handling and delete confirmation are in place, verify accessibility.

### Step 1: Keyboard testing (5 min)

**Test error recovery**:
1. Submit empty form
2. Tab to error summary link
3. Press Enter → focus moves to `#title` input
4. Type title, press Enter to submit
5. Verify success message appears

**Test delete**:
1. Tab to Delete button
2. Press Enter → confirmation dialog appears
3. Press Enter again to confirm
4. Verify screen reader announces deletion status

✋ Check:
- [ ] Error link is keyboard-accessible
- [ ] Error link moves focus to input when activated
- [ ] Delete button keyboard-accessible
- [ ] Confirmation dialog keyboard-accessible

### Step 2: Screen reader testing (10 min)

**Use NVDA (Windows) or Orca (Linux)**:

**Test error announcement (HTMX)**:
1. Forms mode (`F` key in NVDA) → navigate to Add Task form
2. Leave title empty, submit
3. Listen for status region update: "Title is required."
4. Verify SR announces the error (live region should trigger)

**Test error summary (no-JS)**:
1. Disable JS, submit empty form
2. SR should read: "Alert. There is a problem. List with 1 item. Link. Title is required."
3. Activate link → SR announces: "Edit. Title."

**Test delete status**:
1. Navigate to Delete button
2. SR announces: "Delete task: [title]. Button."
3. Activate, confirm
4. SR announces: "Deleted [title]." (from live region)

✋ Check:
- [ ] HTMX errors announced via live region
- [ ] No-JS errors announced via alert role
- [ ] Delete button has accessible name
- [ ] Delete confirmation accessible
- [ ] Status messages announced

### Step 3: No-JS screen reader test (5 min)

Disable JS and repeat error flow with screen reader:
1. Submit empty form (no-JS)
2. SR reads error summary
3. Activate error link
4. Verify focus lands on input
5. Fill form, submit
6. Verify SR reads updated page

✋ Check:
- [ ] Error summary is first thing announced after page load
- [ ] Error links are navigable
- [ ] Focus management works
- [ ] Success state is clear

**Document findings**: Add any issues to `backlog/backlog.csv`. Capture screen reader output in `evidence/wk8/sr-testing.txt`.

---

## Commit & Reflect (10 min)

### Commit message

```bash
git add src/main/kotlin/routes/Tasks.kt templates/tasks/index.peb templates/tasks/_item.peb wk08/lab-w8/scripts/nojs-check.md wk08/docs/prototyping-constraints.md evidence/wk8/ backlog/backlog.csv

git commit -m "$(cat <<'EOF'
wk8s2: dual-path validation, no-JS parity, trade-offs doc

- Added server-side validation for create (blank, max length)
- Dual-path error handling: HTMX (OOB status) vs no-JS (redirect + summary)
- Accessible error identification: aria-invalid, aria-describedby, role=alert
- Hardened delete flow: hx-confirm for HTMX, form fallback for no-JS
- Created repeatable no-JS verification script (scripts/nojs-check.md)
- Documented dual-path trade-offs, risks, mitigations (docs/prototyping-constraints.md)
- Tested with keyboard and screen reader (NVDA/Orca)
- Evidence captured in evidence/wk8/nojs-parity/

Trade-off accepted: No-JS delete has no confirmation (documented in constraints)


EOF
)"
```

### Reflection questions

**Answer in `wk08/reflection.md`**:

1. **Dual-path complexity**: Was it harder to maintain HTMX and no-JS paths than you expected? What helped keep them in sync?

2. **Error handling**: How did accessible error identification change your approach? Did you discover any WCAG requirements you hadn't considered?

3. **Trade-offs**: Which trade-off (e.g., no-JS delete confirmation) felt most significant? Would you design it differently with more time?

4. **Testing**: How useful was the scripted no-JS verification? Will you use similar scripts in future projects?

5. **Inclusion impact**: Who specifically benefits from the dual-path architecture? Think about connectivity, device age, organisational policies.

6. **Next steps**: What would you improve before Week 9 instrumentation? Any technical debt to address?

---

## Looking Ahead: Week 9 Evaluation

Next week you'll instrument your prototype to capture metrics during peer pilots:
- **Task completion time**: How long to add/edit/delete tasks
- **Error rates**: How often do validation errors occur
- **No-JS performance**: Compare HTMX vs no-JS completion times
- **Confidence ratings**: Post-task subjective feedback

**Why this week matters**: You can't evaluate a prototype that doesn't handle errors gracefully. Week 8 Lab 2 ensures your baseline is solid before adding instrumentation.

**Preparation**:
- Verify all flows work (HTMX + no-JS)
- Ensure backlog is up to date
- Review `../../references/evaluation-metrics-quickref.md` to understand what data you'll collect

---

## Further Reading & Resources

### Essential
- [WCAG 3.3: Input Assistance](https://www.w3.org/WAI/WCAG22/Understanding/input-assistance) — Error identification and prevention
- [GOV.UK: Error Message Pattern](https://design-system.service.gov.uk/components/error-message/) — Accessible error design
- [WebAIM: Form Validation](https://webaim.org/articles/formvalidation/) — Screen reader considerations

### HTMX Patterns
- `../../references/htmx-pattern-cheatsheet.md` — OOB status, confirmation, error handling
- [HTMX: Validation Example](https://htmx.org/examples/inline-validation/) — Alternative inline pattern
- [HTMX: hx-confirm](https://htmx.org/attributes/hx-confirm/) — Confirmation dialogs

### Testing
- `../../references/assistive-testing-checklist.md` — Weekly testing checklist
- [GOV.UK: Testing with Assistive Technologies](https://www.gov.uk/service-manual/technology/testing-with-assistive-technologies)
- [WebAIM: Screen Reader Testing](https://webaim.org/articles/screenreader_testing/)

### Progressive Enhancement
- [A List Apart: Understanding Progressive Enhancement](https://alistapart.com/article/understandingprogressiveenhancement/)
- [hypermedia.systems: Progressive Enhancement](https://hypermedia.systems/progressive-enhancement/)
- [GOV.UK: Building a Resilient Frontend](https://www.gov.uk/service-manual/technology/using-progressive-enhancement)

### Academic
- **Nielsen, J. (1994).** *Heuristic evaluation.* In *Usability inspection methods* (pp. 25-62). — Error prevention heuristic
- **W3C (2023).** *Web Content Accessibility Guidelines (WCAG) 2.2.* — 3.3.1, 3.3.3, 4.1.3

---

## Glossary Summary

| Term | One-line definition |
|------|---------------------|
| **Server-side validation** | Validation logic executed on the server; required for security and no-JS parity |
| **Post-Redirect-Get (PRG)** | Pattern where POST responds with redirect to prevent duplicate submissions |
| **Dual-path architecture** | Single route handles both HTMX (fragment) and no-JS (full page) requests |
| **Accessible error identification** | WCAG 3.3.1: Errors identified in text and programmatically associated with inputs |
| **No-JS parity testing** | Verifying all functionality works identically with JavaScript disabled |
| **Progressive enhancement** | Build accessible baseline first, then enhance with JavaScript as enhancement layer |
| **Live region** | ARIA region (`role="status"` or `aria-live`) that announces dynamic content changes |
| **hx-confirm** | HTMX attribute that shows browser confirmation dialog before sending request |
| **aria-invalid** | ARIA attribute indicating field contains validation error (`true`/`false`) |
| **aria-describedby** | ARIA attribute linking field to descriptive text (hint or error message) |

---

**Lab complete!** You now have a robust, accessible, dual-path prototype ready for evaluation instrumentation in Week 9.
