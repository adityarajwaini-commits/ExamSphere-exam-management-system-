package com.exam.view;

import com.exam.controller.ExamController;
import com.exam.model.Exam;
import com.exam.util.SimpleLogger;
import com.exam.util.ThemeManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ExamManagerPanel extends JPanel {
    private final ExamController examController;
    private final JTextField titleField = new JTextField(16);
    private final JTextField durationField = new JTextField(6);
    private final JTextField passField = new JTextField(6);
    private final JTextField totalField = new JTextField(6);
    private final ToggleCheckBox randomizeBox = new ToggleCheckBox("Randomize");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Title", "Duration", "Total", "Pass%", "Random"}, 0);
    private final JTable table = new JTable(model);

    public ExamManagerPanel(ExamController examController) {
        this.examController = examController;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 5, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        form.add(new JLabel("Name"));
        form.add(new JLabel("Duration"));
        form.add(new JLabel("Total Q"));
        form.add(new JLabel("Pass %"));
        form.add(new JLabel("Randomize"));
        form.add(titleField);
        form.add(durationField);
        form.add(totalField);
        form.add(passField);
        form.add(randomizeBox);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::handleRowSelection);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        add(form, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        addButton.addActionListener(e -> createExam());
        updateButton.addActionListener(e -> updateExam());
        deleteButton.addActionListener(e -> deleteExam());

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0);
        try {
            List<Exam> exams = examController.getAllExams();
            if (exams != null) {
                for (Exam exam : exams) {
                    model.addRow(new Object[]{
                            exam.getExamId(),
                            exam.getTitle(),
                            exam.getDurationMinutes(),
                            exam.getTotalQuestions(),
                            exam.getPassPercentage(),
                            exam.isRandomizeQuestions()
                    });
                }
            }
            table.clearSelection();
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Load exams failed: " + ex.getMessage());
        }
    }

    private void createExam() {
        try {
            Exam exam = buildExam(0);
            int id = examController.createExam(exam);
            if (id > 0) {
                SimpleLogger.log("INFO", "Exam created: " + exam.getTitle());
                refreshTable();
                clearForm();
            }
        } catch (NumberFormatException | SQLException ex) {
            SimpleLogger.log("ERROR", "Create exam failed: " + ex.getMessage());
        }
    }

    private void updateExam() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
        try {
            Exam exam = buildExam(id);
            if (examController.updateExam(exam)) {
                SimpleLogger.log("INFO", "Exam updated: " + exam.getTitle());
                refreshTable();
                clearForm();
            }
        } catch (NumberFormatException | SQLException ex) {
            SimpleLogger.log("ERROR", "Update exam failed: " + ex.getMessage());
        }
    }

    private void handleRowSelection(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        titleField.setText(String.valueOf(model.getValueAt(row, 1)));
        durationField.setText(String.valueOf(model.getValueAt(row, 2)));
        totalField.setText(String.valueOf(model.getValueAt(row, 3)));
        passField.setText(String.valueOf(model.getValueAt(row, 4)));
        randomizeBox.setSelected(Boolean.parseBoolean(String.valueOf(model.getValueAt(row, 5))));
    }

    private void deleteExam() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
        try {
            if (examController.deleteExam(id)) {
                SimpleLogger.log("WARN", "Exam deleted: " + id);
                refreshTable();
                clearForm();
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Delete exam failed: " + ex.getMessage());
        }
    }

    private Exam buildExam(int id) {
        String title = titleField.getText().trim();
        int duration = Integer.parseInt(durationField.getText().trim());
        int total = Integer.parseInt(totalField.getText().trim());
        int pass = Integer.parseInt(passField.getText().trim());
        boolean randomize = randomizeBox.isSelected();
        Exam exam = new Exam(title, duration, total, pass, randomize, 1);
        exam.setExamId(id);
        return exam;
    }

    private void clearForm() {
        titleField.setText("");
        durationField.setText("");
        totalField.setText("");
        passField.setText("");
        randomizeBox.setSelected(false);
    }

    private void applyTheme() {
        setBackground(ThemeManager.getBackground());
        table.setBackground(ThemeManager.getCardColor());
        table.setForeground(ThemeManager.getText());
        table.setGridColor(ThemeManager.getBorder());
        
        // Use recursive helper for form components
        for (java.awt.Component c : getComponents()) {
            if (c instanceof JPanel) {
                ThemeManager.applyThemeRecursively(c);
            }
        }
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                setBackground(ThemeManager.getAccent());
                setForeground(ThemeManager.getSidebarText());
            } else {
                setBackground(row % 2 == 0 ? ThemeManager.getCardColor() : ThemeManager.getBackground());
                setForeground(ThemeManager.getText());
            }
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    private static class ToggleCheckBox extends JCheckBox {
        private ToggleCheckBox(String text) {
            super(text);
            setOpaque(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int boxSize = 16;
            int y = (getHeight() - boxSize) / 2;
            g2.setColor(isSelected() ? ThemeManager.getAccent() : ThemeManager.getBorder());
            g2.fillRoundRect(0, y, boxSize, boxSize, 6, 6);
            g2.setColor(ThemeManager.getSidebarText());
            if (isSelected()) {
                g2.drawLine(4, y + 9, 7, y + 12);
                g2.drawLine(7, y + 12, 12, y + 5);
            }
            g2.setColor(ThemeManager.getText());
            g2.drawString(getText(), boxSize + 8, y + 13);
            g2.dispose();
        }
    }
}
