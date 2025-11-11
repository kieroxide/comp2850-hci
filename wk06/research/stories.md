# Job Stories — Week 6 Needs-Finding

## Story S1: Filter Persistence
**Situation**: When I'm filtering tasks by project tag (e.g., "COMP2850")
**Motivation**: I want the filter selection to persist across page reloads
**Outcome**: So I can pick up where I left off without re-selecting the filter
**Underlying need**: Because re-filtering 10+ times per session creates cognitive overload and wastes time

**Evidence**: Participant A (notes L5), Participant B (notes L3)
**Inclusion risk**: Cognitive, memory impairment, ADHD
**Type**: Job story (situation-specific)

---

## Story S2: Confirmation Feedback
**Situation**: When I submit a form (add task, edit task, delete task)
**Motivation**: I want immediate, explicit confirmation that the action succeeded
**Outcome**: So I can trust the interface without refreshing to verify
**Underlying need**: Because uncertainty about save status causes anxiety and inefficient workarounds (page reload)

**Evidence**: Participant A (notes L12), Participant B (notes L8)
**Inclusion risk**: Cognitive, screen reader (if confirmation not announced), low digital literacy
**Type**: Job story

---

## Story S3: Full Keyboard Access
**Situation**: When my mouse/trackpad is unavailable (broken hardware, RSI flare-up, preference)
**Motivation**: I want to access all features using only Tab, Enter, Space, and arrow keys
**Outcome**: So I can complete tasks without being excluded
**Underlying need**: Because reliance on pointing device excludes people with motor impairments or temporary injuries

**Evidence**: Participant A (notes L20)
**Inclusion risk**: Motor impairment, RSI, temporary disability, keyboard-only preference
**Type**: Job story
**WCAG**: 2.1.1 Keyboard (A), 2.1.3 Keyboard (No Exception, AAA)

---

## Story S4: High Contrast
**Situation**: When I'm working in bright sunlight or have low vision
**Motivation**: I want text to have sufficient contrast against background
**Outcome**: So I can read task titles and buttons without straining
**Underlying need**: Because low contrast creates situational disability (sunlight) or permanent exclusion (low vision)

**Evidence**: Participant A (notes L28)
**Inclusion risk**: Low vision, colour-blindness, situational (bright light)
**Type**: Job story
**WCAG**: 1.4.3 Contrast (Minimum, AA) — 4.5:1 for normal text

---

## Story S5: Progress Visualisation
**Situation**: When I'm managing a long task list (15+ items)
**Motivation**: I want to see completion progress (e.g., "8/12 done this week")
**Outcome**: So I can feel motivated and track productivity
**Underlying need**: Because invisible progress reduces motivation and makes it hard to assess workload

**Evidence**: Participant A (notes L35), Participant B (notes L15)
**Inclusion risk**: Cognitive, ADHD (executive function support)
**Type**: Job story

---

## Story S6: Persistent Error Messages (No-JS)
**Situation**: When JavaScript is disabled (corporate firewall, data-saving mode) and I submit invalid data
**Motivation**: I want error messages to persist after page reload
**Outcome**: So I can understand what went wrong and correct it
**Underlying need**: Because ephemeral error messages (lost on redirect) require perfect memory or multiple submission attempts

**Evidence**: Inferred from Lab 1 no-JS testing; no explicit interview mention (add if time)
**Inclusion risk**: Cognitive, screen reader (needs page-level error summary)
**Type**: Pain point (internally identified)
**WCAG**: 3.3.1 Error Identification (A), 3.3.3 Error Suggestion (AA)
