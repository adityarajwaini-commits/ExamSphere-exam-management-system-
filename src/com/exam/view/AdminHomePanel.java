package com.exam.view;

import com.exam.controller.AdminController;
import com.exam.model.Exam;
import com.exam.model.Result;
import com.exam.model.User;
import com.exam.util.SimpleLogger;
import com.exam.util.ThemeManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AdminHomePanel extends JPanel {
    private final AdminController adminController;

    // Each stat card has two labels: [0] = title (small/muted), [1] = value (big/bold)
    private final JLabel[] examsLabels    = { new JLabel("Total Exams"),    new JLabel("0") };
    private final JLabel[] studentsLabels = { new JLabel("Total Students"), new JLabel("0") };
    private final JLabel[] attemptsLabels = { new JLabel("Total Attempts"), new JLabel("0") };
    private final JLabel[] avgLabels      = { new JLabel("Average Score"),  new JLabel("0") };

    private final ChartPanel chartPanel = new ChartPanel();
    private final List<JPanel> statCards = new ArrayList<>();

    public AdminHomePanel(AdminController adminController) {
        this.adminController = adminController;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel c1 = buildCard(examsLabels);
        JPanel c2 = buildCard(studentsLabels);
        JPanel c3 = buildCard(attemptsLabels);
        JPanel c4 = buildCard(avgLabels);

        statCards.add(c1);
        statCards.add(c2);
        statCards.add(c3);
        statCards.add(c4);

        cards.add(c1);
        cards.add(c2);
        cards.add(c3);
        cards.add(c4);

        // Chart section: outer rounded white card
        JPanel chartCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow: 0 4px 12px rgba(0,0,0,0.05)
                g2.setColor(new Color(0, 0, 0, 13));
                g2.fillRoundRect(2, 6, getWidth() - 4, getHeight() - 8, 16, 16);
                // white bg
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 16, 16);
                g2.dispose();
            }
        };
        chartCard.setOpaque(false);
        chartCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartCard.add(chartPanel, BorderLayout.CENTER);

        add(cards, BorderLayout.NORTH);
        add(chartCard, BorderLayout.CENTER);

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
        loadStats();
    }

    /** Builds a stat card with a muted title label and a large bold value label. */
    private JPanel buildCard(JLabel[] pair) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // subtle drop shadow
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 6, 14, 14);
                // white card face
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setPreferredSize(new Dimension(200, 90));

        JLabel titleLbl = pair[0];
        JLabel valueLbl = pair[1];

        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.PLAIN, 11f));
        titleLbl.setForeground(new Color(100, 116, 139));   // #64748B

        valueLbl.setFont(valueLbl.getFont().deriveFont(Font.BOLD, 26f));
        valueLbl.setForeground(new Color(15, 23, 42));      // #0F172A

        card.add(titleLbl);
        card.add(valueLbl);
        return card;
    }

    private void loadStats() {
        try {
            List<Exam> exams = adminController.getAllExams();
            List<User> students = adminController.getAllStudents();
            int totalAttempts = 0;
            int totalScore = 0;
            int totalResults = 0;
            List<ChartItem> items = new ArrayList<>();

            if (exams != null) {
                for (Exam exam : exams) {
                    List<Result> results = adminController.getResultsByExam(exam.getExamId());
                    int count = results == null ? 0 : results.size();
                    totalAttempts += count;
                    if (results != null) {
                        for (Result r : results) {
                            totalScore += r.getScore();
                            totalResults++;
                        }
                    }
                    items.add(new ChartItem(exam.getTitle(), count));
                }
            }

            examsLabels[1].setText(String.valueOf(exams == null ? 0 : exams.size()));
            studentsLabels[1].setText(String.valueOf(students == null ? 0 : students.size()));
            attemptsLabels[1].setText(String.valueOf(totalAttempts));
            avgLabels[1].setText(String.valueOf(totalResults == 0 ? 0 : (totalScore / totalResults)));

            items.sort(Comparator.comparingInt(a -> -a.count));
            chartPanel.setItems(items.subList(0, Math.min(5, items.size())));
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Admin stats load failed: " + ex.getMessage());
        }
    }

    private void applyTheme() {
        setBackground(ThemeManager.getBackground());
        // Value labels stay dark for readability on white cards
        chartPanel.setBackground(new Color(248, 250, 252));
    }

    private static class ChartItem {
        private final String label;
        private final int count;

        private ChartItem(String label, int count) {
            this.label = label;
            this.count = count;
        }
    }

    private static class ChartPanel extends JPanel {
        private List<ChartItem> items = new ArrayList<>();
        private int hoveredIndex = -1;

        private ChartPanel() {
            setBackground(new Color(248, 250, 252));
            setOpaque(false);
            
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    int paddingX = 32;
                    int width = getWidth();
                    int slotWidth = items.isEmpty() ? 0 : (width - paddingX * 2) / items.size();
                    if (slotWidth > 0 && e.getX() >= paddingX && e.getX() <= width - paddingX) {
                        int index = (e.getX() - paddingX) / slotWidth;
                        if (index != hoveredIndex && index >= 0 && index < items.size()) {
                            hoveredIndex = index;
                            repaint();
                        }
                    } else if (hoveredIndex != -1) {
                        hoveredIndex = -1;
                        repaint();
                    }
                }
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (hoveredIndex != -1) {
                        hoveredIndex = -1;
                        repaint();
                    }
                }
            });
        }

        private void setItems(List<ChartItem> items) {
            this.items = items;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw rounded bg: #F8FAFC, 10px radius
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            int width = getWidth();
            int height = getHeight();
            int paddingX = 32;
            int paddingTop = 40;
            int paddingBottom = 36;

            // Section title
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
            g2.setColor(new Color(51, 65, 85));   // #334155
            g2.drawString("Top 5 Exams (Attempts)", paddingX, 24);

            if (items.isEmpty()) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
                g2.setColor(new Color(148, 163, 184));
                g2.drawString("No data available", paddingX, height / 2);
                g2.dispose();
                return;
            }

            int chartH = height - paddingTop - paddingBottom;
            int slotWidth = items.isEmpty() ? 0 : (width - paddingX * 2) / items.size();
            int barWidth = Math.min(32, slotWidth / 2); // thin bars
            int max = Math.max(1, items.stream().mapToInt(i -> i.count).max().orElse(1));

            // Subtle horizontal grid lines (#E2E8F0, thin)
            g2.setStroke(new BasicStroke(1f));
            int gridLines = 4;
            for (int l = 0; l <= gridLines; l++) {
                int lineY = paddingTop + (int) (chartH * (1 - l / (double) gridLines));
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(paddingX, lineY, width - paddingX, lineY);
                // value label (#94A3B8, small font)
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
                g2.setColor(new Color(148, 163, 184));
                int val = (int) (max * l / (double) gridLines);
                g2.drawString(String.valueOf(val), 6, lineY + 4);
            }

            for (int i = 0; i < items.size(); i++) {
                ChartItem item = items.get(i);
                int barH = (int) (chartH * (item.count / (double) max));
                int x = paddingX + i * slotWidth + (slotWidth - barWidth) / 2;
                int y = paddingTop + chartH - barH;

                // Bar fill: gradient #60A5FA -> #3B82F6
                // If hovered, slightly brighter
                Color topColor = new Color(96, 165, 250); // #60A5FA
                Color botColor = new Color(59, 130, 246); // #3B82F6
                if (i == hoveredIndex) {
                    topColor = new Color(147, 197, 253); // #93C5FD (brighter)
                    botColor = new Color(96, 165, 250); // #60A5FA
                }
                g2.setPaint(new java.awt.GradientPaint(x, y, topColor, x, y + barH, botColor));
                
                // border-radius 6px (rounded top)
                g2.fillRoundRect(x, y, barWidth, barH, 6, 6);
                if (barH > 6) {
                    g2.fillRect(x, y + barH - 3, barWidth, 3);
                }

                // Count above bar (#1E293B, font-weight 600)
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
                g2.setColor(new Color(30, 41, 59));
                String countStr = String.valueOf(item.count);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(countStr, x + (barWidth - fm.stringWidth(countStr)) / 2, y - 6);

                // Label below (#64748B)
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
                g2.setColor(new Color(100, 116, 139));
                String shortLabel = item.label.length() > 10 ? item.label.substring(0, 10) + "…" : item.label;
                FontMetrics fm2 = g2.getFontMetrics();
                int lx = paddingX + i * slotWidth + (slotWidth - fm2.stringWidth(shortLabel)) / 2;
                g2.drawString(shortLabel, lx, height - paddingBottom + 16);
            }

            g2.dispose();
        }
    }
}
