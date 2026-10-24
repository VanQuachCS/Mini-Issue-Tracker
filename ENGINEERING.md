# Engineering Decisions

## 1. Why this stack?
Java is the language I have practiced most for object-oriented programming. A Swing desktop interface, optional console interface, and Java standard-library file operations keep the scope small enough to focus on correctness, validation, and maintainability. I avoided introducing a database or web framework solely for this exercise. **Tradeoff:** desktop and console applications are not among the explicitly listed example application types, so its acceptance should be confirmed.

## 2. How is the application structured?
- `Issue`: issue data and timestamps.
- `IssueService`: validation, issue operations, filtering, and searching.
- `IssueRepository`: reads and writes persistent issue records.
- `SwingUI`: Swing table, dialogs, filters, and actions; delegates operations to service.
- `GuiMain`: starts the desktop interface on Swing event dispatch thread.
- `ConsoleUI`: optional terminal menu and input/output.
- `Main`: starts the console program and handles startup failures.
- `IssueServiceTest`: automated checks using temporary storage.

The service is separated from the UI so it can be tested without simulating terminal input. File saves use a temporary file and replacement to reduce the risk of partially written records. Free-text fields are Base64-encoded so tabs and newlines can be stored without breaking the format.

## 3. Most difficult part
*Customize this answer to reflect your actual experience before submitting.* One likely challenge is keeping memory and disk consistent when a write fails. The service restores the previous in-memory state if persistence throws an `IOException`.

## 4. What would I improve with two more days?
Add editable user accounts, stronger data-format versioning, better recovery from corrupt files, expanded tests, and a web interface if required by the reviewers.

## 5. What changes at 100,000 users?
Replace the local file with a transactional database, add authentication and authorization, use a network API, paginate issue lists, index common search/filter fields, handle concurrent updates, add structured logging and monitoring, and use integration/load tests. The current in-memory list and full-file rewrite would not scale.

## AI assistance
AI-assisted code generation was used. Review and understand each file, run the tests, and adapt the reasoning above to accurately reflect your own decisions before submission.
