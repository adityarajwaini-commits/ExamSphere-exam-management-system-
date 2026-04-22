package com.exam.view;

import com.exam.controller.AdminController;
import com.exam.controller.ExamController;
import com.exam.util.ThemeManager;
import com.exam.util.ThemeToggleButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AdminDashboardView extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            
            float[] dist = {0.0f, 1.0f};

            // Bottom-right: light blue sphere
            java.awt.geom.Point2D center1 = new java.awt.geom.Point2D.Float(w, h);
            Color[] colors1 = {new Color(59, 130, 246, 40), new Color(59, 130, 246, 0)};
            g2.setPaint(new java.awt.RadialGradientPaint(center1, 500f, dist, colors1));
            g2.fillRect(0, 0, w, h);

            // Top-left: soft purple sphere
            java.awt.geom.Point2D center2 = new java.awt.geom.Point2D.Float(0, 0);
            Color[] colors2 = {new Color(139, 92, 246, 30), new Color(139, 92, 246, 0)};
            g2.setPaint(new java.awt.RadialGradientPaint(center2, 450f, dist, colors2));
            g2.fillRect(0, 0, w, h);

            // Mid-right: light grey sphere
            java.awt.geom.Point2D center3 = new java.awt.geom.Point2D.Float(w + 50, h / 2f);
            Color[] colors3 = {new Color(148, 163, 184, 30), new Color(148, 163, 184, 0)};
            g2.setPaint(new java.awt.RadialGradientPaint(center3, 350f, dist, colors3));
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    };
    private final JPanel sidebar = new JPanel(new GridLayout(6, 1, 0, 10)) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, ThemeManager.getSidebar(),
                    0, getHeight(), ThemeManager.getSidebarBottom()));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    };
    private final JLabel titleLabel = new JLabel("Admin Dashboard");
    private final Map<String, SidebarButton> buttons = new LinkedHashMap<>();

    public AdminDashboardView(AdminController adminController, ExamController examController) {
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 10)); // ~0.04 alpha
                g2.fillRect(0, 2, getWidth(), getHeight() - 2);

                // Gradient background
                g2.setPaint(new GradientPaint(0, 0, new Color(248, 250, 252),
                        getWidth(), 0, new Color(238, 242, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight() - 1);
                
                // Bottom border
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Title with tracking
        java.util.Map<java.awt.font.TextAttribute, Object> attributes = new java.util.HashMap<>();
        attributes.put(java.awt.font.TextAttribute.TRACKING, 0.04);
        attributes.put(java.awt.font.TextAttribute.WEIGHT, java.awt.font.TextAttribute.WEIGHT_SEMIBOLD);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f).deriveFont(attributes));
        titleLabel.setForeground(new Color(15, 23, 42));
        
        // Title wrapper with accent line
        JPanel titleWrapper = new JPanel();
        titleWrapper.setLayout(new javax.swing.BoxLayout(titleWrapper, javax.swing.BoxLayout.Y_AXIS));
        titleWrapper.setOpaque(false);
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        titleWrapper.add(titleLabel);
        titleWrapper.add(javax.swing.Box.createVerticalStrut(6));
        
        JPanel accentLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(0, 0, 40, 3, 2, 2);
                g2.dispose();
            }
        };
        accentLine.setOpaque(false);
        accentLine.setMaximumSize(new Dimension(40, 3));
        accentLine.setPreferredSize(new Dimension(40, 3));
        accentLine.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        titleWrapper.add(accentLine);

        topBar.add(titleWrapper, BorderLayout.WEST);
        topBar.add(new ThemeToggleButton(), BorderLayout.EAST);

        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        addSidebarButton("home",        "Home",        "\uD83C\uDFE0");
        addSidebarButton("exams",       "Exams",       "\uD83D\uDCDD");
        addSidebarButton("questions",   "Questions",   "\u2753");
        addSidebarButton("results",     "Results",     "\uD83D\uDCCA");
        addSidebarButton("leaderboard", "Leaderboard", "\uD83C\uDFC6");
        SidebarButton logout = addSidebarButton("logout", "Logout", "\uD83D\uDEAA");
        logout.addActionListener(e -> adminController.goToView("login"));

        contentPanel.add(new AdminHomePanel(adminController), "home");
        contentPanel.add(new ExamManagerPanel(examController), "exams");
        contentPanel.add(new QuestionManagerPanel(examController), "questions");
        contentPanel.add(new ResultsViewerPanel(adminController), "results");
        contentPanel.add(new LeaderboardView(examController, adminController.getNavigation(), null), "leaderboard");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
        setActive("home");
    }

    private SidebarButton addSidebarButton(String key, String text, String emoji) {
        SidebarButton button = new SidebarButton(emoji + "  " + text);
        button.addActionListener(e -> {
            if (!"logout".equals(key)) {
                setActive(key);
            }
        });
        buttons.put(key, button);
        sidebar.add(button);
        return button;
    }

    private void setActive(String key) {
        for (Map.Entry<String, SidebarButton> entry : buttons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }
        cardLayout.show(contentPanel, key);
    }

    private void applyTheme() {
        setBackground(ThemeManager.getBackground());
        sidebar.repaint();
        contentPanel.setBackground(ThemeManager.getBackground());
        // topBar stays white in both modes for clarity
        for (SidebarButton button : buttons.values()) {
            button.applyTheme();
        }
    }

    // ── Flat sidebar nav button ──────────────────────────────────────────────
    private static class SidebarButton extends JButton {
        private boolean active;
        private boolean hover;

        // Active-item colours
        private static final Color ACTIVE_BG   = new Color(37, 99, 235);      // #2563EB
        private static final Color ACTIVE_FG   = Color.WHITE;
        // Default text colour: soft slate #CBD5E1
        private static final Color DEFAULT_FG  = new Color(203, 213, 225);
        // Hover: barely-visible white tint
        private static final Color HOVER_BG    = new Color(255, 255, 255, 13); // ~5% white

        private SidebarButton(String text) {
            super(text);
            setHorizontalAlignment(LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            // Fixed 44-px height; let width fill the GridLayout cell
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(196, 44));
            // padding: 10px top/bottom, 12px left, 8px right
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 8));
            setFont(getFont().deriveFont(Font.PLAIN, 14f));
            setForeground(DEFAULT_FG);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            });
        }

        private void setActive(boolean active) {
            this.active = active;
            setForeground(active ? ACTIVE_FG : DEFAULT_FG);
            setFont(getFont().deriveFont(active ? Font.BOLD : Font.PLAIN, 14f));
            repaint();
        }

        /** Called on theme change – sidebar text is always the same slate colour. */
        private void applyTheme() {
            if (!active) setForeground(DEFAULT_FG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                // Solid blue pill for active item
                g2.setColor(ACTIVE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (hover) {
                // Barely-visible white tint on hover
                g2.setColor(HOVER_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            // Default: nothing drawn — fully transparent against the gradient sidebar

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
