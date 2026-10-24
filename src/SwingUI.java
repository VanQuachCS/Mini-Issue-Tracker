import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Desktop interface; all validation and persistence remain in IssueService. */
public class SwingUI extends JFrame {
    private final IssueService service;
    private final DefaultTableModel model;
    private final JTable table;
    private final JComboBox<String> filter;
    private final JTextField search;
    private final JLabel count;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SwingUI(IssueService service) {
        super("Mini Issue Tracker");
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 500));
        setSize(1050, 620);
        setLocationRelativeTo(null);
        model = new DefaultTableModel(new Object[]{"ID", "Title", "Status", "Priority", "Assignee", "Updated"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        filter = new JComboBox<>(new String[]{"All statuses", "Open", "In Progress", "Resolved", "Closed"});
        search = new JTextField(20);
        count = new JLabel();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton create = button("Create", () -> editIssue(null));
        JButton view = button("View details", this::viewIssue);
        JButton edit = button("Edit", () -> editIssue(selected()));
        JButton delete = button("Delete", this::deleteIssue);
        JButton status = button("Change status", this::changeStatus);
        JButton assign = button("Assign", this::assign);
        top.add(create); top.add(view); top.add(edit); top.add(delete); top.add(status); top.add(assign);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottom.add(new JLabel("Status:")); bottom.add(filter);
        bottom.add(new JLabel("Search:")); bottom.add(search);
        bottom.add(button("Apply", this::refresh));
        bottom.add(button("Clear", () -> { search.setText(""); filter.setSelectedIndex(0); refresh(); }));
        bottom.add(count);
        filter.addActionListener(e -> refresh());
        search.addActionListener(e -> refresh());
        table.getSelectionModel().addListSelectionListener(e -> { /* selection handled by actions */ });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewIssue();
            }
        });
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        refresh();
    }

    private JButton button(String title, Runnable action) {
        JButton b = new JButton(title);
        b.addActionListener(e -> {
            try { action.run(); }
            catch (IllegalArgumentException | IllegalStateException ex) { error(ex.getMessage()); }
            catch (Exception ex) { error("Unexpected error. Please try again."); }
        });
        return b;
    }

    private void error(String message) { JOptionPane.showMessageDialog(this, message, "Issue Tracker", JOptionPane.ERROR_MESSAGE); }
    private void saveAction(SaveAction action) {
        try { action.run(); refresh(); }
        catch (IllegalArgumentException ex) { error(ex.getMessage()); }
        catch (IOException ex) { error("Unable to save changes. Check file permissions and disk space."); }
    }
    @FunctionalInterface private interface SaveAction { void run() throws IOException; }

    private Issue selected() {
        int row = table.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException("Select an issue first.");
        int id = (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
        return service.get(id);
    }

    private void refresh() {
        String query = search.getText().trim().toLowerCase(java.util.Locale.ROOT);
        String status = (String) filter.getSelectedItem();
        List<Issue> issues = service.all();
        model.setRowCount(0);
        int visible = 0;
        for (Issue i : issues) {
            if (!"All statuses".equals(status) && !i.getStatus().equals(status)) continue;
            if (!query.isEmpty() && !i.getTitle().toLowerCase(java.util.Locale.ROOT).contains(query)
                    && !i.getDescription().toLowerCase(java.util.Locale.ROOT).contains(query)) continue;
            model.addRow(new Object[]{i.getId(), i.getTitle(), i.getStatus(), i.getPriority(), i.getAssignee(), DATE.format(i.getUpdatedAt())});
            visible++;
        }
        count.setText(visible + " issue(s)");
    }

    private void viewIssue() {
        Issue i = selected();
        JTextArea details = new JTextArea(
            "ID: " + i.getId() + "\nTitle: " + i.getTitle() + "\n\nDescription:\n" + i.getDescription()
            + "\n\nStatus: " + i.getStatus() + "\nPriority: " + i.getPriority()
            + "\nAssignee: " + i.getAssignee() + "\nCreated: " + DATE.format(i.getCreatedAt())
            + "\nUpdated: " + DATE.format(i.getUpdatedAt()));
        details.setEditable(false); details.setLineWrap(true); details.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(details), "Issue #" + i.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void editIssue(Issue existing) {
        JTextField title = new JTextField(existing == null ? "" : existing.getTitle(), 28);
        JTextArea description = new JTextArea(existing == null ? "" : existing.getDescription(), 5, 28);
        description.setLineWrap(true); description.setWrapStyleWord(true);
        JComboBox<String> priority = new JComboBox<>(IssueService.PRIORITIES.toArray(new String[0]));
        JComboBox<String> assignee = new JComboBox<>(IssueService.USERS.toArray(new String[0]));
        priority.setSelectedItem(existing == null ? "Medium" : existing.getPriority());
        assignee.setSelectedItem(existing == null ? IssueService.USERS.get(0) : existing.getAssignee());
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        Object[][] rows = {{"Title", title}, {"Description", new JScrollPane(description)}, {"Priority", priority}, {"Assignee", assignee}};
        for (int n = 0; n < rows.length; n++) {
            c.gridx = 0; c.gridy = n; c.weightx = 0; fields.add(new JLabel((String) rows[n][0]), c);
            c.gridx = 1; c.weightx = 1; fields.add((Component) rows[n][1], c);
        }
        int result = JOptionPane.showConfirmDialog(this, fields, existing == null ? "Create issue" : "Edit issue #" + existing.getId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        saveAction(() -> {
            if (existing == null) service.create(title.getText(), description.getText(), (String) priority.getSelectedItem(), (String) assignee.getSelectedItem());
            else service.update(existing.getId(), title.getText(), description.getText(), (String) priority.getSelectedItem(), (String) assignee.getSelectedItem());
        });
    }

    private void deleteIssue() {
        Issue i = selected();
        if (JOptionPane.showConfirmDialog(this, "Delete issue #" + i.getId() + "?", "Confirm deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            saveAction(() -> service.delete(i.getId()));
    }
    private void changeStatus() {
        Issue i = selected();
        Object value = JOptionPane.showInputDialog(this, "New status:", "Change status", JOptionPane.QUESTION_MESSAGE, null, IssueService.STATUSES.toArray(), i.getStatus());
        if (value != null) saveAction(() -> service.changeStatus(i.getId(), (String) value));
    }
    private void assign() {
        Issue i = selected();
        Object value = JOptionPane.showInputDialog(this, "Assign to:", "Assign issue", JOptionPane.QUESTION_MESSAGE, null, IssueService.USERS.toArray(), i.getAssignee());
        if (value != null) saveAction(() -> service.assign(i.getId(), (String) value));
    }
}
