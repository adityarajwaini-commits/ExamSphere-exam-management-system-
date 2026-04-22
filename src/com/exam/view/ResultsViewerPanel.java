package com.exam.view;

import com.exam.controller.AdminController;
import com.exam.model.Exam;
import com.exam.model.Result;
import com.exam.util.SimpleLogger;
import com.exam.util.ThemeManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ResultsViewerPanel extends JPanel {
    private final AdminController adminController;
    private final JComboBox<Exam> examBox = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Student ID", "Score", "Percent", "Pass/Fail", "Time"}, 0);
    private final JTable table = new JTable(model);
    private final ChartPanel chartPanel = new ChartPanel();

    public ResultsViewerPanel(AdminController adminController) {
        this.adminController = adminController;
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // ── TOP BAR: exam selector + export ──────────────────────────────
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(new Color(248, 250, 252));
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topLeft.setOpaque(false);
        JLabel examLbl = new JLabel("Exam:");
        examLbl.setFont(examLbl.getFont().deriveFont(Font.BOLD, 12f));
        examLbl.setForeground(new Color(51, 65, 85));
        topLeft.add(examLbl);
        topLeft.add(examBox);
        top.add(topLeft, BorderLayout.WEST);

        JButton export = styledBtn("Export Report", new Color(37, 99, 235), Color.WHITE);
        top.add(export, BorderLayout.EAST);

        // ── TABLE CARD (left, 60%) ────────────────────────────────────
        table.setDefaultRenderer(Object.class, new PassFailRenderer());
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 12f));

        // Styled table header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(241, 245, 249));   // #F1F5F9
        header.setForeground(new Color(51, 65, 85));       // #334155
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        header.setPreferredSize(new Dimension(0, 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(226, 232, 240)));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableCard = shadowCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(scroll, BorderLayout.CENTER);

        // ── CHART CARD (right, 40%) ──────────────────────────────────
        JPanel chartCard = shadowCard();
        chartCard.setLayout(new BorderLayout(0, 8));
        JLabel chartTitle = new JLabel("Score Distribution");
        chartTitle.setFont(chartTitle.getFont().deriveFont(Font.BOLD, 13f));
        chartTitle.setForeground(new Color(51, 65, 85));
        chartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);

        // ── SPLIT PANE 60 / 40 ───────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableCard, chartCard);
        split.setResizeWeight(0.60);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(14);
        split.setOpaque(false);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        centerWrapper.add(split, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        examBox.addActionListener(e -> loadResults());
        export.addActionListener(e -> exportReport());

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
        loadExams();
    }

    // ── UI helpers ────────────────────────────────────────────────

    /** White card with a painted drop-shadow. */
    private static JPanel shadowCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 6, 14, 14);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                g2.dispose();
            }
        };
    }

    /** Small rounded button. */
    private static JButton styledBtn(String text, Color bg, Color fg) {
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
        btn.setPreferredSize(new Dimension(120, 30));
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadExams() {
        examBox.removeAllItems();
        try {
            List<Exam> exams = adminController.getAllExams();
            if (exams != null) {
                for (Exam exam : exams) {
                    examBox.addItem(exam);
                }
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Load exams failed: " + ex.getMessage());
        }
    }

    private void loadResults() {
        model.setRowCount(0);
        Exam exam = (Exam) examBox.getSelectedItem();
        if (exam == null) {
            return;
        }
        try {
            List<Result> results = adminController.getResultsByExam(exam.getExamId());
            if (results != null) {
                for (Result r : results) {
                    String status = r.getPercentage().doubleValue() >= exam.getPassPercentage() ? "PASS" : "FAIL";
                    model.addRow(new Object[]{
                            r.getUserId(), r.getScore(), r.getPercentage(), status, r.getTimeTakenSeconds()
                    });
                }
            }
            chartPanel.setResults(results);
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Load results failed: " + ex.getMessage());
        }
    }

    private void exportReport() {
        Exam exam = (Exam) examBox.getSelectedItem();
        if (exam == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Results Report");
        chooser.setSelectedFile(new File("admin_report.txt"));
        int action = chooser.showSaveDialog(this);
        if (action != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, true)) {
            writer.write("Exam: " + exam.getTitle() + "\n");
            for (int i = 0; i < model.getRowCount(); i++) {
                writer.write(model.getValueAt(i, 0) + "," + model.getValueAt(i, 1) + ","
                        + model.getValueAt(i, 2) + "," + model.getValueAt(i, 3) + "\n");
            }
            writer.write("\n");
            SimpleLogger.log("INFO", "Results report exported to: " + file.getAbsolutePath());
        } catch (Exception ex) {
            SimpleLogger.log("ERROR", "Export report failed: " + ex.getMessage());
        }
    }

    private void applyTheme() {
        setBackground(ThemeManager.getBackground());
        table.setForeground(ThemeManager.getText());
        table.setFont(table.getFont());
        chartPanel.setBackground(new Color(248, 250, 252));
    }

    private static class PassFailRenderer extends DefaultTableCellRenderer {
        // Muted green/red — easy on the eye
        private static final Color PASS_BG = new Color(220, 252, 231); // #DCFCE7
        private static final Color PASS_FG = new Color(22, 163, 74);   // #16A34A
        private static final Color FAIL_BG = new Color(254, 226, 226); // #FEE2E2
        private static final Color FAIL_FG = new Color(220, 38, 38);   // #DC2626

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(column == 3 ? CENTER : LEFT);
            if (isSelected) {
                setBackground(ThemeManager.getAccent());
                setForeground(Color.WHITE);
            } else if (column == 3) {
                boolean pass = "PASS".equals(String.valueOf(value));
                setBackground(pass ? PASS_BG : FAIL_BG);
                setForeground(pass ? PASS_FG : FAIL_FG);
                setFont(getFont().deriveFont(Font.BOLD, 11f));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(new Color(30, 41, 59));
                setFont(getFont().deriveFont(Font.PLAIN, 12f));
            }
            return this;
        }
    }

    private static class ChartPanel extends JPanel {
        private List<Result> results = new ArrayList<>();

        private ChartPanel() {
            setBackground(new Color(248, 250, 252));
            setOpaque(true);
        }

        private void setResults(List<Result> results) {
            this.results = results == null ? new ArrayList<>() : results;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth();
            int H = getHeight();
            int padX  = 32;
            int padTop = 12;
            int padBot = 36;

            // Chart background
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, W, H, 12, 12);

            if (results.isEmpty()) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
                g2.setColor(new Color(148, 163, 184));
                String msg = "No results yet";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2);
                g2.dispose();
                return;
            }

            // Buckets: 0-20, 20-40, 40-60, 60-80, 80-100
            int[] buckets = new int[5];
            for (Result r : results) {
                double p = r.getPercentage().doubleValue();
                int idx = (int) Math.min(4, p / 20.0);
                buckets[idx]++;
            }
            int max = 1;
            for (int b : buckets) max = Math.max(max, b);

            int chartH = H - padTop - padBot;
            int slotW  = (W - padX * 2) / 5;
            int barW   = (int) (slotW * 0.55);

            // Horizontal grid lines
            g2.setStroke(new BasicStroke(1f));
            int gridLines = 4;
            for (int l = 0; l <= gridLines; l++) {
                int lineY = padTop + (int) (chartH * (1 - l / (double) gridLines));
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(padX, lineY, W - padX, lineY);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9f));
                g2.setColor(new Color(148, 163, 184));
                int val = (int) (max * l / (double) gridLines);
                g2.drawString(String.valueOf(val), 2, lineY + 4);
            }

            // Bars
            String[] labels = {"0-20", "20-40", "40-60", "60-80", "80-100"};
            for (int i = 0; i < 5; i++) {
                int barH = (int) (chartH * (buckets[i] / (double) max));
                int x = padX + i * slotW + (slotW - barW) / 2;
                int y = padTop + chartH - barH;

                // Soft blue bar  #3B82F6
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(x, y, barW, Math.max(barH, 2), 6, 6);

                // Count above bar
                if (buckets[i] > 0) {
                    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                    g2.setColor(new Color(51, 65, 85));
                    String cnt = String.valueOf(buckets[i]);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(cnt, x + (barW - fm.stringWidth(cnt)) / 2, y - 4);
                }

                // Range label below
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9f));
                g2.setColor(new Color(100, 116, 139));
                FontMetrics fm2 = g2.getFontMetrics();
                int lx = padX + i * slotW + (slotW - fm2.stringWidth(labels[i])) / 2;
                g2.drawString(labels[i], lx, H - padBot + 14);
            }

            // X-axis line
            g2.setColor(new Color(203, 213, 225));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(padX, padTop + chartH, W - padX, padTop + chartH);

            g2.dispose();
        }
    }
}
