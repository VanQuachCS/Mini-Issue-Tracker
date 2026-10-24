import java.nio.file.*;
import java.util.*;

/** Run with: java -ea -cp out IssueServiceTest */
public class IssueServiceTest {
    private static int checks = 0;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void invalid(Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException ex) { rejected = true; }
        check(rejected, "Expected invalid input rejection");
    }
    public static void main(String[] args) throws Exception {
        Path folder = Files.createTempDirectory("tracker-test-");
        Path file = folder.resolve("issues.tsv");
        try {
            IssueService service = new IssueService(new IssueRepository(file));
            Issue a = service.create("Login bug", "Button fails", "High", "Alex");
            check(a.getId() == 1, "Create ID");
            check(service.get(1).getTitle().equals("Login bug"), "Retrieve");
            check(service.all().size() == 1, "List");
            service.update(1, "Login broken", "Click fails", "Critical", "Sam");
            check(service.get(1).getPriority().equals("Critical"), "Update");
            service.changeStatus(1, "In Progress");
            check(service.filter("In Progress").size() == 1, "Filter");
            check(service.filter("Open").isEmpty(), "Other filter");
            check(service.search("CLICK").size() == 1, "Case-insensitive search");
            service.assign(1, "Jordan");
            check(service.get(1).getAssignee().equals("Jordan"), "Assignment");
            invalid(() -> { try { service.create(" ", "Desc", "High", "Alex"); } catch (java.io.IOException e) { throw new RuntimeException(e); } });
            invalid(() -> { try { service.create("Good", "Desc", "Unknown", "Alex"); } catch (java.io.IOException e) { throw new RuntimeException(e); } });
            invalid(() -> { try { service.create("Good", "Desc", "Low", "Unknown"); } catch (java.io.IOException e) { throw new RuntimeException(e); } });
            invalid(() -> service.get(999));
            invalid(() -> service.filter("Unknown"));
            IssueService reloaded = new IssueService(new IssueRepository(file));
            check(reloaded.get(1).getStatus().equals("In Progress"), "Persistence");
            check(reloaded.get(1).getAssignee().equals("Jordan"), "Persist assignment");
            reloaded.delete(1);
            check(new IssueService(new IssueRepository(file)).all().isEmpty(), "Delete persists");
            Issue b = reloaded.create("Pipe | and tab\t", "Line 1\nLine 2", "Low", "Taylor");
            check(new IssueService(new IssueRepository(file)).get(b.getId()).getDescription().contains("\n"), "Special characters");
            System.out.println("PASS: " + checks + " checks");
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(folder);
        }
    }
}
