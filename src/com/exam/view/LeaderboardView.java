package com.exam.view;

import com.exam.controller.ExamController;
import com.exam.model.Exam;
import com.exam.model.Result;
import com.exam.util.NavigationHelper;
import com.exam.util.ThemeManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class LeaderboardView extends JPanel {
    private final ExamController examController;
    private final NavigationHelper navigation;
    private final JComboBox<Exam> examDropdown = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable resultsTable;
    private final JLabel emptyStateLabel = new JLabel("No attempts yet for this exam", SwingConstants.CENTER);
    private final JScrollPane tableScroll;
    private final String backTarget;
    private int currentStudentId;

    public LeaderboardView(ExamController examController, NavigationHelper navigation, String backTarget) {
        this.examController = examController;
        this.navigation = navigation;
        this.backTarget = backTarget;

        setLayout(new BorderLayout());
        setOpaque(false);

        String[] columns = {"Rank", "Student", "Score", "Percent", "Time Taken"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        resultsTable = new JTable(tableModel);
        
        tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        styleScrollBar(tableScroll);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildContentPanel(), BorderLayout.CENTER);

        setupTableStyle();
        wireActions();

        ThemeManager.addThemeListener(this::applyTheme);
        applyTheme();
    }

    private void styleScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(148, 163, 184); // #94A3B8 soft grey
                this.trackColor = new Color(241, 245, 249); // matches track to background
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }

            @Override
            protected void paintThumb(Graphics g, javax.swing.JComponent c, java.awt.Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
                g2.dispose();
            }
            
            @Override
            protected void paintTrack(Graphics g, javax.swing.JComponent c, java.awt.Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(24, 32, 16, 32));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        JButton backBtn = new JButton("← Back");
        styleTopButton(backBtn, false);
        if (backTarget != null) {
            backBtn.addActionListener(e -> navigation.show(backTarget));
        } else {
            backBtn.setVisible(false);
        }

        JLabel label = new JLabel("Select Exam:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(30, 41, 59));

        examDropdown.setPreferredSize(new Dimension(240, 38));
        examDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        examDropdown.setRenderer(new ExamListRenderer());

        JButton refreshBtn = new JButton("Refresh");
        styleTopButton(refreshBtn, true);
        refreshBtn.addActionListener(e -> loadExams());

        left.add(backBtn);
        left.add(label);
        left.add(examDropdown);
        left.add(refreshBtn);

        topBar.add(left, BorderLayout.WEST);
        return topBar;
    }

    private JPanel buildContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 32, 32, 32));

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardColor());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ThemeManager.getInputBorder());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        emptyStateLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emptyStateLabel.setForeground(new Color(148, 163, 184));
        emptyStateLabel.setVisible(false);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(tableScroll, BorderLayout.CENTER);
        tableContainer.add(emptyStateLabel, BorderLayout.NORTH);

        card.add(tableContainer, BorderLayout.CENTER);
        content.add(card, BorderLayout.CENTER);

        return content;
    }

    private void setupTableStyle() {
        resultsTable.setRowHeight(50);
        resultsTable.setShowGrid(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 0));
        resultsTable.setSelectionBackground(new Color(241, 245, 249));

        JTableHeader header = resultsTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setReorderingAllowed(false);
        
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new RankRenderer());
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(new StudentCellRenderer());
        resultsTable.getColumnModel().getColumn(2).setCellRenderer(new BadgeRenderer("score"));
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(new BadgeRenderer("percent"));
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(new TimeRenderer());
    }

    private void wireActions() {
        examDropdown.addActionListener(e -> {
            Exam selected = (Exam) examDropdown.getSelectedItem();
            if (selected != null) {
                loadResults(selected.getExamId());
            }
        });
    }

    public void setCurrentStudentId(int id) {
        this.currentStudentId = id;
        loadExams();
    }

    private void loadExams() {
        try {
            List<Exam> exams = examController.getAllExams();
            DefaultComboBoxModel<Exam> model = new DefaultComboBoxModel<>();
            for (Exam e : exams) model.addElement(e);
            examDropdown.setModel(model);
            
            if (model.getSize() > 0) {
                examDropdown.setSelectedIndex(0);
                loadResults(exams.get(0).getExamId());
            } else {
                emptyStateLabel.setText("No leaderboard data available");
                emptyStateLabel.setVisible(true);
                tableScroll.setVisible(false);
            }
            revalidate();
            repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading exams: " + ex.getMessage());
        }
    }

    private void loadResults(int examId) {
        tableModel.setRowCount(0);
        try {
            List<Result> results = examController.getLeaderboard(examId);
            if (results == null || results.isEmpty()) {
                emptyStateLabel.setText("No attempts yet for this exam");
                emptyStateLabel.setVisible(true);
                tableScroll.setVisible(false);
            } else {
                emptyStateLabel.setVisible(false);
                tableScroll.setVisible(true);
                for (int i = 0; i < results.size(); i++) {
                    Result r = results.get(i);
                    tableModel.addRow(new Object[]{
                        i + 1,
                        r.getStudentName() != null ? r.getStudentName() : "Student " + r.getUserId(),
                        r.getScore() + " / " + r.getTotalQuestions(),
                        r.getPercentage() + "%",
                        formatTime(r.getTimeTakenSeconds())
                    });
                }
            }
            revalidate();
            repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading leaderboard data: " + ex.getMessage());
        }
    }

    private String formatTime(int secs) {
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    private void applyTheme() {
        boolean dark = ThemeManager.isDarkMode();
        
        resultsTable.setBackground(ThemeManager.getCardColor());
        resultsTable.setForeground(ThemeManager.getText());
        resultsTable.setSelectionBackground(ThemeManager.getHover());
        
        JTableHeader header = resultsTable.getTableHeader();
        header.setBackground(ThemeManager.getCardColor());
        header.setForeground(ThemeManager.getTextSecondarySoft());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getInputBorder()));
        
        ThemeManager.applyThemeRecursively(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        boolean dark = ThemeManager.isDarkMode();
        if (dark) {
            g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59)));
        } else {
            g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(248, 250, 252), 0, getHeight(), new Color(238, 242, 255)));
        }
        g2.fillRect(0, 0, getWidth(), getHeight());

        int w = getWidth();
        int h = getHeight();
        float[] dist = {0.0f, 1.0f};

        java.awt.geom.Point2D brCenter1 = new java.awt.geom.Point2D.Float(w - 50, h + 50);
        Color[] brColors1 = {new Color(99, 102, 241, 64), new Color(99, 102, 241, 0)};
        g2.setPaint(new java.awt.RadialGradientPaint(brCenter1, 800f, dist, brColors1));
        g2.fillRect(0, 0, w, h);

        java.awt.geom.Point2D brCenter2 = new java.awt.geom.Point2D.Float(w + 50, h + 100);
        Color[] brColors2 = {new Color(59, 130, 246, 64), new Color(59, 130, 246, 0)};
        g2.setPaint(new java.awt.RadialGradientPaint(brCenter2, 600f, dist, brColors2));
        g2.fillRect(0, 0, w, h);

        java.awt.geom.Point2D tlCenter = new java.awt.geom.Point2D.Float(0, 0);
        Color[] tlColors = {new Color(59, 130, 246, 50), new Color(59, 130, 246, 0)};
        g2.setPaint(new java.awt.RadialGradientPaint(tlCenter, 600f, dist, tlColors));
        g2.fillRect(0, 0, w, h);
        
        if (!dark) {
            g2.setColor(new Color(99, 102, 241, 60));
            int startX = w - 180;
            int startY = h / 2 - 80;
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 6; c++) {
                    g2.fillOval(startX + c * 20, startY + r * 20, 4, 4);
                }
            }
            
            startX = 60;
            startY = 80;
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 6; c++) {
                    g2.fillOval(startX + c * 20, startY + r * 20, 4, 4);
                }
            }
        }
        g2.dispose();
    }

    private void styleTopButton(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean hover = btn.getModel().isRollover();
                boolean pressed = btn.getModel().isPressed();
                
                if (primary) {
                    if (pressed) g2.setColor(ThemeManager.getAccentHover().darker());
                    else if (hover) g2.setColor(ThemeManager.getAccentHover());
                    else g2.setColor(ThemeManager.getAccent());
                } else {
                    if (pressed) g2.setColor(ThemeManager.getInputBorder());
                    else if (hover) g2.setColor(ThemeManager.getHover());
                    else g2.setColor(ThemeManager.getCardColor());
                }
                
                g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), 16, 16);
                
                if (!primary) {
                    g2.setColor(ThemeManager.getAccent());
                    g2.drawRoundRect(0, 0, btn.getWidth()-1, btn.getHeight()-1, 16, 16);
                }
                
                g2.dispose();
                super.paint(g, c);
            }
        });
        
        btn.setForeground(primary ? Color.WHITE : ThemeManager.getAccent());
        btn.setPreferredSize(new Dimension(primary ? 100 : 90, 36));
    }

    // --- Custom Renderers ---

    private static class ExamListRenderer extends JLabel implements ListCellRenderer<Exam> {
        public ExamListRenderer() { setOpaque(true); setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); }
        @Override public Component getListCellRendererComponent(javax.swing.JList<? extends Exam> list, Exam value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value != null ? value.getTitle() : "");
            setBackground(isSelected ? new Color(59, 130, 246) : Color.WHITE);
            setForeground(isSelected ? Color.WHITE : new Color(15, 23, 42));
            return this;
        }
    }

    private static class RankRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            int rank = (int) v;
            if (rank <= 3) {
                return new MedalBadge(rank);
            }
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            l.setHorizontalAlignment(CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 14));
            l.setForeground(ThemeManager.isDarkMode() ? new Color(148, 163, 184) : new Color(100, 116, 139));
            return l;
        }
    }

    private static class MedalBadge extends JPanel {
        private final int rank;
        public MedalBadge(int rank) {
            this.rank = rank;
            setOpaque(false);
            setPreferredSize(new Dimension(50, 50));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color bg;
            Color fg;
            if (rank == 1) { bg = new Color(255, 215, 0); fg = new Color(30, 41, 59); }
            else if (rank == 2) { bg = new Color(192, 192, 192); fg = new Color(30, 41, 59); }
            else { bg = new Color(205, 127, 50); fg = Color.WHITE; }

            int size = 28;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillOval(x + 1, y + 2, size, size);

            g2.setColor(bg);
            g2.fillOval(x, y, size, size);
            
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String txt = String.valueOf(rank);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, x + (size - fm.stringWidth(txt)) / 2, y + (size + fm.getAscent() - fm.getDescent()) / 2);
            
            g2.dispose();
        }
    }

    private static class StudentCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
            p.setOpaque(false);
            if (isS) p.setBackground(t.getSelectionBackground());
            
            String name = (String) v;
            String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
            JLabel avatar = new JLabel(initial);
            avatar.setOpaque(true);
            avatar.setBackground(new Color(59, 130, 246));
            avatar.setForeground(Color.WHITE);
            avatar.setPreferredSize(new Dimension(32, 32));
            avatar.setHorizontalAlignment(CENTER);
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JLabel label = new JLabel(name);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(ThemeManager.isDarkMode() ? Color.WHITE : new Color(15, 23, 42));
            
            p.add(avatar);
            p.add(label);
            return p;
        }
    }

    private static class BadgeRenderer extends DefaultTableCellRenderer {
        private final String type;
        public BadgeRenderer(String type) { this.type = type; setHorizontalAlignment(CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            return new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    Color bg = new Color(240, 253, 244);
                    Color fg = new Color(22, 163, 74);
                    
                    String val = v != null ? v.toString() : "";
                    if (val.contains("%")) {
                        try {
                            int p = Integer.parseInt(val.replace("%", ""));
                            if (p < 40) { bg = new Color(254, 242, 242); fg = new Color(220, 38, 38); }
                            else if (p < 75) { bg = new Color(255, 251, 235); fg = new Color(217, 119, 6); }
                        } catch (Exception e) {}
                    }

                    int bw = g2.getFontMetrics().stringWidth(val) + 24;
                    int bh = 30;
                    int bx = (getWidth() - bw) / 2;
                    int by = (getHeight() - bh) / 2;
                    
                    g2.setColor(bg);
                    g2.fillRoundRect(bx, by, bw, bh, 30, 30);
                    g2.setColor(fg);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g2.drawString(val, bx + 12, by + 20);
                    g2.dispose();
                }
            };
        }
    }

    private static class TimeRenderer extends DefaultTableCellRenderer {
        public TimeRenderer() { setHorizontalAlignment(CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            l.setForeground(new Color(100, 116, 139));
            return l;
        }
    }
}
