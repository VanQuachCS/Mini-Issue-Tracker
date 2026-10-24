import java.io.IOException;
import java.util.*;

public class IssueService {
    public static final List<String> STATUSES = List.of("Open", "In Progress", "Resolved", "Closed");
    public static final List<String> PRIORITIES = List.of("Low", "Medium", "High", "Critical");
    public static final List<String> USERS = List.of("Alex", "Sam", "Jordan", "Taylor");
    private final IssueRepository repository;
    private final List<Issue> issues;
    private int nextId = 1;

    public IssueService(IssueRepository repository) throws IOException {
        this.repository = repository;
        issues = repository.load();
        Set<Integer> ids = new HashSet<>();
        for (Issue issue : issues) {
            if (issue.getId() < 1 || !ids.add(issue.getId())) throw new IOException("Invalid or duplicate saved issue ID");
            validate(issue.getTitle(), issue.getDescription(), issue.getPriority(), issue.getAssignee());
            requireChoice(issue.getStatus(), STATUSES, "status");
            if (issue.getId() == Integer.MAX_VALUE) throw new IOException("Issue ID limit reached");
            nextId = Math.max(nextId, issue.getId() + 1);
        }
    }
    private static void requireText(String text, String name) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException(name + " cannot be empty");
        if (text.length() > 2000) throw new IllegalArgumentException(name + " is too long");
    }
    private static void requireChoice(String value, List<String> options, String name) {
        if (!options.contains(value)) throw new IllegalArgumentException("Invalid " + name + ": " + value);
    }
    private static void validate(String title, String description, String priority, String assignee) {
        requireText(title, "Title");
        requireText(description, "Description");
        requireChoice(priority, PRIORITIES, "priority");
        requireChoice(assignee, USERS, "assignee");
    }
    public List<Issue> all() { return List.copyOf(issues); }
    public Issue get(int id) {
        return issues.stream().filter(i -> i.getId() == id).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Issue #" + id + " not found"));
    }
    public Issue create(String title, String description, String priority, String assignee) throws IOException {
        validate(title, description, priority, assignee);
        if (nextId == Integer.MAX_VALUE) throw new IllegalStateException("Issue ID limit reached");
        Issue issue = new Issue(nextId, title.trim(), description.trim(), priority, assignee);
        issues.add(issue);
        try { repository.save(issues); nextId++; return issue; }
        catch (IOException ex) { issues.remove(issue); throw ex; }
    }
    public void update(int id, String title, String description, String priority, String assignee) throws IOException {
        validate(title, description, priority, assignee);
        Issue old = get(id);
        int index = issues.indexOf(old);
        Issue replacement = new Issue(id, title.trim(), description.trim(), old.getStatus(), priority,
            assignee, old.getCreatedAt(), java.time.LocalDateTime.now());
        issues.set(index, replacement);
        try { repository.save(issues); }
        catch (IOException ex) { issues.set(index, old); throw ex; }
    }
    public void changeStatus(int id, String status) throws IOException {
        requireChoice(status, STATUSES, "status");
        Issue old = get(id);
        int index = issues.indexOf(old);
        Issue replacement = new Issue(id, old.getTitle(), old.getDescription(), status, old.getPriority(),
            old.getAssignee(), old.getCreatedAt(), java.time.LocalDateTime.now());
        issues.set(index, replacement);
        try { repository.save(issues); }
        catch (IOException ex) { issues.set(index, old); throw ex; }
    }
    public void assign(int id, String assignee) throws IOException {
        requireChoice(assignee, USERS, "assignee");
        Issue old = get(id);
        int index = issues.indexOf(old);
        Issue replacement = new Issue(id, old.getTitle(), old.getDescription(), old.getStatus(),
            old.getPriority(), assignee, old.getCreatedAt(), java.time.LocalDateTime.now());
        issues.set(index, replacement);
        try { repository.save(issues); }
        catch (IOException ex) { issues.set(index, old); throw ex; }
    }
    public void delete(int id) throws IOException {
        Issue old = get(id);
        int index = issues.indexOf(old);
        issues.remove(index);
        try { repository.save(issues); }
        catch (IOException ex) { issues.add(index, old); throw ex; }
    }
    public List<Issue> filter(String status) {
        requireChoice(status, STATUSES, "status");
        return issues.stream().filter(i -> i.getStatus().equals(status)).toList();
    }
    public List<Issue> search(String query) {
        requireText(query, "Search query");
        String q = query.toLowerCase(Locale.ROOT);
        return issues.stream().filter(i -> i.getTitle().toLowerCase(Locale.ROOT).contains(q)
            || i.getDescription().toLowerCase(Locale.ROOT).contains(q)).toList();
    }
}
