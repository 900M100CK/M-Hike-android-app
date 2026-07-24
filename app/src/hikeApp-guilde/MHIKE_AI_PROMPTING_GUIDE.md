# M-Hike Project — AI Agent Prompting Guide

**Quick Reference**: How to effectively prompt AI to generate supporting documentation from the M-Hike briefs

---

## BEFORE YOU START

### Setup
1. Open Claude.ai or Claude app
2. Copy the relevant brief:
   - **Android**: `MHIKE_ANDROID_AGENT_PLAN.md`
   - **React Native**: `MHIKE_REACT_NATIVE_AGENT_PLAN.md`
3. Have the master briefing handy: `MHIKE_MASTER_BRIEFING.md`

### Key Principle
- **Be specific**: Reference exact sections, features, rules
- **Show examples**: "Like how the brief describes X..."
- **Ask for reasoning**: "Explain why you chose this pattern..."
- **Request iterations**: "Now add more detail to section Y..."

---

## PROMPT TEMPLATES

### Template 1: System Design Document

**Best for**: Understanding architecture, class relationships, data flow

```
I'm developing [Android/React Native] for a coursework project.
Here's my detailed brief: [PASTE MHIKE_ANDROID/REACT_NATIVE_AGENT_PLAN.md]

Create a System Design Document with:

1. **Architecture Overview**
   - Layered architecture (diagram or ASCII art)
   - Each layer's responsibility
   - How layers communicate

2. **Data Flow**
   - How data flows for Feature A (Add Hike)
   - From UI → Validation → Storage → Database
   - Include error handling at each step

3. **Class/Component Breakdown**
   - List all classes/components needed
   - Their responsibilities (2-3 sentences each)
   - Key methods/functions (signatures only)
   - Code example for 3 key classes

4. **Design Patterns**
   - Repository pattern (explained with code)
   - DAO pattern (explained with code)
   - State management (Zustand for RN; nothing for Android)

5. **Database Design**
   - Schema with SQL CREATE statements
   - Entity relationship diagram
   - Foreign key relationships

6. **Error Handling**
   - Strategy for database errors
   - Strategy for validation failures
   - User-facing error messages
   - Example try-catch block

Format: Markdown with code blocks (Java for Android, TypeScript for RN).
Include specific class names, method names, database field names.
Use Material Design 3 color codes where applicable.
```

**Expected Output**: 2000-3000 word document with diagrams, code examples, detailed explanations

---

### Template 2: UI/UX Design Specification

**Best for**: Screen layouts, visual design, component library

```
Using my [Android/React Native] brief, create a comprehensive UI/UX Design Spec:

1. **Design System**
   - Material Design 3 color palette (hex codes)
   - Typography scale (font sizes, weights for display, headline, body, label)
   - Spacing grid (8dp baseline with concrete examples)
   - Corner radius values
   - Shadow/elevation rules

2. **Component Library**
   - List 6-8 reusable components
   - Props/properties for each component
   - Usage examples
   - States (normal, hover, disabled, error)
   - Accessibility attributes

3. **Screen Layouts** (for Android screens/RN screens)
   - Hike List screen: description + layout diagram
   - Add/Edit Hike screen: description + field layout
   - Hike Detail screen: description + observation list layout
   - Search screen: description + filter layout
   - [Add Observation] screen: description + form layout
   
   For each screen, include:
   - Component breakdown
   - Spacing & alignment
   - Typography applied to each element
   - Color application

4. **Navigation Flow**
   - Screen relationships (which screen leads to which)
   - Back button behavior
   - Tab or drawer navigation (if applicable)

5. **Accessibility Guidelines**
   - Touch target sizes (48dp min)
   - Color contrast requirements (4.5:1)
   - Text alternatives for icons
   - Keyboard navigation
   - Screen reader considerations

6. **Responsive Design**
   - How layouts adapt for:
     - Small phones (5.2" - Nexus 5X)
     - Medium phones (5.5" - Pixel 3)
     - Large phones (5.7" - Nexus 6P)
     - Tablets (if applicable)
   - Orientation handling (portrait/landscape)

Format: Markdown with inline code for hex colors, component prop tables.
Include ASCII diagrams or describe layouts clearly.
Reference Material Design 3 official guidelines.
```

**Expected Output**: 3000-4000 word spec with layout descriptions, color palette, component details

---

### Template 3: Code Implementation Templates

**Best for**: Getting class/component stubs, method signatures, structure

```
Using my [Android/React Native] brief, create Code Implementation Templates for Feature [A/B/C/D]:

**For Android:**
Provide:
1. [ActivityName].java stub with:
   - Class declaration + imports
   - onCreate() method stub
   - Lifecycle methods (if needed)
   - Key method signatures (with JavaDoc)
   - Comments marking TODO sections

2. [DAO].java for database operations:
   - SQL queries as constants
   - Method signatures for CRUD operations
   - Example insert() with parameters

3. [Repository].java for business logic:
   - Constructor taking DAOs
   - Methods for add, update, delete, search
   - Error handling pattern

4. Validation utility class:
   - Methods for each validation rule
   - Return ValidationResult object
   - Example: validateHikeName(), validateDate()

**For React Native:**
Provide:
1. [Screen].tsx component stub with:
   - React component function
   - Hooks (useState, useEffect)
   - Zustand store usage
   - JSX structure for UI
   - Event handlers (stubs)

2. [Store].ts Zustand store:
   - Store interface definition
   - Initial state
   - Actions/methods
   - Async operations with error handling

3. Database utility:
   - TypeScript interfaces
   - Database initialization
   - Query functions
   - Error handling

4. Custom hook:
   - Example: useHikeForm
   - State management for form
   - Validation logic
   - Return object structure

For all templates:
- Include proper TS/Java type annotations
- Add JavaDoc/TSDoc comments
- Show error handling patterns
- Mark TODO sections clearly
- Use naming conventions from brief
```

**Expected Output**: Complete, compilable code stubs with all required sections

---

### Template 4: Testing & QA Plan

**Best for**: Test cases, testing strategy, quality assurance

```
Using my [Android/React Native] brief, create a Testing & QA Plan:

1. **Unit Tests**
   - List test cases for validation utilities
   - Example Jest (RN) or JUnit (Android) test suite
   - Minimum 3-4 test cases per utility
   - Test for edge cases (empty, null, invalid)

2. **Integration Tests**
   - Test data flow: UI → Validation → Database
   - Test scenarios for each feature (a-d)
   - Example: "Add hike with valid data → database save → list updates"
   - Test error scenarios

3. **Manual Testing Checklist**
   - Comprehensive checklist covering:
     - All fields in forms
     - Required vs. optional field validation
     - CRUD operations (create, read, update, delete)
     - Search functionality (basic + advanced)
     - Edge cases (empty lists, max length strings, etc.)
     - Offline functionality
     - App lifecycle (foreground/background)
     - Device rotation (landscape/portrait)

4. **Performance Testing**
   - Benchmarks (e.g., "render 100 hikes in <500ms")
   - Memory usage targets
   - Battery impact considerations
   - Search performance targets

5. **Accessibility Testing**
   - WCAG 2.1 AA checklist
   - Color contrast verification
   - Touch target size verification
   - Keyboard navigation testing
   - Screen reader testing (if applicable)

6. **Platform-Specific Testing** (React Native only)
   - iOS-specific test cases
   - Android-specific test cases
   - Platform difference handling verification

7. **Known Issues & Workarounds**
   - Common bugs to watch for
   - Platform-specific gotchas
   - Best practices to avoid problems

Format: Organized checklists with concrete examples.
Include actual test code for critical functions.
Reference brief's "Common Pitfalls" section.
```

**Expected Output**: Comprehensive test plan with code examples and checklist

---

### Template 5: Code Review Guidelines

**Best for**: Ensuring quality code during development

```
Using my [Android/React Native] brief, create a Code Review Checklist:

1. **Naming Conventions**
   - Classes: PascalCase examples
   - Methods: camelCase examples
   - Constants: UPPER_SNAKE_CASE examples
   - Variables: camelCase examples
   - Package/folder names: [Android pattern/RN pattern]
   - Include bad/good examples for each

2. **Code Structure**
   - Method length (should be < 30 lines)
   - Class length (should be < 500 lines)
   - Proper separation of concerns
   - No duplicate code patterns
   - Cohesion and coupling assessment

3. **Architecture Compliance**
   - Uses Repository pattern correctly
   - Uses DAO pattern correctly
   - State management isolated (RN: Zustand)
   - Business logic separate from UI
   - Database operations in appropriate layer

4. **Error Handling**
   - All database operations wrapped in try-catch
   - User-friendly error messages (no tech jargon)
   - Proper error logging
   - Edge cases handled (null, empty, invalid input)

5. **Documentation**
   - JavaDoc/TSDoc on public methods
   - Inline comments for "why", not "what"
   - README adequate
   - Code is self-documenting (clear names)

6. **Performance & Efficiency**
   - No UI-blocking database operations
   - Proper threading model (Android) or async (RN)
   - Efficient list rendering (RecyclerView/FlatList)
   - No memory leaks
   - Proper resource cleanup

7. **Android-Specific** (if Android):
   - Proper Activity lifecycle handling
   - ViewBinding used
   - No hardcoded strings (use resources)
   - Proper permissions handling
   - Thread safety for database access

8. **React Native-Specific** (if RN):
   - Strict TypeScript (no `any` types)
   - Functional components with hooks
   - Proper memoization (useMemo, useCallback)
   - Safe async handling (cleanup in useEffect)
   - Platform-specific code handled with Platform.select()

9. **Accessibility**
   - All buttons have descriptive labels
   - Touch targets ≥ 48dp
   - Color not sole indicator of status
   - Text contrast ≥ 4.5:1

10. **Testing**
    - Code is testable
    - Unit tests provided for critical functions
    - Edge cases covered in tests
    - Manual testing checklist complete

Format: Organized by category with examples.
Include code snippets for bad/good patterns.
Provide specific line numbers or patterns to look for.
```

**Expected Output**: Detailed checklist usable during actual code review

---

### Template 6: Database Schema & API Documentation

**Best for**: Understanding data structures and queries

```
Using my [Android/React Native] brief, create Database Documentation:

1. **Schema Overview**
   - ER diagram (ASCII or Mermaid)
   - Relationship descriptions
   - Data types and constraints

2. **Table Definitions** (SQL + descriptions)
   - hikes table: all fields with types, constraints, examples
   - observations table: all fields with types, constraints, examples
   - Indexes: which fields are indexed and why

3. **Queries Reference**
   - Insert new hike (with parameter list)
   - Update hike (with parameter list)
   - Delete hike (with cascade behavior)
   - Get all hikes (with sorting options)
   - Get hike by ID (with observations)
   - Search hikes (by name, location, date range, length range)
   - Get observations for hike
   - Add observation (with parameter list)
   - Delete observation

4. **Data Access Layer**
   - For Android: DAO interface & implementation
   - For React Native: Database query functions
   - Error handling for each operation
   - Example usage in Repository/store

5. **Initialization**
   - Database creation script
   - Schema validation
   - Migration strategy (if any)

6. **Data Validation**
   - Field constraints (length, range, format)
   - Validation at database level (if applicable)
   - Validation at application level

Format: SQL with code examples.
Include data type explanations.
Show example queries and results.
```

**Expected Output**: Complete database reference documentation

---

### Template 7: Feature Implementation Checklist

**Best for**: Tracking progress and ensuring completeness

```
Using my [Android/React Native] brief, create a Feature Implementation Checklist.

For each feature (a-g):

**Feature A: Hike Data Entry**
Required:
  - [ ] Name field (required, text input)
  - [ ] Location field (required, text input)
  - [ ] Date field (required, date picker)
  - [ ] Parking field (required, radio buttons)
  - [ ] Length field (required, number input)
  - [ ] Difficulty field (required, dropdown)
  - [ ] Description field (optional, text area)
  - [ ] Custom field 1 (optional)
  - [ ] Custom field 2 (optional)
  - [ ] Form validation (required fields check)
  - [ ] Error messages (for each required field)
  - [ ] Confirmation screen (show all data, allow edit)
  - [ ] Save to database
  - [ ] Cancel option (back to previous screen)

Testing:
  - [ ] Test valid data entry
  - [ ] Test missing required fields
  - [ ] Test field validation (invalid date, negative length, etc.)
  - [ ] Test confirmation screen
  - [ ] Test edit from confirmation screen
  - [ ] Test cancel operation

UI/UX:
  - [ ] Material Design 3 colors applied
  - [ ] Proper spacing (8dp baseline)
  - [ ] Touch targets ≥ 48dp
  - [ ] Clear error messages
  - [ ] Sensible defaults (today's date, etc.)
  - [ ] Keyboard handling

Code Quality:
  - [ ] Form validation in utility class
  - [ ] No duplicate validation code
  - [ ] Proper error handling
  - [ ] Code follows naming conventions
  - [ ] JavaDoc/TSDoc comments

[Repeat for Features B, C, D, g]

Format: Checkbox-style list.
Separate required, testing, UI, code quality sections.
Include subtasks for complex features.
```

**Expected Output**: Comprehensive tracking checklist for each feature

---

### Template 8: Andrej Karpathy-Style Skills Application

**Best for**: Applying LLM coding best practices

```
Using my [Android/React Native] brief and Andrej Karpathy's LLM coding advice,
create a "Coding Skills & Pitfalls Avoidance Guide" for this project:

Reference: https://github.com/multica-ai/andrej-karpathy-skills

1. **Common Pitfalls to Avoid** (project-specific)
   - Over-complicating state management
   - Missing error handling
   - Blocking UI thread with database operations
   - Not validating input properly
   - Ignoring platform differences (RN only)
   - Premature optimization

2. **Best Practices** (project-specific)
   - Start with simplest working solution
   - Test thoroughly before optimizing
   - Keep components small and focused
   - Separate concerns (UI, logic, data)
   - Document "why" decisions, not "what" code does
   - Use types to catch errors early (TypeScript for RN)

3. **Testing & Debugging Strategies**
   - Write tests for business logic early
   - Use logging strategically (not everywhere)
   - Profile performance with tools
   - Test edge cases before moving on
   - Manual testing checklist before video

4. **Code Organization Advice**
   - Package/folder structure from brief
   - One responsibility per class/component
   - Group related functions together
   - Avoid god objects/components

5. **AI Collaboration Best Practices**
   - Be specific in prompts (reference brief)
   - Ask for concrete examples, not pseudo-code
   - Iterate on suggestions (ask for refinements)
   - Understand generated code before using
   - Don't blindly copy; adapt to your needs
   - Validate AI suggestions against brief

6. **Common Mistakes in This Project**
   - [Android] Forgetting to run DB ops in background thread
   - [Android] Creating new Contexts unnecessarily
   - [RN] Not unsubscribing from listeners (memory leaks)
   - [RN] Using `any` type in TypeScript
   - [Both] Not validating required fields
   - [Both] Ignoring error cases
   - [Both] Not testing offline scenarios

Format: Practical, actionable guidance.
Include specific examples from brief.
Reference Andrej's original principles.
```

**Expected Output**: Practical guide to applying LLM best practices in this project

---

## ADVANCED PROMPTING TECHNIQUES

### Technique 1: Iterative Refinement

**First prompt**: Get initial version
```
Create a system design document for Android feature A.
[Include brief excerpt]
```

**Follow-up prompt 1**: Add detail
```
Great! Now expand the "Database Design" section:
- Show complete SQL schema
- Explain index strategy
- Include data validation rules
```

**Follow-up prompt 2**: Request improvements
```
Now add:
1. Sequence diagram for "Add Hike" workflow
2. Error handling patterns with code examples
3. Performance considerations
```

### Technique 2: Comparison & Contrast

```
I have both Android and React Native briefs.
Compare the architecture patterns:
- How is state managed differently?
- Where is threading/async handled?
- How do they differ in database access?

Create a side-by-side comparison table showing:
- Android approach | React Native approach | Tradeoffs
[For state, threading, database, error handling, etc.]
```

### Technique 3: Concrete Examples

```
Using the brief, show me:
1. Complete code for HikeRepository.addHike() method
2. Complete SQL for hike insertion
3. Unit test for validateHikeName()
4. Error handling example

Don't explain; just provide working code.
Then explain the design decision for each.
```

### Technique 4: Questioning the Design

```
Looking at the brief's architecture (Repository + DAO pattern):
1. Why use Repository pattern instead of direct DAO access?
2. What problems does it solve?
3. What tradeoffs exist?
4. Could we simplify this for a coursework project?
5. When would this pattern be overkill?

Support your answers with code examples.
```

### Technique 5: Catching Briefs Gaps

```
I'm implementing Feature D (Search) from the brief.
The brief says "basic search" and "advanced search" but doesn't specify:
1. How should partial matching work (fuzzy or substring)?
2. What order should results appear?
3. How should multi-criteria search combine filters (AND or OR)?
4. Should search be case-sensitive?
5. Any performance requirements?

Based on best practices, suggest reasonable defaults.
Explain tradeoffs for each decision.
```

---

## ANTI-PATTERNS: WHAT NOT TO DO

### ❌ Vague Prompts
```
"Create a system design"
"Make the UI look good"
"Write some code"
```

### ✅ Specific Prompts
```
"Create a system design document covering:
- Package structure from the brief
- Data flow for Feature A with error handling
- 3 key classes with code examples"
```

---

### ❌ Asking for Everything at Once
```
"Create system design, UI spec, code, tests, and documentation"
```

### ✅ Breaking Into Stages
1. First: System design
2. Then: UI/UX specification
3. Then: Code templates
4. Then: Testing plan

---

### ❌ Ignoring Constraints
```
"Generate code without worrying about Material Design 3"
```

### ✅ Explicit Constraints
```
"Use Material Design 3 colors:
- Primary: #6750A4
- Secondary: #625B71
Provide hex codes in output."
```

---

### ❌ Vague Quality Standards
```
"Make it production-ready"
"Write clean code"
```

### ✅ Specific Standards
```
"Follow Java naming conventions:
- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE
Include JavaDoc comments.
Methods should be < 30 lines."
```

---

## QUICK REFERENCE: WHAT TO PROMPT FOR

### Start Here
1. **System Design** → Understand architecture
2. **UI/UX Spec** → Understand visual design
3. **Code Templates** → Start implementation

### During Implementation
1. **Feature Checklist** → Track progress
2. **Code Review Guidelines** → Quality check
3. **Testing Plan** → Ensure coverage

### Before Submission
1. **Report Outline** → Structure documentation
2. **Demo Script** → Plan 15-minute video
3. **Q&A Prep** → Anticipate questions

---

## TEMPLATE: YOUR FIRST PROMPT

```
I'm developing a mobile hiker app (M-Hike) for coursework (COMP1786).
Here's my comprehensive brief:

[PASTE MHIKE_ANDROID_AGENT_PLAN.md OR MHIKE_REACT_NATIVE_AGENT_PLAN.md]

I need to understand the system architecture before coding.
Please create a System Design Document with:

1. Layered architecture diagram (ASCII or text description)
2. Data flow for Feature A: Add Hike
   - From UI input → validation → storage → database
   - Include error handling at each step
3. Class/component breakdown:
   - List 8-10 key classes (Android) or components (RN)
   - 2-3 sentence description of each
   - Their key methods
4. Design patterns:
   - Repository pattern (why, how, code example)
   - DAO pattern (Android only, why, how, code example)
5. Database schema:
   - SQL CREATE TABLE statements
   - Entity relationship diagram
6. Error handling:
   - Strategy for validation errors
   - Strategy for database errors
   - Example code

Format: Markdown with code blocks.
Use exact class/field names from the brief.
Include concrete code examples.
Explain design decisions.
```

---

**Document Version**: 1.0  
**Created**: 2025-26 COMP1786 Term 1  
**For**: M-Hike Project Development Teams

---

## RESOURCES

- Claude Docs: https://docs.anthropic.com/
- Andrej Karpathy Skills: https://github.com/multica-ai/andrej-karpathy-skills
- Material Design 3: https://m3.material.io/
- Android Docs: https://developer.android.com/
- React Native Docs: https://reactnative.dev/
