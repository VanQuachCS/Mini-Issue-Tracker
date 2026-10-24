import java.io.IOException;
import java.util.*;

public class ConsoleUI {
    private final Scanner scanner;
    private final IssueService service;
    public ConsoleUI(IssueService service, Scanner scanner) { this.service = service; this.scanner = scanner; }
    private String prompt(String label) {
        System.out.print(label + ": ");
        if (!scanner.hasNextLine()) throw new NoSuchElementException("Input closed");
        return scanner.nextLine().trim();
    }
    private int id() {
        try { return Integer.parseInt(prompt("Issue ID")); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Issue ID must be a number"); }
    }
    private String choose(String label, List<String> options) {
        System.out.println(label + " options: " + String.join(", ", options));
        String answer = prompt(label);
        return options.stream().filter(x -> x.equalsIgnoreCase(answer)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid " + label.toLowerCase()));
    }
    private void show(Issue i) {
        System.out.printf("#%d | %s | %s | %s | %s%n", i.getId(), i.getTitle(), i.getStatus(),
            i.getPriority(), i.getAssignee());
    }
    private void showAll(List<Issue> items) {
        if (items.isEmpty()) System.out.println("No matching issues.");
        else items.forEach(this::show);
    }
    private void details(Issue i) {
        System.out.printf("ID: %d%nTitle: %s%nDescription: %s%nStatus: %s%nPriority: %s%nAssignee: %s%nCreated: %s%nUpdated: %s%n",
            i.getId(), i.getTitle(), i.getDescription(), i.getStatus(), i.getPriority(), i.getAssignee(),
            i.getCreatedAt(), i.getUpdatedAt());
    }
    public void run() {
        while (true) {
            System.out.println("\n=== MINI ISSUE TRACKER ===");
            System.out.println("1 Create  2 View All  3 View One  4 Update  5 Delete");
            System.out.println("6 Change Status  7 Filter Status  8 Search  9 Assign  0 Exit");
            try {
                String option = prompt("Choice");
                switch (option) {
                    case "0" -> { System.out.println("Goodbye!"); return; }
                    case "1" -> {
                        Issue issue = service.create(prompt("Title"), prompt("Description"),
                            choose("Priority", IssueService.PRIORITIES), choose("Assignee", IssueService.USERS));
                        System.out.println("Created issue #" + issue.getId());
                    }
                    case "2" -> showAll(service.all());
                    case "3" -> details(service.get(id()));
                    case "4" -> {
                        int issueId = id();
                        details(service.get(issueId));
                        service.update(issueId, prompt("New title"), prompt("New description"),
                            choose("Priority", IssueService.PRIORITIES), choose("Assignee", IssueService.USERS));
                        System.out.println("Updated.");
                    }
                    case "5" -> {
                        int issueId = id();
                        if (prompt("Type DELETE to confirm").equals("DELETE")) {
                            service.delete(issueId); System.out.println("Deleted.");
                        } else System.out.println("Cancelled.");
                    }
                    case "6" -> { service.changeStatus(id(), choose("Status", IssueService.STATUSES)); System.out.println("Status updated."); }
                    case "7" -> showAll(service.filter(choose("Status", IssueService.STATUSES)));
                    case "8" -> showAll(service.search(prompt("Search title or description")));
                    case "9" -> { service.assign(id(), choose("Assignee", IssueService.USERS)); System.out.println("Assigned."); }
                    default -> System.out.println("Choose a number from 0 to 9.");
                }
            } catch (NoSuchElementException ex) {
                System.out.println("Input ended. Goodbye!"); return;
            } catch (IllegalArgumentException | IOException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
