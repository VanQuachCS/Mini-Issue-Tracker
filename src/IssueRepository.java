import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;

public class IssueRepository {
    private final Path file;
    public IssueRepository(Path file) { this.file = file; }

    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
    private static String decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }
    public List<Issue> load() throws IOException {
        List<Issue> issues = new ArrayList<>();
        if (!Files.exists(file)) return issues;
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) continue;
            try {
                String[] parts = line.split("\t", -1);
                if (parts.length != 8) throw new IllegalArgumentException("Wrong number of fields");
                issues.add(new Issue(Integer.parseInt(parts[0]), decode(parts[1]), decode(parts[2]),
                    parts[3], parts[4], decode(parts[5]), LocalDateTime.parse(parts[6]),
                    LocalDateTime.parse(parts[7])));
            } catch (RuntimeException ex) {
                throw new IOException("Invalid saved data at line " + lineNumber, ex);
            }
        }
        return issues;
    }
    public void save(Collection<Issue> issues) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent, "issues-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (Issue i : issues) {
                lines.add(i.getId() + "\t" + encode(i.getTitle()) + "\t" + encode(i.getDescription())
                    + "\t" + i.getStatus() + "\t" + i.getPriority() + "\t"
                    + encode(i.getAssignee()) + "\t" + i.getCreatedAt() + "\t" + i.getUpdatedAt());
            }
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try { Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temp); }
    }
}
