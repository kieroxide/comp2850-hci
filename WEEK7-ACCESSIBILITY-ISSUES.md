# Week 7 Lab 2: Intentional Accessibility Issues

This document lists the **intentional accessibility issues** added to the starter code for Week 7 Lab 2 audit exercise.

Students will discover these using axe DevTools and manual testing, then fix at least one high-priority issue.

---

## Current Issues (axe DevTools should find 4-6)

### Issue 1: Button color contrast fails WCAG 1.4.3 (Serious)
- **Element**: All `<button>` elements (Add Task, Delete buttons)
- **Rule**: `color-contrast` (WCAG 1.4.3 Contrast Minimum)
- **Description**: Button text color `#6c757d` (gray) on white background = 4.2:1 contrast ratio
- **Requirement**: WCAG AA requires minimum 4.5:1 for normal text
- **Impact**: People with low vision, color blindness, or using devices in bright sunlight struggle to read button text
- **Location**: `src/main/resources/static/css/custom.css` lines 260-266
- **How to fix**: Override with darker color in CSS or inline styles
  - Example fix: `button { color: #495057; }` (7:1 contrast, passes AAA)
  - Alternative: `button { color: white !important; }` (white text on blue Pico CSS button background)
- **Priority**: ⚠️ **HIGH** — Main issue students should fix in Week 7 Lab 2 Activity 5

```css
/* INTENTIONAL ISSUE - located in custom.css */
button[type="submit"],
button {
  color: #6c757d;
}
```

---

### Issue 2: Form input without label (Serious)
- **Element**: `<input type="text" name="priority" placeholder="Priority (optional)">`
- **Rule**: `label` (WCAG 1.3.1 Info and Relationships, 4.1.2 Name, Role, Value)
- **Description**: Text input has no associated `<label>` element
- **Impact**: Screen readers don't announce what the input is for; keyboard users have reduced click target
- **Location**: `src/main/resources/templates/tasks/index.peb` line 18
- **How to fix**: Add `<label for="priority">Priority</label>` and `id="priority"` to input
- **Priority**: MEDIUM — Students may fix this or document in backlog

```html
<!-- INTENTIONAL ISSUE - located in tasks/index.peb line 18 -->
<input type="text" name="priority" placeholder="Priority (optional)">
```

**Fix example:**
```html
<label for="priority">Priority</label>
<input type="text" id="priority" name="priority" placeholder="Optional">
```

---

### Issue 3: Image without alt text (Serious/Moderate)
- **Element**: `<img src="/static/img/icon.png" width="16" height="16">`
- **Rule**: `image-alt` (WCAG 1.1.1 Non-text Content)
- **Description**: Image has no `alt` attribute
- **Impact**: Screen readers announce "image" with no description; context lost
- **Location**: `src/main/resources/templates/tasks/index.peb` line 34
- **How to fix**: Add `alt=""` (if decorative) or `alt="Task list icon"` (if meaningful)
- **Priority**: MEDIUM

```html
<!-- INTENTIONAL ISSUE - located in tasks/index.peb line 34 -->
<img src="/static/img/icon.png" width="16" height="16">
```

**Fix example:**
```html
<!-- If decorative -->
<img src="/static/img/icon.png" width="16" height="16" alt="">

<!-- If meaningful -->
<img src="/static/img/icon.png" width="16" height="16" alt="Task list">
```

---

### Issue 4: Link with no text content (Serious/Moderate)
- **Element**: `<a href="/about"></a>`
- **Rule**: `link-name` (WCAG 2.4.4 Link Purpose, 4.1.2 Name, Role, Value)
- **Description**: Link element is completely empty (no text or aria-label)
- **Impact**: Screen readers announce "link" with no destination or purpose
- **Location**: `src/main/resources/templates/_layout/base.peb` line 49
- **How to fix**: Add link text or remove the link entirely
- **Priority**: LOW — Easy fix or remove

```html
<!-- INTENTIONAL ISSUE - located in base.peb line 49 -->
<a href="/about"></a>
```

**Fix example:**
```html
<!-- Option 1: Add text -->
<a href="/about">About</a>

<!-- Option 2: Remove entirely if not needed -->
<!-- <a href="/about"></a> -->
```

---

### Issue 5: Empty heading (Minor) — May not be detected by axe
- **Element**: `<h3></h3>` (was added but removed, may show if still cached)
- **Rule**: `empty-heading` (WCAG 1.3.1)
- **Description**: Heading element with no content
- **Impact**: Screen readers announce empty heading, causing confusion
- **Note**: This was removed in favor of more detectable issues
- **Priority**: N/A (not present in current version)

---

## False Positives Students May Encounter

### False Positive: Form label missing on title input
- **Element**: `<input id="title" name="title">` in tasks/index.peb
- **Rule**: `label`
- **Status**: ❌ **FALSE POSITIVE** — Label exists at line 13: `<label for="title">Title</label>`
- **Action**: Students should verify manually and document as false positive

---

## Expected Student Actions (Week 7 Lab 2)

1. **Run axe DevTools** on http://localhost:8080/tasks
2. **Find 4-6 issues** (2-3 Serious, 1-2 Moderate/Minor, 1 Best Practice)
3. **Document all findings** in `wk07/audit/axe-report.md`
4. **Verify false positives** manually (title input has label)
5. **Prioritize Issue 1** (button contrast) as highest severity
6. **Implement fix for Issue 1**:
   - Add CSS override in base.peb `<style>` block OR custom.css
   - Test with WebAIM contrast checker (https://webaim.org/resources/contrastchecker/)
   - Verify with axe DevTools (issue should disappear)
7. **Document remaining issues** in inclusive backlog (Issues 2-4)
8. **Add to backlog** with severity + inclusion risk scores

---

## Files Containing Intentional Issues

1. **`src/main/resources/static/css/custom.css`** (lines 260-266)
   - Button color contrast issue

2. **`src/main/resources/templates/tasks/index.peb`** (lines 18, 34)
   - Unlabeled priority input
   - Image without alt text

3. **`src/main/resources/templates/_layout/base.peb`** (line 49)
   - Empty link

---

## How Students Fix Issue 1 (Button Contrast) — Main Teaching Point

Students will add CSS to override the gray button color. Two approaches shown in mdbook:

**Approach 1: Inline `<style>` in base.peb** (Week 7 Lab 2 suggests this)
```html
<head>
  ...
  <style>
    /* Override Pico.css button color for WCAG 1.4.3 AA compliance */
    button[type="submit"],
    button {
      color: #495057 !important; /* 7:1 contrast (AAA) */
    }
  </style>
</head>
```

**Approach 2: Update custom.css** (Alternative)
Replace the intentional issue with the fix:
```css
/* Fix for button contrast (replaces #6c757d) */
button[type="submit"],
button {
  color: #495057; /* 7:1 contrast (passes AAA) */
}
```

---

## Testing the Fix

1. **Visual inspection**: Buttons should be darker gray (more readable)
2. **WebAIM Contrast Checker**:
   - Foreground: #495057
   - Background: #ffffff
   - Result: 7.0:1 (Pass AAA) ✅
3. **axe DevTools**: Re-run scan, button contrast issue should be gone
4. **Manual testing**: Buttons still functional, no regressions

---

## Implementation Notes

- Issues added in commits: `be5c216`, `c05fed2`, `2b218fd`
- **DO NOT** remove these issues before students complete Week 7 Lab 2
- These are pedagogical scaffolding for accessibility audit exercise
- Button contrast is the primary learning issue; others provide additional practice

---

**Version**: Week 7 Lab 2 starter (with intentional accessibility issues for discovery)
**Last updated**: 2025-11-12
