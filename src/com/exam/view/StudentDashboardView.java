package com.exam.view;

import com.exam.controller.ExamController;
import com.exam.model.Exam;
import com.exam.util.NavigationHelper;
import com.exam.util.SimpleLogger;
import com.exam.util.ThemeManager;
import com.exam.util.ThemeToggleButton;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.awt.font.TextAttribute;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class StudentDashboardView extends JPanel {
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private final ExamController examController;
    private final NavigationHelper navigation;
    private final InstructionsView instructionsView;
    private final LeaderboardView leaderboardView;

    private final JPanel sidebar;
    private JPanel header;
    private final JPanel content;
    private final JPanel examsGrid;
    private final JScrollPane examsScroll;
    private final JLabel welcomeLabel;
    private final JLabel subtitleLabel;
    private final ProfileBadge profileBadge = new ProfileBadge();
    private final SidebarNavButton dashboardButton;
    private final SidebarNavButton resultsButton;
    private final SidebarNavButton leaderboardButton;
    private final SidebarNavButton logoutButton;
    private final HeaderCard softHeaderCard;

    private int currentUserId;
    private String currentUserName;

    public StudentDashboardView(ExamController examController, NavigationHelper navigation,
                                 InstructionsView instructionsView, LeaderboardView leaderboardView) {
        this.examController = examController;
        this.navigation = navigation;
        this.instructionsView = instructionsView;
        this.leaderboardView = leaderboardView;

        setLayout(new BorderLayout());

        // Sidebar Buttons
        dashboardButton = createSidebarButton("Dashboard", "studentDashboard");
        resultsButton = createSidebarButton("My Results", "resultView");
        leaderboardButton = createSidebarButton("Leaderboard", "leaderboard");
        logoutButton = createSidebarButton("Logout", null);
        logoutButton.addActionListener(e -> handleLogout());

        sidebar = buildSidebar();
        content = new JPanel(new BorderLayout());
        
        examsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        examsScroll = new JScrollPane(examsGrid);
        
        welcomeLabel = new JLabel("Welcome, Student");
        subtitleLabel = new JLabel("Choose an exam and start your attempt");
        softHeaderCard = new HeaderCard("Available exams listed below", true);

        buildHeader();
        buildContent();

        JPanel mainArea = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Base background
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

                // Bottom-right layered sphere
                java.awt.geom.Point2D brCenter1 = new java.awt.geom.Point2D.Float(w - 50, h + 50);
                Color[] brColors1 = {new Color(99, 102, 241, 64), new Color(99, 102, 241, 0)};
                g2.setPaint(new java.awt.RadialGradientPaint(brCenter1, 800f, dist, brColors1));
                g2.fillRect(0, 0, w, h);

                java.awt.geom.Point2D brCenter2 = new java.awt.geom.Point2D.Float(w + 50, h + 100);
                Color[] brColors2 = {new Color(59, 130, 246, 64), new Color(59, 130, 246, 0)};
                g2.setPaint(new java.awt.RadialGradientPaint(brCenter2, 600f, dist, brColors2));
                g2.fillRect(0, 0, w, h);

                // Top-left soft light blue blob
                java.awt.geom.Point2D tlCenter = new java.awt.geom.Point2D.Float(0, 0);
                Color[] tlColors = {new Color(59, 130, 246, 50), new Color(59, 130, 246, 0)};
                g2.setPaint(new java.awt.RadialGradientPaint(tlCenter, 600f, dist, tlColors));
                g2.fillRect(0, 0, w, h);
                
                // Mid-right dot pattern (6 cols x 4 rows)
                if (!dark) {
                    g2.setColor(new Color(99, 102, 241, 60)); // subtle purple/blue dots
                    int startX = w - 180;
                    int startY = h / 2 - 80;
                    for (int r = 0; r < 4; r++) {
                        for (int c = 0; c < 6; c++) {
                            g2.fillOval(startX + c * 20, startY + r * 20, 4, 4);
                        }
                    }
                }
                
                g2.dispose();
            }
        };
        mainArea.setOpaque(false);
        mainArea.add(header, BorderLayout.NORTH);
        mainArea.add(content, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainArea, BorderLayout.CENTER);

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
        setActiveMenu(dashboardButton);
    }

    public void setCurrentStudent(int userId, String name) {
        this.currentUserId = userId;
        this.currentUserName = name;
        if (leaderboardView != null) {
            leaderboardView.setCurrentStudentId(userId);
        }
        if (welcomeLabel != null) {
            String displayName = (name == null || name.trim().isEmpty()) ? "Student" : name.trim();
            welcomeLabel.setText("Welcome, " + displayName.toLowerCase());
            profileBadge.setInitial(displayName.substring(0, 1).toUpperCase());
        }
        setActiveMenu(dashboardButton);
        refreshExams();
    }

    private JPanel buildSidebar() {
        JPanel sidePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 58, 138)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(220, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(32, 16, 32, 16));

        JLabel brand = new JLabel("Exam Portal");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        brand.setIcon(new javax.swing.ImageIcon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillOval(x, y, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.drawString("🎓", x + 6, y + 24);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 42; }
            @Override public int getIconHeight() { return 32; }
        });
        brand.setAlignmentX(LEFT_ALIGNMENT);
        sidePanel.add(brand);
        sidePanel.add(javax.swing.Box.createVerticalStrut(48));

        sidePanel.add(dashboardButton);
        sidePanel.add(javax.swing.Box.createVerticalStrut(12));
        sidePanel.add(resultsButton);
        sidePanel.add(javax.swing.Box.createVerticalStrut(12));
        sidePanel.add(leaderboardButton);
        sidePanel.add(javax.swing.Box.createVerticalGlue());
        sidePanel.add(logoutButton);

        return sidePanel;
    }

    private SidebarNavButton createSidebarButton(String text, String viewName) {
        SidebarNavButton button = new SidebarNavButton(text);
        button.addActionListener(e -> {
            setActiveMenu(button);
            if (viewName != null) {
                navigation.show(viewName);
            }
        });
        return button;
    }

    private void buildHeader() {
        header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean dark = ThemeManager.isDarkMode();
                
                // Subtle Gradient Background
                if (dark) {
                    g2.setColor(new Color(30, 41, 59));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Let the main window background and blobs show through!
                }
                
                // Bottom separator line
                g2.setColor(dark ? new Color(51, 65, 85) : new Color(226, 232, 240)); // #E2E8F0
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setPreferredSize(new Dimension(0, 90));
        header.setBorder(BorderFactory.createEmptyBorder(20, 32, 10, 32));
        header.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246)); // #3B82F6
                // Micro-accent under welcomeLabel
                g2.fillRoundRect(0, welcomeLabel.getHeight() + 2, 40, 2, 2, 2);
                g2.dispose();
            }
        };
        left.setOpaque(false);
        
        // Text Refinement
        welcomeLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 22)); // Slightly increased weight
        welcomeLabel.setForeground(new Color(15, 23, 42)); // #0F172A

        java.util.Map<java.awt.font.TextAttribute, Object> attributes = new java.util.HashMap<>();
        attributes.put(java.awt.font.TextAttribute.TRACKING, 0.05);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14).deriveFont(attributes));
        subtitleLabel.setForeground(new Color(100, 116, 139)); // #64748B

        left.add(welcomeLabel);
        left.add(subtitleLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        right.setOpaque(false);
        right.add(profileBadge);
        right.add(new ThemeToggleButton());

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
    }

    private void buildContent() {
        content.setBorder(BorderFactory.createEmptyBorder(10, 32, 32, 32));
        content.setOpaque(false);

        examsGrid.setOpaque(false);
        examsScroll.setBorder(BorderFactory.createEmptyBorder());
        examsScroll.getVerticalScrollBar().setUnitIncrement(16);
        examsScroll.getViewport().setOpaque(false);
        examsScroll.setOpaque(false);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.add(softHeaderCard, BorderLayout.NORTH);
        contentWrap.add(examsScroll, BorderLayout.CENTER);

        content.add(contentWrap, BorderLayout.CENTER);
    }

    private void refreshExams() {
        examsGrid.removeAll();
        try {
            List<Exam> exams = examController.getAllExams();
            if (exams == null || exams.isEmpty()) {
                JLabel empty = new JLabel("No exams available");
                empty.setForeground(TEXT_MUTED);
                examsGrid.add(empty);
            } else {
                for (Exam exam : exams) {
                    examsGrid.add(buildExamCard(exam));
                }
            }
        } catch (SQLException ex) {
            SimpleLogger.log("ERROR", "Load exams failed: " + ex.getMessage());
        }
        examsGrid.revalidate();
        examsGrid.repaint();
    }

    private JPanel buildExamCard(Exam exam) {
        ExamCard card = new ExamCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(340, 290));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        
        // Circular indicator for items (8px)
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(new Color(59, 130, 246));
        
        JLabel title = new JLabel(exam.getTitle());
        title.setFont(new Font("Segoe UI Black", Font.PLAIN, 19));
        title.setForeground(new Color(15, 23, 42));
        
        top.add(dot);
        top.add(title);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        meta.setOpaque(false);
        meta.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        meta.add(createChip("⏱ " + exam.getDurationMinutes() + " min"));
        meta.add(createChip("❓ " + exam.getTotalQuestions() + " Qs"));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        status.setBackground(new Color(236, 253, 245));
        status.setPreferredSize(new Dimension(0, 38));
        status.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        JLabel check = new JLabel("✔");
        check.setForeground(new Color(22, 163, 74));
        JLabel statusText = new JLabel("Ready to start");
        statusText.setForeground(new Color(22, 163, 74));
        statusText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        status.add(check);
        status.add(statusText);

        JButton startBtn = new PrimaryGradientButton("Start Exam");
        startBtn.addActionListener(e -> handleStartExam(exam));

        card.add(top);
        card.add(javax.swing.Box.createVerticalStrut(18));
        card.add(meta);
        card.add(javax.swing.Box.createVerticalStrut(18));
        card.add(status);
        card.add(javax.swing.Box.createVerticalStrut(18));
        card.add(startBtn);

        return card;
    }

    private JLabel createChip(String text) {
        boolean dark = ThemeManager.isDarkMode();
        return new JLabel(text) {
            @Override public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dark ? new Color(30, 58, 138, 100) : new Color(238, 242, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
            {
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setForeground(dark ? new Color(147, 197, 253) : new Color(37, 99, 235));
                setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            }
        };
    }

    private void applyTheme() {
        boolean dark = ThemeManager.isDarkMode();
        welcomeLabel.setForeground(dark ? new Color(241, 245, 249) : new Color(15, 23, 42)); // #0F172A
        subtitleLabel.setForeground(dark ? new Color(148, 163, 184) : new Color(100, 116, 139)); // #64748B
        
        // Header gradient is handled by custom paintComponent - just trigger repaint
        header.repaint();
        
        for (Component c : sidebar.getComponents()) {
            if (c instanceof SidebarNavButton b) b.applyTheme();
        }
        
        if (softHeaderCard != null) softHeaderCard.applyTheme();
        
        refreshExams();
    }

    private void setActiveMenu(SidebarNavButton active) {
        dashboardButton.setActive(dashboardButton == active);
        resultsButton.setActive(resultsButton == active);
        leaderboardButton.setActive(leaderboardButton == active);
        logoutButton.setActive(logoutButton == active);
    }

    private void handleStartExam(Exam exam) {
        if (currentUserId <= 0) return;
        try {
            int attemptId = examController.startAttempt(currentUserId, exam.getExamId());
            if (attemptId > 0) {
                instructionsView.setExamDetails(exam, attemptId, currentUserId, currentUserName);
                navigation.show("instructions");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Start exam failed: " + ex.getMessage());
        }
    }

    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
            navigation.show("login");
        }
    }

    private static class SidebarNavButton extends JButton {
        private boolean active;
        private boolean hover;
        private double hoverProgress = 0;
        private Timer hoverTimer;

        public SidebarNavButton(String text) {
            super(text);
            setHorizontalAlignment(LEFT);
            setPreferredSize(new Dimension(190, 44));
            setMaximumSize(new Dimension(190, 44));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 16));
            setForeground(new Color(229, 231, 235));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; startHoverAnim(true); }
                @Override public void mouseExited(MouseEvent e) { hover = false; startHoverAnim(false); }
            });
        }

        private void startHoverAnim(boolean entering) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new Timer(16, e -> {
                if (entering) { hoverProgress += 0.2; if (hoverProgress >= 1) { hoverProgress = 1; hoverTimer.stop(); } }
                else { hoverProgress -= 0.2; if (hoverProgress <= 0) { hoverProgress = 0; hoverTimer.stop(); } }
                repaint();
            });
            hoverTimer.start();
        }

        public void setActive(boolean active) { this.active = active; repaint(); }
        public void applyTheme() {}

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            boolean dark = ThemeManager.isDarkMode();
            
            if (active) {
                g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(59, 130, 246, 40), getWidth(), 0, new Color(37, 99, 235, 10)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(2, 10, 3, getHeight() - 20, 2, 2);
            } else if (hoverProgress > 0) {
                int alpha = (int) (hoverProgress * 20);
                g2.setColor(new Color(255, 255, 255, alpha));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }

            // Dot Indicator
            int dotSize = 8;
            double scale = 1.0 + (hoverProgress * 0.2);
            int finalSize = (int) (dotSize * scale);
            int x = 16 - (finalSize / 2) + 4;
            int y = (getHeight() - finalSize) / 2;

            Color dotColor;
            if (active) {
                dotColor = dark ? new Color(96, 165, 250) : new Color(59, 130, 246);
            } else if (hover) {
                dotColor = new Color(96, 165, 250);
            } else {
                dotColor = dark ? new Color(100, 116, 139) : new Color(148, 163, 184);
            }
            
            g2.setColor(dotColor);
            g2.fillOval(x, y, finalSize, finalSize);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class HeaderCard extends JPanel {
        private final JLabel dotLabel;
        private final JLabel textLabel;
        private final boolean isHeading;
        private boolean hover;
        public HeaderCard(String text, boolean isHeading) {
            this.isHeading = isHeading;
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            
            dotLabel = new JLabel("●");
            dotLabel.setFont(new Font("Segoe UI", Font.BOLD, isHeading ? 14 : 12));
            dotLabel.setForeground(isHeading ? new Color(96, 165, 250) : new Color(59, 130, 246));
            
            textLabel = new JLabel(text);
            textLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            
            add(dotLabel);
            add(textLabel);
            
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        public void applyTheme() {
            boolean dark = ThemeManager.isDarkMode();
            textLabel.setForeground(dark ? new Color(241, 245, 249) : new Color(30, 41, 59));
            if (dark) dotLabel.setForeground(new Color(147, 197, 253));
            else dotLabel.setForeground(isHeading ? new Color(96, 165, 250) : new Color(59, 130, 246));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean dark = ThemeManager.isDarkMode();
            
            Color bg;
            if (dark) {
                bg = new Color(30, 41, 59);
            } else {
                bg = hover ? new Color(226, 232, 240) : new Color(241, 245, 249);
            }
            
            if (!dark) {
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 14, 14);
            }
            
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
        }
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, 56);
        }
    }

    private static class ExamCard extends JPanel {
        private double hoverScale = 0;
        private Timer animTimer;

        public ExamCard() {
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { startAnim(true); }
                @Override public void mouseExited(MouseEvent e) { startAnim(false); }
            });
        }

        private void startAnim(boolean entering) {
            if (animTimer != null) animTimer.stop();
            animTimer = new Timer(15, e -> {
                if (entering) { hoverScale += 0.1; if (hoverScale >= 1) { hoverScale = 1; animTimer.stop(); } }
                else { hoverScale -= 0.1; if (hoverScale <= 0) { hoverScale = 0; animTimer.stop(); } }
                repaint();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int yOffset = (int) (hoverScale * -8);
            int w = getWidth() - 10;
            int h = getHeight() - 15;

            if (!ThemeManager.isDarkMode()) {
                g2.setColor(new Color(59, 130, 246, (int) (hoverScale * 25)));
                g2.fillRoundRect(5, 10 + yOffset, w, h, 20, 20);
                g2.setColor(new Color(0, 0, 0, (int) (12 + hoverScale * 10)));
                g2.fillRoundRect(5, 10 + yOffset, w, h, 20, 20);
            }

            g2.setColor(ThemeManager.getCardColor());
            g2.fillRoundRect(0, yOffset, w, h, 20, 20);
            
            if (hoverScale > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (hoverScale * 0.15)));
                g2.setColor(new Color(59, 130, 246));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(0, yOffset, w - 1, h - 1, 20, 20);
            }
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class PrimaryGradientButton extends JButton {
        private double hoverProgress = 0;
        private Timer timer;

        public PrimaryGradientButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(0, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { startAnim(true); }
                @Override public void mouseExited(MouseEvent e) { startAnim(false); }
            });
        }

        private void startAnim(boolean in) {
            if (timer != null) timer.stop();
            timer = new Timer(15, e -> {
                if (in) { hoverProgress += 0.1; if (hoverProgress >= 1) { hoverProgress = 1; timer.stop(); } }
                else { hoverProgress -= 0.1; if (hoverProgress <= 0) { hoverProgress = 0; timer.stop(); } }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int shadowAlpha = (int) (60 + hoverProgress * 40);
            g2.setColor(new Color(59, 130, 246, shadowAlpha));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, getHeight(), getHeight());

            double scale = 1.0 + (hoverProgress * 0.05);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2.translate(centerX, centerY);
            g2.scale(scale, scale);
            g2.translate(-centerX, -centerY);

            g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(59, 130, 246), getWidth(), 0, new Color(37, 99, 235)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int textWidth = fm.stringWidth(text);
            
            int iconSize = 24;
            int gap = 10;
            int totalWidth = iconSize + gap + textWidth;
            int startX = (getWidth() - totalWidth) / 2;
            
            // Draw spherical icon background
            int iconY = (getHeight() - iconSize) / 2;
            g2.setColor(new Color(255, 255, 255, 40)); 
            g2.fillOval(startX, iconY, iconSize, iconSize);
            g2.setColor(new Color(255, 255, 255, 80));
            g2.drawOval(startX, iconY, iconSize, iconSize);
            
            // Draw white play triangle inside the sphere
            g2.setColor(Color.WHITE);
            int ax = startX + 10;
            int ay = iconY + 7;
            int[] xPoints = {ax, ax + 8, ax};
            int[] yPoints = {ay, ay + 5, ay + 10};
            g2.fillPolygon(xPoints, yPoints, 3);
            
            int textX = startX + iconSize + gap;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, textX, textY);
            
            g2.dispose();
        }
    }

    private static class ProfileBadge extends JPanel {
        private String initial = "A";
        private double hoverScale = 0;
        private Timer timer;

        public ProfileBadge() { 
            setPreferredSize(new Dimension(40, 40)); 
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { startAnim(true); }
                @Override public void mouseExited(MouseEvent e) { startAnim(false); }
            });
        }
        
        private void startAnim(boolean in) {
            if (timer != null) timer.stop();
            timer = new Timer(15, e -> {
                if (in) { hoverScale += 0.1; if (hoverScale >= 1) { hoverScale = 1; timer.stop(); } }
                else { hoverScale -= 0.1; if (hoverScale <= 0) { hoverScale = 0; timer.stop(); } }
                repaint();
            });
            timer.start();
        }

        public void setInitial(String i) { this.initial = i; repaint(); }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            double scale = 1.0 + (hoverScale * 0.05);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2.translate(centerX, centerY);
            g2.scale(scale, scale);
            g2.translate(-centerX, -centerY);

            // Soft shadow
            g2.setColor(new Color(0, 0, 0, (int)(255 * 0.08)));
            g2.fillOval(1, 2, getWidth() - 2, getHeight() - 2);
            
            g2.setColor(new Color(59, 130, 246));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, (getWidth()-fm.stringWidth(initial))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            g2.dispose();
        }
    }
}
