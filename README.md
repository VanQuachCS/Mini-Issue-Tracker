# Mini Issue Tracker (Java Swing + Console)

A desktop and terminal-based issue tracker built for the MPC Software Engineering Intern technical screening. Supports creating, listing, viewing, updating, deleting, status changes, filtering, searching, and assignment.

## Technology
- Java 17+ (standard library only)
- Java Swing desktop GUI (primary) and console input/output (alternative)
- Local TSV file with Base64-encoded free-text fields for persistence
- Standalone automated test harness (no external dependencies)

## Requirements
Install JDK 17 or newer. No environment variables or external services are required.

## Compile and run
From the repository root:

```bash
mkdir -p out
javac -d out src/*.java
java -cp out GuiMain
```

To run the original console version instead:

```bash
java -cp out Main
```

The program creates `data/issues.tsv` in the current working directory on first successful change. Run from the repository root to use the same data file.

## Run tests

```bash
javac -d out src/*.java tests/*.java
java -ea -cp out IssueServiceTest
```

## Assumptions
- One local user operates the application at a time; no authentication or networking.
- Assignments use a predefined sample user list: Alex, Sam, Jordan, Taylor.
- New issues start with status Open; valid statuses: Open, In Progress, Resolved, Closed.
- Valid priorities: Low, Medium, High, Critical.
- Search matches title or description, case-insensitively.
- Issue IDs are generated automatically and are not reused within a running session; after restarting, the next ID is based on the highest remaining saved ID.

## Limitations
- Desktop/console UI: the task brief lists web/mobile/API application examples; confirm acceptability with the reviewers.
- GUI interactions are manually tested; automated tests focus on the shared service and persistence layer.
- Sample users are hard-coded; there is no user management or authentication.
- No multi-process locking; do not run multiple instances against the same file.
- File format is custom TSV rather than a database; best suited for a small single-user exercise.
- The file is rewritten after each change; not designed for very large datasets.
- Tests use a built-in Java harness rather than JUnit to avoid dependencies.
- For safety, startup rejects invalid saved data rather than silently discarding records.

## Repository notes
`data/` and `out/` are excluded from Git. Commit source, tests, README, ENGINEERING.md, and .gitignore.
