# Week 7 • Lab 2: Accessibility Audit, Backlog & Inclusive Fix

![COMP2850](https://img.shields.io/badge/COMP2850-HCI-blue)
![Week 7](https://img.shields.io/badge/Week-7-orange)
![Lab 2](https://img.shields.io/badge/Lab-2-green)
![Status](https://img.shields.io/badge/Status-Draft-yellow)

---

## Terminology Note

Throughout COMP2850 we use **people-centred language** (e.g., "person using a screen reader") rather than deficit-based terms (e.g., "blind user"). This reflects contemporary inclusive-design practice and acknowledges that disability arises from environmental barriers, not individual impairment.

---

## Pre-reading

**Essential**
- [W3C (2024). WCAG 2.2 Quick Reference](https://www.w3.org/WAI/WCAG22/quickref/)
- [axe DevTools Documentation](https://www.deque.com/axe/devtools/)
- [GOV.UK: Making Your Service Accessible](https://www.gov.uk/service-manual/helping-people-to-use-your-service/making-your-service-accessible-an-introduction)

**Recommended**
- [WebAIM: WCAG 2 Checklist](https://webaim.org/standards/wcag/checklist)
- [Nielsen, J. (1994). "Heuristic Evaluation"](https://www.nngroup.com/articles/how-to-conduct-a-heuristic-evaluation/)
- [Shneiderman et al. (2016). *Designing the User Interface*, Ch. 3: Guidelines](https://www.cs.umd.edu/~ben/)

---

## Introduction

### Context

In **Week 7 Lab 1** you implemented accessible inline edit with dual-path support (HTMX + no-JS), ARIA error identification, and status message announcements. You tested with keyboard and screen reader (NVDA/VoiceOver) and collected evidence.

**Now you'll audit the entire application systematically** using:
1. **WCAG 2.2 AA checklist** (technical compliance)
2. **Heuristics** (Nielsen's 10 Usability Heuristics + Shneiderman's Golden Rules)
3. **Assistive technology testing** (keyboard, screen reader, no-JS, zoom)

You'll log findings in your inclusive backlog (from Week 6), prioritize using *severity × inclusion risk*, and **implement one high-priority fix end-to-end** (code → verification → evidence).

This lab directly feeds **Gradescope Task 1** (evaluation & findings) and sets up **Task 2** (redesign & verification).

### Why This Matters

**Professionally**, accessibility audits are standard practice:
- **GOV.UK** mandates WCAG 2.2 AA compliance before launch
- **BBC** runs quarterly audits with assistive tech users
- **Microsoft** uses automated (axe) + manual (SR) testing in CI/CD pipeline

**Academically**, this lab teaches:
- **Systematic evaluation**: Structured checklists reduce bias (vs. ad-hoc testing)
- **Triangulation**: Automated tools + manual testing + user feedback
- **Evidence-based prioritization**: Data (backlog scores) beats intuition

### Learning Outcomes

By the end of this lab, you will be able to:

| Learning Outcome | Module LO | ACM/WCAG Ref |
|---|---|---|
| Conduct structured accessibility audit using WCAG 2.2 AA criteria | LO10: Evaluate inclusive design | WCAG 2.2 (process) |
| Use automated tools (axe DevTools) and manual testing (SR, keyboard) | LO10: Apply evaluation methods | ACM: HC/4 |
| Populate inclusive backlog with severity + inclusion risk scores | LO3: Apply HCI frameworks | ACM: HC/3 |
| Implement and verify one priority accessibility fix | LO9: Apply accessibility standards | WCAG 2.2 AA |

---

## Key Concepts

### 1. Accessibility Audit Levels

**Level 1: Automated testing** (20-30% of WCAG issues)
- Tools: axe DevTools, WAVE, Lighthouse
- Fast, repeatable, catches low-hanging fruit
- **Limitations**: Can't detect non-semantic HTML, focus order issues, SR announcements

**Level 2: Manual testing** (keyboard, screen reader)
- Catches 50-60% of issues
- Time-consuming, requires expertise
- **Essential for**: Focus management, ARIA announcements, keyboard traps

**Level 3: User testing** (people with disabilities)
- Catches 90%+ of issues
- Gold standard, but expensive and slow
- **Week 9**: You'll run task-based pilots (mini user tests)

**COMP2850 approach**: Levels 1 + 2 in Labs 7-8; Level 3 in Week 9.

### 2. WCAG 2.2 Conformance Levels

**Level A** (minimum, legal requirement in many jurisdictions):
- Examples: 1.3.1 Info and Relationships, 2.1.1 Keyboard, 3.3.1 Error Identification

**Level AA** (target for most organizations):
- Examples: 1.4.3 Contrast (Minimum), 2.4.7 Focus Visible, 4.1.3 Status Messages

**Level AAA** (enhanced, not always achievable):
- Examples: 1.4.6 Contrast (Enhanced, 7:1), 2.1.3 Keyboard (No Exception)

**COMP2850 target**: AA compliance (industry standard).

### 3. Severity Scoring (COMP2850 Framework)

**High (3 points)**:
- Blocks task completion entirely (e.g., form unsubmittable via keyboard)
- Excludes entire group (e.g., no screen reader access)
- WCAG Level A failure

**Medium (2 points)**:
- Makes task significantly harder (e.g., poor contrast, slow to navigate)
- Affects multiple groups (e.g., keyboard + screen reader)
- WCAG Level AA failure

**Low (1 point)**:
- Minor friction (e.g., missing hint text, suboptimal label)
- Affects niche scenario (e.g., rare browser/AT combination)
- WCAG AAA or best practice (not required)

### 4. Inclusion Risk Tagging

**Tags** identify who's affected:
- **SR** (Screen reader): NVDA, JAWS, VoiceOver, TalkBack
- **Keyboard**: Keyboard-only users, motor impairments, RSI
- **Cognitive**: ADHD, dyslexia, memory impairments
- **Low vision**: Magnification users, high contrast needs
- **Motor**: Tremor, limited dexterity, switch users
- **Deaf**: Caption/transcript needs (not applicable to this project yet)
- **Situational**: Bright light, broken mouse, noisy environment

**Example**: "Delete button lacks keyboard access" = **Keyboard + Motor** tags.

### 5. Nielsen's 10 Usability Heuristics (Applied to Accessibility)

1. **Visibility of system status**: Screen readers announce state changes
2. **Match between system and real world**: Plain language (not jargon)
3. **User control and freedom**: Undo, cancel actions
4. **Consistency and standards**: Follow ARIA patterns, GOV.UK conventions
5. **Error prevention**: Validation before submission
6. **Recognition rather than recall**: Labels visible (not just placeholders)
7. **Flexibility and efficiency of use**: Keyboard shortcuts for power users
8. **Aesthetic and minimalist design**: No clutter (easier for SR/cognitive users)
9. **Help users recognize, diagnose, and recover from errors**: Specific error messages
10. **Help and documentation**: Context-sensitive help text

---

## Activity 1: Automated Audit with axe DevTools

**Time**: 20 minutes
**Materials**: Chrome/Firefox with axe DevTools extension

### Step 1: Install axe DevTools

**Chrome**:
1. Visit [Chrome Web Store](https://chrome.google.com/webstore)
2. Search "axe DevTools"
3. Install extension

**Firefox**:
1. Visit [Firefox Add-ons](https://addons.mozilla.org/)
2. Search "axe DevTools"
3. Install add-on

### Step 2: Run Automated Scan

1. **Open http://localhost:8080/tasks** (ensure server running)
2. **Open DevTools** (F12)
3. **Navigate to "axe DevTools" tab**
4. **Click "Scan ALL of my page"**
5. **Wait 5-10 seconds** for results

### Step 3: Review Findings

axe categorizes issues:

| Category | Severity | Action |
|----------|----------|--------|
| **Critical** | Blocks access (Level A failures) | Must fix |
| **Serious** | Major barriers (Level A/AA failures) | Should fix |
| **Moderate** | Noticeable issues (Level AA/AAA) | Consider fixing |
| **Minor** | Best practice violations | Low priority |

### Step 4: Document Automated Findings

Create `wk07/audit/axe-report.md`:

```markdown
# axe DevTools Audit Report — Week 7

**Date**: [YYYY-MM-DD]
**URL**: http://localhost:8080/tasks
**Tool**: axe DevTools 4.x
**Scope**: Full page scan (add form + task list)

---

## Summary
- **Critical**: 0
- **Serious**: 2
- **Moderate**: 1
- **Minor**: 3
- **Total**: 6 issues

---

## Critical Issues
None detected.

---

## Serious Issues

### Issue 1: Form label missing (Serious)
**Element**: `<input id="title" name="title">`
**Rule**: `label` (WCAG 1.3.1, 4.1.2)
**Description**: Form element does not have an associated label.
**Impact**: Screen reader users don't know what the input is for.
**Fix**: Ensure `<label for="title">Title</label>` exists and is visible (not visually-hidden).
**Status**: ❌ **FALSE POSITIVE** — Label exists in template. Possible axe bug or dynamic rendering issue. Verify manually.

### Issue 2: Insufficient color contrast (Serious)
**Element**: `<button type="submit">Delete</button>`
**Rule**: `color-contrast` (WCAG 1.4.3)
**Description**: Text color #6c757d on white background = 4.2:1 (fails AA 4.5:1)
**Impact**: Low vision users struggle to read button text.
**Fix**: Change button color to #495057 (darker gray, 7:1 contrast).
**Status**: ✅ **CONFIRMED** — Add to backlog as High severity.

---

## Moderate Issues

### Issue 3: Skip link not keyboard-accessible (Moderate)
**Element**: `<a href="#main" class="skip-link">`
**Rule**: `skip-link` (best practice)
**Description**: Skip link exists but positioned off-screen; unclear if focus visible.
**Impact**: Keyboard users may not discover skip link.
**Fix**: Ensure `:focus` pseudo-class makes skip link visible (already implemented in CSS, but verify).
**Status**: ✅ **VERIFIED** — Tab reveals skip link. No action needed.

---

## Minor Issues

### Issue 4-6: [Document remaining minor issues]
[Low priority, defer to Semester 2]

---

## Actions
1. **False positive (Issue 1)**: Verify label exists with manual inspection
2. **High priority (Issue 2)**: Fix contrast ratio → Add to backlog
3. **Verified (Issue 3)**: No action needed

---

**Next step**: Manual testing to catch issues axe misses (focus order, SR announcements, keyboard traps).
```

**Stop and check**:
- ✅ axe scan completed
- ✅ Findings documented in `axe-report.md`
- ✅ At least 1 serious issue identified (contrast, label, etc.)

---

## Activity 2: Manual WCAG Checklist

**Time**: 30 minutes
**Materials**: WCAG 2.2 Quick Reference, keyboard, screen reader

### Step 1: Create WCAG Checklist Template

Create `wk07/audit/wcag-checklist.md`:

```markdown
# WCAG 2.2 AA Checklist — Week 7

**Date**: [YYYY-MM-DD]
**Scope**: Task manager (add, edit, delete flows)
**Tester**: [Your Name]

---

## Perceivable (Principle 1)

### 1.1 Text Alternatives
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 1.1.1 Non-text Content | A | N/A | No images yet | Will add in Week 8 |

### 1.3 Adaptable
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 1.3.1 Info and Relationships | A | ✅ Pass | `<label for="title">` links to input | Semantic HTML (`<main>`, `<section>`, `<ul>`) |
| 1.3.2 Meaningful Sequence | A | ✅ Pass | Tab order: skip link → add form → task list | Logical reading order |

### 1.4 Distinguishable
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 1.4.3 Contrast (Minimum) | AA | ❌ Fail | Delete button #6c757d = 4.2:1 | Needs 4.5:1 (AA) |
| 1.4.11 Non-text Contrast | AA | ✅ Pass | Focus outline 3px solid #1976d2 | Sufficient contrast |

---

## Operable (Principle 2)

### 2.1 Keyboard Accessible
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 2.1.1 Keyboard | A | ✅ Pass | All features accessible via Tab/Enter/Space | Tested: add, edit, delete, cancel |
| 2.1.2 No Keyboard Trap | A | ✅ Pass | No traps detected | Can Tab out of all forms |

### 2.4 Navigable
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 2.4.1 Bypass Blocks | A | ✅ Pass | Skip link appears on Tab, jumps to #main | Tested with keyboard |
| 2.4.3 Focus Order | A | ✅ Pass | Tab order: Edit → Title → Save → Cancel | Logical sequence |
| 2.4.7 Focus Visible | AA | ⚠️ Partial | Pico.css default outline visible, but faint | Consider custom outline (3px solid) |

---

## Understandable (Principle 3)

### 3.2 Predictable
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 3.2.1 On Focus | A | ✅ Pass | No context change on focus | Only explicit button clicks trigger actions |
| 3.2.2 On Input | A | ✅ Pass | No auto-submit on input change | Form submits only on button click |

### 3.3 Input Assistance
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 3.3.1 Error Identification | A | ✅ Pass | Error: "Title is required. Please enter at least one character." | Specific, actionable |
| 3.3.2 Labels or Instructions | A | ✅ Pass | All inputs have `<label>` + hint text | `aria-describedby` links to hint |
| 3.3.3 Error Suggestion | AA | ✅ Pass | Error message includes correction hint | "Please enter at least one character" |

---

## Robust (Principle 4)

### 4.1 Compatible
| Criterion | Level | Status | Evidence | Notes |
|-----------|-------|--------|----------|-------|
| 4.1.2 Name, Role, Value | A | ✅ Pass | All buttons have accessible names | `aria-label="Edit task: Buy milk"` |
| 4.1.3 Status Messages | AA | ✅ Pass | `<div role="status" aria-live="polite">` | Tested with NVDA |

---

## Summary
- **Total criteria evaluated**: 18
- **Pass**: 15
- **Fail**: 1 (1.4.3 Contrast)
- **Partial**: 1 (2.4.7 Focus Visible)
- **N/A**: 1

---

## High-Priority Failures
1. **1.4.3 Contrast (Minimum, AA)**: Delete button text fails 4.5:1 ratio
   - **Action**: Change Pico.css button color or add custom CSS

2. **2.4.7 Focus Visible (AA, Partial)**: Default outline may be too faint
   - **Action**: Add custom `:focus` styles (3px solid #1976d2)

---

**Next**: Add these findings to backlog with severity scores.
```

### Step 2: Test Each Criterion

**Keyboard testing** (2.1.1, 2.1.2, 2.4.3):
1. Tab through entire page
2. Activate all buttons with Enter/Space
3. Check for keyboard traps (can you Tab out of forms?)

**Screen reader testing** (4.1.2, 4.1.3):
1. Start NVDA/VoiceOver
2. Navigate to each form field (listen for labels + hints)
3. Trigger error (listen for `role="alert"` announcement)
4. Save successfully (listen for status message)

**Contrast testing** (1.4.3):
1. Open DevTools → Inspect element
2. Check computed color values
3. Use [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
   - Foreground: #6c757d
   - Background: #ffffff
   - **Result**: 4.2:1 (fails AA)

**Focus visible testing** (2.4.7):
1. Tab to each interactive element
2. Can you clearly see which element has focus?
3. Take screenshot of focus indicator

**Stop and check**:
- ✅ WCAG checklist completed (at least 15 criteria)
- ✅ Pass/Fail status recorded
- ✅ Evidence cited (screenshot, manual test, tool output)

---

## Activity 3: Heuristic Evaluation

**Time**: 20 minutes
**Materials**: Nielsen's heuristics, Shneiderman's Golden Rules

### Step 1: Apply Nielsen's Heuristics

Create `wk07/audit/heuristics.md`:

```markdown
# Heuristic Evaluation — Week 7

**Evaluator**: [Your Name]
**Date**: [YYYY-MM-DD]
**Method**: Nielsen's 10 Usability Heuristics + Shneiderman's Golden Rules

---

## Nielsen's Heuristics

### 1. Visibility of System Status
**Rating**: 4/5 (Good)
**Evidence**:
- ✅ Status messages announce add/delete/edit actions
- ✅ ARIA live region updates (`role="status"`)
- ⚠️ No loading indicator for HTMX requests (instant for now, but could be slow on poor network)

**Accessibility implication**: Screen reader users get confirmation via live region (WCAG 4.1.3).

**Issue identified**: None (meets WCAG). Enhancement: Add `hx-indicator` for slow requests.

---

### 2. Match Between System and Real World
**Rating**: 5/5 (Excellent)
**Evidence**:
- ✅ Plain language: "Add Task", "Edit", "Delete" (not technical jargon)
- ✅ Confirmation messages in natural language: "Task 'Buy milk' added successfully"

**Accessibility implication**: Simple language benefits cognitive disabilities, low digital literacy.

**Issue identified**: None.

---

### 3. User Control and Freedom
**Rating**: 4/5 (Good)
**Evidence**:
- ✅ Cancel button in edit mode (escape hatch)
- ❌ No undo for delete (permanent action)

**Accessibility implication**: People with motor impairments may accidentally trigger delete.

**Issue identified**: **Medium severity** — Add confirmation dialog or undo feature for delete.

---

### 4. Consistency and Standards
**Rating**: 5/5 (Excellent)
**Evidence**:
- ✅ Semantic HTML (`<button>`, not `<div onclick>`)
- ✅ Follows ARIA patterns (errors use `role="alert"`)
- ✅ Consistent with GOV.UK patterns (error summary, hints)

**Accessibility implication**: Consistency reduces learning curve for AT users.

**Issue identified**: None.

---

### 5. Error Prevention
**Rating**: 3/5 (Fair)
**Evidence**:
- ✅ Client-side `required` attribute prevents blank submission
- ⚠️ Server-side validation catches blank titles (good), but no prevention of accidental delete

**Accessibility implication**: Cognitive users benefit from preventing errors before they happen.

**Issue identified**: **Medium severity** — Delete button too easy to trigger accidentally (no confirmation).

---

### 6. Recognition Rather Than Recall
**Rating**: 4/5 (Good)
**Evidence**:
- ✅ Labels always visible (not just placeholders)
- ✅ Hint text persists below input (`aria-describedby`)
- ⚠️ Error message only appears after submission (could preview validation on blur)

**Accessibility implication**: Persistent labels help cognitive disabilities, memory impairments.

**Issue identified**: None (meets WCAG). Enhancement: Live validation on blur.

---

### 7. Flexibility and Efficiency of Use
**Rating**: 3/5 (Fair)
**Evidence**:
- ✅ Keyboard shortcuts work (Enter submits, Escape cancels in some browsers)
- ❌ No custom shortcuts (e.g., Ctrl+E to edit first task)

**Accessibility implication**: Power keyboard users could benefit from shortcuts.

**Issue identified**: **Low severity** — Add keyboard shortcuts (defer to Semester 2).

---

### 8. Aesthetic and Minimalist Design
**Rating**: 5/5 (Excellent)
**Evidence**:
- ✅ No clutter (only essential fields shown)
- ✅ Edit form appears inline (progressive disclosure)
- ✅ Status messages dismiss automatically (don't accumulate)

**Accessibility implication**: Minimalism reduces cognitive load, SR navigation time.

**Issue identified**: None.

---

### 9. Help Users Recognize, Diagnose, and Recover from Errors
**Rating**: 5/5 (Excellent)
**Evidence**:
- ✅ Error message specific: "Title is required. Please enter at least one character."
- ✅ Error programmatically associated (`aria-describedby`, `role="alert"`)
- ✅ Recovery path clear (fix input, resubmit)

**Accessibility implication**: Meets WCAG 3.3.1 (Error Identification, A) and 3.3.3 (Error Suggestion, AA).

**Issue identified**: None.

---

### 10. Help and Documentation
**Rating**: 2/5 (Poor)
**Evidence**:
- ❌ No help text beyond inline hints
- ❌ No "What is this?" links for fields

**Accessibility implication**: Cognitive users, first-time users may struggle without documentation.

**Issue identified**: **Low severity** — Add help tooltips or links to docs (defer to Semester 2).

---

## Summary of Issues

| Heuristic | Issue | Severity | Inclusion Risk |
|-----------|-------|----------|----------------|
| 3 (Control & Freedom) | No undo for delete | Medium | Motor, Cognitive |
| 5 (Error Prevention) | Delete lacks confirmation | Medium | Motor, Cognitive |
| 7 (Flexibility) | No keyboard shortcuts | Low | Keyboard (power users) |
| 10 (Help) | No help documentation | Low | Cognitive, Low digital literacy |

---

**Next**: Add these to backlog.
```

**Stop and check**:
- ✅ Heuristic evaluation completed (10 heuristics)
- ✅ Ratings assigned (1-5 scale)
- ✅ Issues identified with severity

---

## Activity 4: Update Inclusive Backlog

**Time**: 25 minutes
**Materials**: `wk06/backlog/backlog.csv`, audit findings

### Step 1: Consolidate Findings

Merge findings from:
- axe DevTools (`axe-report.md`)
- WCAG checklist (`wcag-checklist.md`)
- Heuristics (`heuristics.md`)
- Week 6 needs-finding interviews

### Step 2: Add New Backlog Items

Edit `wk06/backlog/backlog.csv`:

```csv
id,title,story_ref,story_type,need,type,severity,inclusion_risk,evidence,notes,candidate_fix
9,Delete button text contrast 4.2:1 (fails AA),wk07/audit/axe-report.md#Issue2,WCAG violation,Visibility,Accessibility,High,"Low vision,Situational","axe: color-contrast; WCAG 1.4.3","Pico.css default gray #6c757d on white = 4.2:1; needs 4.5:1",true
10,Focus outline too faint (Pico.css default),wk07/audit/wcag-checklist.md#2.4.7,WCAG partial,Visibility,Accessibility,Medium,Keyboard,"Manual test: outline visible but faint","Add custom :focus { outline: 3px solid #1976d2; }",true
11,No confirmation for delete action,wk07/audit/heuristics.md#H3,Heuristic violation,Error prevention,Usability,Medium,"Motor,Cognitive","Nielsen H3 + H5","Accidental clicks cause permanent deletion; add confirmation dialog",false
12,No undo feature for delete,wk07/audit/heuristics.md#H3,Heuristic violation,User control,Usability,Low,"Motor,Cognitive","Nielsen H3","Defer to Semester 2; requires state management",false
13,No help documentation or tooltips,wk07/audit/heuristics.md#H10,Heuristic violation,Learning support,Usability,Low,Cognitive,"Nielsen H10","Defer to Semester 2; low priority",false
```

### Step 3: Prioritize with Scoring

**Formula**: `Priority = (Severity × 2) + (Inclusion Weight) + (WCAG Bonus)`

- **Severity**: High=3, Medium=2, Low=1 (×2 weighting)
- **Inclusion Weight**: +1 per group affected (max +3)
- **WCAG Bonus**: Level A failure=+2, Level AA failure=+1

**Example (Item 9: Contrast)**:
- Severity: High (3) × 2 = 6
- Inclusion: Low vision + Situational = +2
- WCAG: Level AA (1.4.3) = +1
- **Total**: 6 + 2 + 1 = **9 (highest priority)**

**Example (Item 11: Delete confirmation)**:
- Severity: Medium (2) × 2 = 4
- Inclusion: Motor + Cognitive = +2
- WCAG: N/A = 0
- **Total**: 4 + 2 + 0 = **6 (medium priority)**

### Step 4: Select Candidate Fix

Mark **1-2 items** with `candidate_fix=true`. Criteria:
- Highest priority score
- Fixable in 30-40 minutes (lab time constraint)
- Clear success criteria (WCAG pass = verifiable)

**Recommended fixes**:
1. **Item 9** (Contrast): Change button color in CSS (10 min fix)
2. **Item 10** (Focus outline): Add custom `:focus` styles (15 min fix)

**Stop and check**:
- ✅ Backlog updated with 5+ new items from audits
- ✅ Priority scores calculated
- ✅ 1-2 items marked `candidate_fix=true`

---

## Activity 5: Implement One Priority Fix

**Time**: 35 minutes
**Materials**: Backlog, code editor

### Step 1: Choose Fix Target

**Selected**: Item 9 (Delete button contrast)

**Why**: Highest priority (WCAG AA failure), quick fix, measurable success.

### Step 2: Create Fix Plan

Create `wk07/fixes/fix09-contrast.md`:

```markdown
# Fix 09: Delete Button Contrast (WCAG 1.4.3)

**Backlog ID**: 9
**WCAG Criterion**: 1.4.3 Contrast (Minimum, Level AA)
**Priority**: 9 (highest)

---

## Problem Statement
Delete button text color (#6c757d) on white background (#ffffff) = 4.2:1 contrast ratio.
**Fails**: WCAG AA requires 4.5:1 for normal text.

**Evidence**:
- axe DevTools: color-contrast (Serious)
- WebAIM Contrast Checker: 4.2:1 (Fail AA)
- Screenshot: `wk07/evidence/contrast-before.png`

---

## Target State
Contrast ratio ≥ 4.5:1 (AA) or ≥ 7:1 (AAA).

---

## Solution
Override Pico.css button color with darker gray or custom color.

**Option A**: Darker gray (#495057 = 7:1 contrast, passes AAA)
**Option B**: Custom blue (#1976d2 = 5.2:1 contrast, passes AA)

**Chosen**: Option A (darker gray) for consistency with Pico theme.

---

## Implementation

### Before (Current CSS)
Pico.css default:
```css
button {
  color: #6c757d; /* 4.2:1 contrast */
}
```

### After (Custom CSS)
Add to `base.peb` `<style>`:
```css
button[type="submit"],
button {
  color: #495057 !important; /* 7:1 contrast (AAA) */
}
```

---

## Verification

### Step 1: Visual Inspection
- Load http://localhost:8080/tasks
- Inspect "Delete" button
- Confirm text appears darker

### Step 2: Contrast Check
- Use WebAIM Contrast Checker
- Foreground: #495057
- Background: #ffffff
- **Expected**: 7:1 (Pass AAA)

### Step 3: Re-run axe
- Open axe DevTools
- Scan page
- **Expected**: color-contrast issue resolved

### Step 4: Screenshot
- Save `wk07/evidence/contrast-after.png`
- Show button with new color

---

## Evidence
- Before: `wk07/evidence/contrast-before.png`
- After: `wk07/evidence/contrast-after.png`
- axe report: `wk07/evidence/axe-after.png` (0 serious issues)

---

## Commit
`git commit -m "Fix #9: Increase button contrast to 7:1 (WCAG 1.4.3 AAA)"`
```

### Step 3: Implement Fix

**Edit `src/main/re../../references/templates/base.peb`**:

Add inside `<style>` block:

```css
/* Override Pico.css button color for WCAG 1.4.3 AA compliance */
button[type="submit"],
button {
  color: #495057 !important; /* 7:1 contrast (passes AAA) */
}
```

**Reload page**, inspect button:
- **Before**: #6c757d (light gray)
- **After**: #495057 (dark gray)

### Step 4: Verify Fix

**1. Visual check**:
```bash
# Reload http://localhost:8080/tasks
# Confirm button text darker
```

**2. Contrast calculator**:
- Visit [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- Foreground: #495057
- Background: #ffffff
- **Result**: 7.0:1 (Pass AAA) ✅

**3. Re-run axe**:
- Open axe DevTools
- Scan page
- **Expected**: 0 serious issues (contrast resolved)

**4. Screenshot**:
```bash
# Save screenshot to wk07/evidence/contrast-after.png
```

### Step 5: Document Verification

Create `wk07/fixes/verification.md`:

```markdown
# Verification Log — Fix 09

**Date**: [YYYY-MM-DD]
**Fix**: Delete button contrast (WCAG 1.4.3)

---

## Before State
- **Color**: #6c757d (Pico.css default)
- **Contrast**: 4.2:1 (Fail AA)
- **axe**: 1 serious issue (color-contrast)

## After State
- **Color**: #495057 (custom override)
- **Contrast**: 7.0:1 (Pass AAA)
- **axe**: 0 serious issues

---

## Tests Performed

### Test 1: Contrast Calculation
**Tool**: WebAIM Contrast Checker
**Foreground**: #495057
**Background**: #ffffff
**Result**: ✅ 7.0:1 (Pass AAA)

### Test 2: Visual Inspection
**Action**: Loaded http://localhost:8080/tasks, inspected "Delete" button
**Result**: ✅ Text noticeably darker, easier to read

### Test 3: Automated Re-scan
**Tool**: axe DevTools 4.x
**Result**: ✅ 0 critical, 0 serious (contrast issue resolved)

### Test 4: Regression Check
**Action**: Tested add, edit, delete flows
**Result**: ✅ No regressions, all features work

---

## Evidence
- Before screenshot: `wk07/evidence/contrast-before.png`
- After screenshot: `wk07/evidence/contrast-after.png`
- Contrast checker result: `wk07/evidence/webaim-contrast-7-1.png`
- axe report (after): `wk07/evidence/axe-after.png`

---

## WCAG Compliance
**Criterion**: 1.4.3 Contrast (Minimum, Level AA)
**Status**: ✅ Pass (7.0:1 exceeds 4.5:1 requirement)

**Bonus**: Also passes AAA (7:1 threshold)

---

## Commit
SHA: [abc123f]
Message: "Fix #9: Increase button contrast to 7:1 (WCAG 1.4.3 AAA)"
Files changed: `base.peb` (+3 lines)
```

**Stop and check**:
- ✅ Fix implemented (CSS change)
- ✅ Contrast verified (7:1, Pass AAA)
- ✅ axe re-scan shows 0 serious issues
- ✅ Evidence collected (screenshots, verification log)

---

## Reflection Questions

1. **Automated vs. manual testing**: axe found the contrast issue, but did it catch the focus outline being faint? Why do we need both automated and manual testing?

2. **Prioritization**: You had 5 new backlog items. How did the scoring formula (severity × inclusion × WCAG) change which one you fixed first? Would your decision differ if time wasn't limited?

3. **Evidence chains**: Trace the evidence chain for your fix: audit finding → backlog item → fix implementation → verification. What would happen if you skipped any step?

4. **Regression risk**: After fixing contrast, did you verify all buttons still work? Why is regression testing important?

---

## Further Reading

**WCAG 2.2 & auditing**
- W3C (2024). *WCAG 2.2 Quick Reference*. <https://www.w3.org/WAI/WCAG22/quickref/>
- Deque (2024). *axe DevTools Documentation*. <https://www.deque.com/axe/devtools/>
- WebAIM (2024). *WCAG 2 Checklist*. <https://webaim.org/standards/wcag/checklist>

**Heuristic evaluation**
- Nielsen, J. (1994). "Heuristic Evaluation." *Usability Inspection Methods*, 25-62.
- Shneiderman, B. et al. (2016). *Designing the User Interface* (6th ed.), Ch. 3.

**Inclusive backlogs**
- Cohn, M. (2004). *User Stories Applied*. Addison-Wesley.
- Microsoft (2024). *Inclusive Design Toolkit*. <https://inclusive.microsoft.design/>

---

## Glossary Summary

| Term | Definition | Example/Context |
|------|------------|-----------------|
| **Accessibility audit** | Systematic evaluation against WCAG criteria | axe scan + manual keyboard/SR testing |
| **Automated testing** | Tools that scan code for issues (20-30% coverage) | axe DevTools, WAVE, Lighthouse |
| **Manual testing** | Human evaluation (keyboard, SR, heuristics) | Tab through page, listen with NVDA |
| **Severity** | Impact on task completion (High/Medium/Low) | High = blocks; Medium = hinders; Low = cosmetic |
| **Inclusion risk** | Who's affected (SR, Keyboard, Cognitive, etc.) | "Affects SR + Keyboard users" |
| **Heuristic evaluation** | Expert review using usability principles | Nielsen's 10 Heuristics, Shneiderman's Golden Rules |
| **Contrast ratio** | Luminance difference (text vs. background) | 4.5:1 (AA), 7:1 (AAA) |
| **Regression testing** | Verifying fixes don't break existing features | After contrast fix, confirm buttons still work |
| **Evidence chain** | Traceable path: audit → backlog → fix → verification | Finding → Item #9 → CSS change → 7:1 verified |

---

## Lab Checklist

Before leaving lab, confirm:

- [ ] **Automated audit completed**: axe DevTools scan, report saved
- [ ] **WCAG checklist filled**: At least 15 criteria evaluated (Pass/Fail/N/A)
- [ ] **Heuristic evaluation done**: Nielsen's 10 applied, issues logged
- [ ] **Backlog updated**: 5+ new items added with severity + inclusion risk
- [ ] **One fix implemented**: Code changed, committed
- [ ] **Fix verified**: Contrast/tool re-scan shows success
- [ ] **Evidence collected**: Before/after screenshots, verification log
- [ ] **Code committed**: `git add .`, `git commit -m "wk7-lab2: audit + fix #9 (contrast)"`

---

## Next Steps

In **Week 8 Lab 1** you will:
1. Add partials and pagination to task list (Pebble template patterns)
2. Implement filtering with constraint-based design
3. Test dual-path filtering (HTMX + no-JS)

In **Week 8 Lab 2** you will:
1. Verify routing parity (HTMX vs. no-JS)
2. Document trade-offs (performance, complexity, accessibility)
3. Run no-JS verification script

**Preparation**:
- Ensure backlog up-to-date with Week 7 findings
- Have working task manager (add, edit, delete functional)
- Review audit evidence (you'll reference it in Task 1)

---

**Lab authored by**: COMP2850 Teaching Team, University of Leeds
**Last updated**: 2025-01-14
**Licence**: Academic use only (not for redistribution)
