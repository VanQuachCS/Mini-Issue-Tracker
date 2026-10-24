import java.nio.file.Path;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            IssueRepository repository = new IssueRepository(Path.of("data", "issues.tsv"));
            IssueService service = new IssueService(repository);
            new ConsoleUI(service, new Scanner(System.in)).run();
        } catch (IOException | IllegalArgumentException ex) {
            System.err.println("Unable to start application: " + ex.getMessage());
            System.exit(1);
        }
    }
}
