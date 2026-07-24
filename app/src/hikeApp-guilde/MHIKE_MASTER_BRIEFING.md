# M-Hike Project — Master Briefing & AI Agent Guide

**Project**: M-Hike Hiker Management Application  
**Coursework**: COMP1786 Mobile Application Design and Development (Term 1, 2025-26)  
**Status**: Dual-Platform Implementation (Android Native + React Native Cross-Platform)  
**Assessment**: 80% implementation + 20% report  

---

## PROJECT OVERVIEW

**M-Hike** is a mobile application enabling outdoor hikers to:
- Plan and record hike details (name, location, date, difficulty, parking, length)
- Capture real-time observations during hikes (wildlife, vegetation, weather, trail conditions)
- Search and manage hike data locally
- Operate without internet connectivity (local-first model)

### Two Parallel Implementations

| Aspect | Android Native | React Native |
|--------|---|---|
| **Codebase** | Single platform | Cross-platform |
| **Language** | Java | JavaScript/TypeScript |
| **Framework** | Android SDK | React Native + Expo |
| **Database** | SQLite | SQLite (expo-sqlite) |
| **Platforms** | Android only | iOS + Android |
| **Deployment** | APK | APK + IPA (via Expo) |
| **Coursework Requirement** | Features a-d (core) | Features e-g (cross-platform) |
| **Note** | Native implementation | Replaces Xamarin/MAUI requirement |

### Shared Features (Both Implementations)

**Core Features (a-d)**:
- a) Hike data entry with validation
- b) Local SQLite persistence + CRUD operations
- c) Add observations to hikes
- d) Search functionality

**Extended (Feature g)**:
- Additional creative features (photos, GPS, maps, etc.)

---

## TWO AGENT BRIEFING DOCUMENTS

### Document 1: MHIKE_ANDROID_AGENT_PLAN.md
**Purpose**: Guides AI agents in creating Android-specific documentation

**Covers**:
- Android architecture (Activities, DAOs, Repositories)
- Material Design 3 implementation (Android-specific)
- Java code quality & conventions
- Android-specific UI patterns (RecyclerView, etc.)
- Testing strategy for Android
- Lifecycle & threading considerations

**Use this brief when asking AI to generate**:
- System design document (Android architecture)
- UI/UX mockups (Android Material Design)
- Activity class templates
- Database schema & DAO implementations
- Code review guidelines for Android

---

### Document 2: MHIKE_REACT_NATIVE_AGENT_PLAN.md
**Purpose**: Guides AI agents in creating React Native documentation

**Covers**:
- React Native + Expo setup
- TypeScript project structure
- Zustand state management
- React Navigation patterns
- FlatList & component architecture
- Cross-platform considerations (iOS/Android differences)
- Testing strategy for React Native

**Use this brief when asking AI to generate**:
- System design document (React Native architecture)
- UI/UX design tokens & component library
- Screen implementation templates
- State management patterns
- Database integration examples
- Cross-platform testing strategy

---

## HOW TO USE THESE BRIEFS WITH AI AGENTS

### Workflow: AI-Assisted Documentation Creation

#### Step 1: Choose Your Scope
Decide which document you need:
- **System Design Spec** → Architecture & design patterns
- **UI/UX Design Guide** → Screens, layouts, design tokens
- **Code Templates** → Class stubs, components, utilities
- **API Documentation** → Database schemas, state interfaces
- **Testing Plan** → Test cases, manual testing checklist

#### Step 2: Prepare Your Brief
1. Copy the relevant plan (Android or React Native)
2. Paste into Claude or Claude app
3. Add your specific request

**Example Prompt**:
```
I have this M-Hike Android development brief. 
Please create a detailed System Design document covering:
- Package structure and class responsibilities
- Database schema with ER diagram
- Activity flow and navigation
- Data flow between layers (UI → Repository → DAO → SQLite)
Include code examples for key classes.
```

#### Step 3: Iterate & Refine
- Ask follow-up questions about design decisions
- Request code examples or diagrams
- Push back on unclear sections
- Request revisions focusing on specific aspects

#### Step 4: Output Artifact
- Generate a markdown document or artifact
- Include diagrams (ASCII or Mermaid)
- Provide code snippets for critical components
- Link to official documentation for reference

---

## DOCUMENT CREATION TEMPLATES

### Template 1: System Design Document

**Sections to Request**:
1. Architecture overview (layered diagram)
2. Data flow (how data moves through layers)
3. Class responsibilities (with code samples)
4. Design patterns used (Repository, DAO, etc.)
5. Error handling strategy
6. Performance considerations
7. Testing approach

**Example Request**:
```
Using the Android brief, create a System Design document with:
- Visual architecture diagram
- Class UML showing relationships
- Sequence diagram for "Add Hike" workflow
- Database schema with field types
- Repository pattern explanation
Include Java code examples for HikeRepository and HikeDAO.
```

---

### Template 2: UI/UX Design Specification

**Sections to Request**:
1. Design tokens (colors, typography, spacing)
2. Screen layouts (wireframes or descriptions)
3. Component library (reusable UI elements)
4. Navigation flows
5. Accessibility guidelines
6. Responsive design strategy
7. Platform-specific guidelines (if cross-platform)

**Example Request**:
```
Using the React Native brief, create a comprehensive UI/UX Design Spec including:
- Material 3 color palette with hex codes
- Typography scale (font sizes, weights)
- Spacing grid (8dp baseline examples)
- 8 screen layouts with component descriptions
- Accessibility checklist (WCAG 2.1 AA)
- Component library (HikeCard, FormField, SearchFilter, etc.)
```

---

### Template 3: Code Implementation Guide

**Sections to Request**:
1. Class/component stubs (empty implementations)
2. Method signatures with documentation
3. Error handling patterns
4. Testing examples
5. Common pitfalls to avoid
6. Code review checklist

**Example Request**:
```
Using the Android brief, create a Code Implementation Guide for Feature A (Hike Entry):
- Stub classes for AddEditHikeActivity, HikeDAO, HikeRepository
- Method signatures with JavaDoc comments
- Form validation helper functions
- Error handling examples
- Unit test cases
- Code review checklist (naming, structure, validation)
```

---

### Template 4: Testing & Quality Assurance Plan

**Sections to Request**:
1. Unit test examples (with framework setup)
2. Integration test scenarios
3. Manual testing checklist
4. Performance benchmarks
5. Accessibility audit checklist
6. Edge case handling

**Example Request**:
```
Using the React Native brief, create a Testing & QA Plan including:
- Jest unit test examples (validation, store, database)
- Manual test cases for each feature (a-d)
- Performance testing strategy (FlatList rendering, search)
- Cross-platform testing matrix (iOS/Android devices)
- Accessibility checklist (WCAG 2.1 AA)
- Known limitations and workarounds
```

---

## CRITICAL RULES FOR AI AGENTS

When working from these briefs, AI agents should follow:

### 1. **Feature Completeness**
- Implement features a-d completely before feature g
- Feature g (additional features) must not compromise core functionality
- Core features: Add, View, Edit, Delete, Search

### 2. **Design Consistency**
- Follow Material Design 3 color palette exactly
- Use specified typography (font sizes, weights)
- Maintain 8dp spacing baseline
- Ensure accessibility (WCAG 2.1 AA minimum)

### 3. **Code Quality**
- **Android**: Follow Java naming conventions (PascalCase classes, camelCase methods)
- **React Native**: Strict TypeScript, no `any` types
- Comments explain "why", not "what"
- Methods ≤ 30 lines; classes ≤ 500 lines
- No duplicate code; extract to utilities

### 4. **Error Handling**
- Wrap all database operations in try-catch
- Show user-friendly error messages (no technical jargon)
- Log errors for debugging
- Handle edge cases (empty data, null values, invalid input)

### 5. **Documentation**
- Code is self-documenting (clear naming, simple logic)
- JavaDoc/TSDoc for public methods
- README explains architecture & setup
- Examples provided for non-obvious patterns

### 6. **Testing**
- Design code to be testable (dependency injection, pure functions)
- Provide unit test examples
- Create comprehensive manual testing checklist
- Test on both platforms (Android & iOS for React Native)

### 7. **Performance**
- Database operations on background threads (Android) or async (React Native)
- UI updates on main thread only
- Efficient list rendering (RecyclerView or FlatList)
- No memory leaks or dangling listeners

### 8. **Accessibility**
- All interactive elements have descriptive labels
- Minimum 48dp touch targets (iOS) / 56dp (Android)
- Color + icons/text for status (not color alone)
- Text contrast ≥ 4.5:1 on white background

### 9. **No Inventions**
- Don't add requirements not in the brief
- Don't skip validation or error handling "for simplicity"
- Don't use deprecated APIs or outdated patterns
- Don't assume knowledge of implementation details

### 10. **Specific Over Generic**
- Provide concrete code examples, not pseudo-code
- Use actual class/method names from the brief
- Include database field names, column types
- Show real Material Design 3 hex codes, not "colors.primary"

---

## EXAMPLE: AI AGENT CONVERSATION FLOW

### Scenario: Request System Design Document for Android

**User to AI:**
```
I have a coursework project for an Android hiker app. 
Here's my detailed brief [pastes MHIKE_ANDROID_AGENT_PLAN.md]

Can you create a System Design Document covering:
1. Layered architecture diagram
2. Data flow for each feature (a-d)
3. Class responsibilities with code snippets
4. Database schema with ER diagram
5. Error handling strategy
6. Testing approach

Focus on Java best practices and Material Design 3.
```

**AI Generates:**
- Architecture diagram (Mermaid or ASCII)
- UML class diagram showing relationships
- Sequence diagrams for key workflows
- Database schema with field types & constraints
- Code snippets for:
  - HikeActivity (screen)
  - HikeDAO (data access)
  - HikeRepository (business logic)
  - Validation utility
- Error handling examples
- Unit test examples
- Testing checklist

**User Feedback:**
```
Great! Now can you:
1. Add more detail to the Repository pattern explanation
2. Include an example of handling database transaction failures
3. Show how to prevent ANR (Application Not Responding) errors
4. Add a code review checklist focusing on Android-specific issues
```

**AI Refines:**
- Deeper Repository pattern explanation with concrete examples
- Try-catch pattern for database operations
- AsyncTask/Thread pattern for background operations
- Android-specific code review checklist (lifecycle, threading, etc.)

---

## INTEGRATION WITH ACTUAL DEVELOPMENT

### During Implementation
1. Keep both briefs open while coding
2. Reference the "Common Pitfalls" section
3. Follow the "Development Workflow" (8-week timeline)
4. Use AI agents to generate:
   - Class stubs for new files
   - Database queries
   - Test cases
   - Bug fix suggestions

### During Code Review
1. Compare code against the "Code Quality Rules" section
2. Check accessibility guidelines
3. Verify error handling is complete
4. Ensure performance (no ANR, smooth scrolling)

### During Report Writing
1. Reference the brief's "Grading Criteria" section
2. Use briefs to support evaluation claims
3. Explain design decisions referencing the brief's rationale
4. Include architecture diagrams from briefs

---

## DELIVERABLES CHECKLIST

### For Android Implementation
- [ ] Source code (organized by package structure in brief)
- [ ] Gradle build file with dependencies
- [ ] README.md (setup instructions from brief)
- [ ] Database schema (from brief, implemented in SQLite)
- [ ] Demonstration video (15 minutes, showing features a-d + g)
- [ ] Comprehensive report (sections 1-5 per brief)

### For React Native Implementation
- [ ] Complete React Native + Expo project
- [ ] TypeScript source code (organized per brief)
- [ ] package.json with locked dependencies
- [ ] README.md (setup instructions from brief)
- [ ] Database schema (expo-sqlite, per brief)
- [ ] Cross-platform testing evidence (iOS + Android)
- [ ] Demonstration video (15 minutes)
- [ ] Comprehensive report (referencing cross-platform challenges)

### Both Implementations
- [ ] ZIP file containing all source code
- [ ] Video links (YouTube unlisted or cloud storage)
- [ ] Q&A session preparation (know the code & design decisions)
- [ ] Plagiarism check passed (code commented, sources cited)

---

## ASSESSMENT CRITERIA (From Coursework)

### Feature Implementation (Features a-d)
- Features a-d are **mandatory**
- Feature g (additional features) is **bonus**
- Quality matters more than quantity

### Code Quality
- Follows naming conventions (specified in briefs)
- Logical structure (packages, classes, methods)
- Comments explain decisions
- Uses appropriate language features (Java generics, TypeScript types)
- Avoids code duplication

### UI/UX
- Clean, simple navigation (per brief design)
- Appropriate controls (pickers, text inputs, buttons)
- Material Design 3 adherence
- No manual required to use
- Responsive to different screen sizes

### Demonstration (15 minutes)
- Show all implemented features
- Link features to code
- Explain design decisions
- Be prepared for Q&A

### Report (20% of grade)
- Feature checklist (accurately reflects implementation)
- Screenshots with annotations
- Reflection (350 words, lessons learned)
- Evaluation (700-1000 words on HCI, security, screen sizes, deployment)
- Code listings (Java or TypeScript)

**Passing Grade**: 40% (minimum)  
**1st Class**: 70%+ (strong implementation + excellent report)

---

## REFERENCES & RESOURCES

### Android
- https://developer.android.com/
- https://m3.material.io/ (Material Design 3)
- https://developer.android.com/jetpack (Architecture components)
- Java Conventions: https://www.oracle.com/java/technologies/javase/codeconventions

### React Native
- https://reactnative.dev/
- https://docs.expo.dev/
- https://reactnavigation.org/
- https://m3.material.io/ (Material Design 3)

### Design & UX
- https://design.md/ (design.md template)
- WCAG 2.1: https://www.w3.org/WAI/WCAG21/quickref/
- Andrej Karpathy Skills: https://github.com/multica-ai/andrej-karpathy-skills

### Development Tools
- **Android**: Android Studio, Gradle, SQLite Browser
- **React Native**: VS Code, Expo CLI, Xcode (for iOS)
- **Database**: SQLite Browser, Room (Android), expo-sqlite (RN)

---

## TIPS FOR SUCCESS

### Phase 1: Setup (Week 1-2)
- Get database working first
- Test CRUD operations thoroughly
- Build foundation before UI

### Phase 2: Core Features (Week 3-6)
- Implement a-d completely
- Test each feature independently
- Don't rush to UI polish yet

### Phase 3: Polish (Week 7)
- Refinement & bug fixes
- Additional features (feature g)
- Performance optimization
- Accessibility audit

### Phase 4: Documentation (Week 8)
- Record video (practice first!)
- Write report with concrete examples
- Prepare for Q&A (know your code)
- Gather screenshots/annotations

### Key Success Factors
1. **Complete core features first** — partial features = lost marks
2. **Test thoroughly** — edge cases matter
3. **Follow design system** — Material Design 3 is non-negotiable
4. **Write clean code** — readability > cleverness
5. **Document decisions** — report asks "why", not just "what"
6. **Prepare for Q&A** — understand your own code deeply

---

## FINAL NOTES

These briefs are **comprehensive** but not exhaustive. They define:
- ✅ What to build (features a-g)
- ✅ How to structure it (architecture, packages)
- ✅ Design consistency (Material Design 3)
- ✅ Code quality standards (naming, structure, comments)
- ✅ Testing approach (unit, manual, performance)

They do NOT define:
- ❌ Exact implementation details (you own those decisions)
- ❌ Performance optimizations (research context-specific solutions)
- ❌ Additional features beyond examples (your creativity)
- ❌ How to handle specific edge cases (think critically)

**Use AI agents as partners**: Ask for suggestions, templates, and guidance. Don't ask for complete solutions. The goal is learning & building a quality app for your portfolio.

---

**Document Version**: 1.0  
**Created**: 2025-26 COMP1786 Term 1  
**For**: M-Hike Project (Dual-Platform Mobile Application)
