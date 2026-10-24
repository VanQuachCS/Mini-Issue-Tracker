import javax.swing.*;
import java.nio.file.Path;

/** Launches the Swing interface using the same service and saved data as console mode. */
public class GuiMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                IssueService service = new IssueService(new IssueRepository(Path.of("data", "issues.tsv")));
                new SwingUI(service).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Unable to start: " + ex.getMessage(), "Startup error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
