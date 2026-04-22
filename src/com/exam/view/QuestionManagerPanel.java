package com.exam.view;

import com.exam.controller.ExamController;
import com.exam.model.Exam;
import com.exam.model.Question;
import com.exam.util.SimpleLogger;
import com.exam.util.ThemeManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class QuestionManagerPanel extends JPanel {
    private final ExamController examController;
    private final JComboBox<Exam> examBox = new JComboBox<>();
    private final JTextArea questionArea = new JTextArea(3, 20);
    private final JTextField optionA = new JTextField(14);
    private final JTextField optionB = new JTextField(14);
    private final JTextField optionC = new JTextField(14);
    private final JTextField optionD = new JTextField(14);
    private final JComboBox<String> correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});
    private final JTextField marksField = new JTextField("1", 4);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Question", "A", "B", "C", "D", "Correct", "Marks"}, 0);
    private final JTable table = new JTable(model);

    public QuestionManagerPanel(ExamController examController) {
        this.examController = examController;
        setLayout(new BorderLayout());

        // ── TOP BAR: Exam selector ───────────────────────────────────────────
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        top.setOpaque(false);
        JLabel examLbl = sectionLabel("Exam:");
        top.add(examLbl);
        top.add(examBox);
        
        JButton refreshBtn = styledButton("Refresh", new Color(100, 116, 139), Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(80, 28));
        refreshBtn.addActionListener(e -> loadExams());
        top.add(Box.createHorizontalStrut(10));
        top.add(refreshBtn);

        // ── SECTION 1: Question ──────────────────────────────────────────────
        JPanel qSection = new JPanel(new BorderLayout(0, 6));
        qSection.setBackground(new Color(248, 250, 252));
        qSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        qSection.add(sectionLabel("Question"), BorderLayout.NORTH);
        questionArea.setRows(4);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        JScrollPane qScroll = new JScrollPane(questionArea);
        qScroll.setPreferredSize(new Dimension(0, 80));
        qScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        qSection.add(qScroll, BorderLayout.CENTER);

        // ── SECTION 2: Options 2×2 grid ─────────────────────────────────────
        JPanel optSection = new JPanel(new BorderLayout(0, 10));
        optSection.setBackground(Color.WHITE);
        optSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        optSection.add(sectionLabel("Options"), BorderLayout.NORTH);

        JPanel optGrid = new JPanel(new GridLayout(2, 2, 14, 10));
        optGrid.setOpaque(false);
        optGrid.add(labeledField("Option A", optionA));
        optGrid.add(labeledField("Option B", optionB));
        optGrid.add(labeledField("Option C", optionC));
        optGrid.add(labeledField("Option D", optionD));
        optSection.add(optGrid, BorderLayout.CENTER);

        // ── SECTION 3: Correct Answer + Marks (one row) ──────────────────────
        JPanel ansSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        ansSection.setOpaque(false);
        ansSection.add(labeledField("Correct Answer", correctBox));
        marksField.setPreferredSize(new Dimension(70, 28));
        ansSection.add(labeledField("Marks", marksField));

        // ── FORM: stack the 3 sections with gaps ─────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(4, 18, 8, 18));
        form.add(qSection);
        form.add(Box.createVerticalStrut(16));
        form.add(optSection);
        form.add(Box.createVerticalStrut(16));
        form.add(ansSection);

        // ── BUTTONS: bottom-right ─────────────────────────────────────────────
        JButton add        = styledButton("Add",        new Color(37, 99, 235),  Color.WHITE);
        JButton edit       = styledButton("Edit",       new Color(16, 185, 129), Color.WHITE);
        JButton delete     = styledButton("Delete",     new Color(239, 68, 68),  Color.WHITE);
        JButton importCsv  = styledButton("Import CSV", new Color(100, 116, 139), Color.WHITE);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.setOpaque(false);
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(importCsv);

        JPanel formAndActions = new JPanel(new BorderLayout());
        formAndActions.setOpaque(false);
        formAndActions.add(form, BorderLayout.CENTER);
        formAndActions.add(actions, BorderLayout.SOUTH);

        // ── TABLE ─────────────────────────────────────────────────────────────
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formAndActions, scroll);
        splitPane.setResizeWeight(0.52);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 2));

        add(top, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        examBox.addActionListener(e -> loadQuestions());
        add.addActionListener(e -> createQuestion());
        edit.addActionListener(e -> updateQuestion());
        delete.addActionListener(e -> deleteQuestion());
        importCsv.addActionListener(e -> importCsv());

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
        loadExams();
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    /** Bold section label in #334155. */
    private static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(new Color(51, 65, 85));  // #334155
        return lbl;
    }

    /** Wraps a component with a bold label above it. */
    private static JPanel labeledField(String labelText, java.awt.Component field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        wrapper.add(sectionLabel(labelText), BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    /** Rounded action button with custom bg/fg. */
    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(100, 32));
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    public void loadExams() {
        examBox.removeAllItems();
        try {
            List<Exam> exams = examController.getAllExams();
            if (exams != null) {
                for (Exam exam : exams) {
                    examBox.addItem(exam);
                }
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Load exams failed: " + ex.getMessage());
        }
    }

    private void loadQuestions() {
        model.setRowCount(0);
        Exam exam = (Exam) examBox.getSelectedItem();
        if (exam == null) {
            return;
        }
        try {
            List<Question> questions = examController.loadQuestions(exam.getExamId(), false);
            if (questions != null) {
                for (Question q : questions) {
                    model.addRow(new Object[]{
                            q.getQuestionId(), q.getQuestionText(), q.getOptionA(), q.getOptionB(),
                            q.getOptionC(), q.getOptionD(), q.getCorrectOption(), q.getMarks()
                    });
                }
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Load questions failed: " + ex.getMessage());
        }
    }

    private void createQuestion() {
        Exam exam = (Exam) examBox.getSelectedItem();
        if (exam == null) {
            return;
        }
        try {
            Question q = buildQuestion(exam.getExamId(), 0);
            int id = examController.createQuestion(q);
            if (id > 0) {
                SimpleLogger.log("INFO", "Question added: " + id);
                loadQuestions();
                clearForm();
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Create question failed: " + ex.getMessage());
        }
    }

    private void updateQuestion() {
        int row = table.getSelectedRow();
        Exam exam = (Exam) examBox.getSelectedItem();
        if (row < 0 || exam == null) {
            return;
        }
        int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
        try {
            Question q = buildQuestion(exam.getExamId(), id);
            if (examController.updateQuestion(q)) {
                SimpleLogger.log("INFO", "Question updated: " + id);
                loadQuestions();
                clearForm();
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Update question failed: " + ex.getMessage());
        }
    }

    private void deleteQuestion() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
        try {
            if (examController.deleteQuestion(id)) {
                SimpleLogger.log("WARN", "Question deleted: " + id);
                loadQuestions();
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Delete question failed: " + ex.getMessage());
        }
    }

    private void importCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Questions CSV");
        chooser.setSelectedFile(new File("questions_import.csv"));
        int action = chooser.showOpenDialog(this);
        if (action != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (file == null || !file.exists() || !file.isFile()) {
            SimpleLogger.log("WARN", "CSV file not found: " + (file == null ? "" : file.getAbsolutePath()));
            return;
        }

        Exam exam = (Exam) examBox.getSelectedItem();
        if (exam == null) {
            return;
        }

        int imported = 0;
        int skipped = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    skipped++;
                    continue;
                }

                if (firstLine && "question".equalsIgnoreCase(parts[0].trim())) {
                    firstLine = false;
                    continue;
                }

                Question q = new Question(exam.getExamId(), parts[0], parts[1], parts[2],
                        parts[3], parts[4], parts[5].trim().toUpperCase(), 1);
                examController.createQuestion(q);
                imported++;
                firstLine = false;
            }
            SimpleLogger.log("INFO", "CSV import completed from: " + file.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Import completed. Added: " + imported + ", Skipped: " + skipped,
                    "CSV Import",
                    JOptionPane.INFORMATION_MESSAGE);
            loadQuestions();
        } catch (Exception ex) {
            SimpleLogger.log("ERROR", "CSV import failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "CSV import failed: " + ex.getMessage(),
                    "CSV Import",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Question buildQuestion(int examId, int questionId) {
        Question q = new Question();
        q.setQuestionId(questionId);
        q.setExamId(examId);
        q.setQuestionText(questionArea.getText().trim());
        q.setOptionA(optionA.getText().trim());
        q.setOptionB(optionB.getText().trim());
        q.setOptionC(optionC.getText().trim());
        q.setOptionD(optionD.getText().trim());
        q.setCorrectOption(String.valueOf(correctBox.getSelectedItem()));
        q.setMarks(Integer.parseInt(marksField.getText().trim()));
        return q;
    }

    private void clearForm() {
        questionArea.setText("");
        optionA.setText("");
        optionB.setText("");
        optionC.setText("");
        optionD.setText("");
        marksField.setText("1");
        correctBox.setSelectedIndex(0);
    }

    private void applyTheme() {
        setBackground(ThemeManager.getBackground());
        table.setBackground(ThemeManager.getCardColor());
        table.setForeground(ThemeManager.getText());
        table.setGridColor(ThemeManager.getBorder());
        questionArea.setBackground(ThemeManager.getCardColor());
        questionArea.setForeground(ThemeManager.getText());
        
        // Use recursive helper for form components
        for (java.awt.Component c : getComponents()) {
            if (c instanceof JPanel || c instanceof javax.swing.JSplitPane) {
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
            return this;
        }
    }
}
