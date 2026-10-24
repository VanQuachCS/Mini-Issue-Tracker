import java.time.LocalDateTime;

public class Issue {
    private final int id;
    private String title, description, status, priority, assignee;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Issue(int id, String title, String description, String priority, String assignee) {
        this(id, title, description, "Open", priority, assignee, LocalDateTime.now(), LocalDateTime.now());
    }

    public Issue(int id, String title, String description, String status, String priority,
                 String assignee, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assignee = assignee;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getAssignee() { return assignee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void update(String title, String description, String priority, String assignee) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignee = assignee;
        this.updatedAt = LocalDateTime.now();
    }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    public void setAssignee(String assignee) {
        this.assignee = assignee;
        this.updatedAt = LocalDateTime.now();
    }
}
