package com.exam.view;

import com.exam.controller.ExamController;
import com.exam.model.Exam;
import com.exam.util.NavigationHelper;
import com.exam.util.ThemeManager;
import com.exam.util.ThemeToggleButton;
import java.awt.AlphaComposite;
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class InstructionsView extends JPanel {
    private static final int CONTENT_WIDTH = 560;

    private final NavigationHelper navigation;
    private final ExamView examView;
    private int examId;
    private int attemptId;
    private int userId;
    private String studentName;
    private Exam exam;

    private final JPanel topBar = new JPanel(new BorderLayout());
    private final JPanel container = new JPanel();
    private final FadeCard card = new FadeCard();

    private final JPanel iconPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ThemeManager.isDarkMode() ? new Color(30, 58, 138) : new Color(239, 246, 255));
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(ThemeManager.getAccent());
            int w = 18;
            int h = 22;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawRoundRect(x, y, w, h, 4, 4);
            g2.drawLine(x + 5, y + 6, x + 13, y + 6);
            g2.drawLine(x + 5, y + 11, x + 13, y + 11);
            g2.drawLine(x + 5, y + 16, x + 9, y + 16);
            g2.dispose();
        }
    };
    private final JLabel titleLabel = new JLabel("Exam: -", SwingConstants.CENTER);
    private final JLabel durationBadge = new JLabel("Duration: -", SwingConstants.CENTER);
    private final JLabel questionsBadge = new JLabel("Total Questions: -", SwingConstants.CENTER);
    private final RoundedPanel durationPanel = new RoundedPanel(20);
    private final RoundedPanel questionsPanel = new RoundedPanel(20);

    private final JLabel sectionLabel = new JLabel("Instructions", SwingConstants.CENTER);
    private final JPanel sectionDivider = new JPanel();
    private final JPanel sectionAccent = new JPanel();

    private final JPanel instructionsContainer = new JPanel();
    private final List<InstructionRow> instructionRows = new ArrayList<>();

    private final JCheckBox confirmBox = new JCheckBox("I have read and understood the instructions");
    private final JButton backButton = new ActionButton("Back", false);
    private final JButton startButton = new ActionButton("Start Exam  ", true);

    private final RoundedPanel footerBar = new RoundedPanel(14);
    private final JLabel footerLeft = new JLabel(
            "Ensure a stable internet connection and avoid interruptions during the exam.");
    private final JLabel footerRight = new JLabel("All the best!");

    private final Timer fadeTimer;

    public InstructionsView(ExamController examController, NavigationHelper navigation, ExamView examView) {
        this.navigation = navigation;
        this.examView = examView;

        setLayout(new BorderLayout());

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);

        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 0, 16));
        topBar.add(new ThemeToggleButton(), BorderLayout.EAST);

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setAlignmentX(CENTER_ALIGNMENT);
        container.setAlignmentY(Component.TOP_ALIGNMENT);
        container.add(Box.createVerticalStrut(40));

        card.setLayout(new BorderLayout());

        JPanel cardContent = new JPanel();
        cardContent.setOpaque(false);
        cardContent.setLayout(new BoxLayout(cardContent, BoxLayout.Y_AXIS));
        cardContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        iconPanel.setPreferredSize(new Dimension(50, 50));
        iconPanel.setMaximumSize(new Dimension(50, 50));
        iconPanel.setAlignmentX(CENTER_ALIGNMENT);
        iconPanel.setOpaque(false);

        titleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(CENTER_ALIGNMENT);
        
        durationBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        durationPanel.setLayout(new BorderLayout());
        durationPanel.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        durationPanel.add(durationBadge, BorderLayout.CENTER);
        
        questionsBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        questionsPanel.setLayout(new BorderLayout());
        questionsPanel.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        questionsPanel.add(questionsBadge, BorderLayout.CENTER);
        
        badgeRow.add(durationPanel);
        badgeRow.add(questionsPanel);

        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionLabel.setAlignmentX(CENTER_ALIGNMENT);
        sectionDivider.setPreferredSize(new Dimension(540, 1));
        sectionDivider.setMaximumSize(new Dimension(CONTENT_WIDTH, 1));
        sectionDivider.setAlignmentX(CENTER_ALIGNMENT);
        sectionAccent.setPreferredSize(new Dimension(62, 3));
        sectionAccent.setMaximumSize(new Dimension(62, 3));
        sectionAccent.setAlignmentX(CENTER_ALIGNMENT);

        instructionsContainer.setOpaque(false);
        instructionsContainer.setLayout(new BoxLayout(instructionsContainer, BoxLayout.Y_AXIS));
        instructionsContainer.setAlignmentX(CENTER_ALIGNMENT);
        addInstructionRow("●", "Do not refresh or close the exam window", "✓");
        addInstructionRow("●", "Each question carries equal marks", "✓");
        addInstructionRow("●", "Use Next and Previous to navigate questions", "✓");
        addInstructionRow("●", "Exam auto-submits when time ends", "✓");
        instructionsContainer.setMaximumSize(new Dimension(CONTENT_WIDTH, instructionsContainer.getPreferredSize().height));

        confirmBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        confirmBox.setAlignmentX(CENTER_ALIGNMENT);
        confirmBox.setFocusPainted(false);
        confirmBox.setOpaque(false);

        JPanel checkboxRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkboxRow.setOpaque(false);
        checkboxRow.setAlignmentX(CENTER_ALIGNMENT);
        checkboxRow.setMaximumSize(new Dimension(CONTENT_WIDTH, 28));
        checkboxRow.setPreferredSize(new Dimension(CONTENT_WIDTH, 28));
        checkboxRow.add(confirmBox);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(CENTER_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(CONTENT_WIDTH, 50));
        buttonRow.setPreferredSize(new Dimension(CONTENT_WIDTH, 50));
        buttonRow.add(backButton);
        buttonRow.add(startButton);

        footerBar.setLayout(new BorderLayout(10, 0));
        footerBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        footerBar.setAlignmentX(CENTER_ALIGNMENT);
        footerBar.setMaximumSize(new Dimension(CONTENT_WIDTH, 48));
        footerBar.setPreferredSize(new Dimension(CONTENT_WIDTH, 48));
        
        JPanel shieldIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getAccent());
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawPolygon(new int[]{6, 12, 18, 18, 12, 6}, new int[]{6, 4, 6, 14, 18, 14}, 6);
                g2.drawLine(9, 11, 11, 13);
                g2.drawLine(11, 13, 15, 8);
                g2.dispose();
            }
        };
        shieldIcon.setPreferredSize(new Dimension(24, 24));
        shieldIcon.setOpaque(false);
        
        footerLeft.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerRight.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JPanel footerContent = new JPanel(new BorderLayout());
        footerContent.setOpaque(false);
        footerContent.add(shieldIcon, BorderLayout.WEST);
        footerContent.add(footerLeft, BorderLayout.CENTER);
        
        footerBar.add(footerContent, BorderLayout.CENTER);
        footerBar.add(footerRight, BorderLayout.EAST);

        cardContent.add(iconPanel);
        cardContent.add(Box.createVerticalStrut(8));
        cardContent.add(titleLabel);
        cardContent.add(Box.createVerticalStrut(16));
        cardContent.add(badgeRow);
        cardContent.add(Box.createVerticalStrut(15));
        cardContent.add(sectionLabel);
        cardContent.add(Box.createVerticalStrut(8));
        cardContent.add(sectionDivider);
        cardContent.add(Box.createVerticalStrut(6));
        cardContent.add(sectionAccent);
        cardContent.add(Box.createVerticalStrut(15));
        cardContent.add(instructionsContainer);
        cardContent.add(Box.createVerticalStrut(20));
        cardContent.add(checkboxRow);
        cardContent.add(Box.createVerticalStrut(15));
        cardContent.add(buttonRow);
        cardContent.add(Box.createVerticalStrut(18));
        cardContent.add(footerBar);

        card.add(cardContent, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));
        card.setAlignmentX(CENTER_ALIGNMENT);
        container.add(card);
        container.add(Box.createVerticalGlue());

        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> navigation.show("studentDashboard"));

        startButton.setPreferredSize(new Dimension(150, 40));
        startButton.setEnabled(false);
        startButton.setText("Start Exam \u2192");
        startButton.addActionListener(e -> startExam());
        confirmBox.addActionListener(e -> startButton.setEnabled(confirmBox.isSelected()));

        add(topBar, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintThumb(Graphics g, javax.swing.JComponent c, java.awt.Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.isDarkMode() ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 30));
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, javax.swing.JComponent c, java.awt.Rectangle trackBounds) {
                // leave track transparent
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scrollPane.getVerticalScrollBar().setOpaque(false);

        topBar.setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        fadeTimer = new Timer(25, e -> {
            float next = Math.min(1f, card.getAlpha() + 0.08f);
            card.setAlpha(next);
            if (next >= 1f) {
                ((Timer) e.getSource()).stop();
            }
        });

        applyTheme();
        startFadeIn();
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

    private void addInstructionRow(String leftIcon, String text, String rightIcon) {
        InstructionRow row = new InstructionRow(leftIcon, text, rightIcon);
        row.setAlignmentX(CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(CONTENT_WIDTH, 52));
        row.setPreferredSize(new Dimension(CONTENT_WIDTH, 52));
        row.setMinimumSize(new Dimension(CONTENT_WIDTH, 52));
        instructionRows.add(row);
        instructionsContainer.add(row);
        instructionsContainer.add(Box.createVerticalStrut(12));
    }

    private void startFadeIn() {
        card.setAlpha(0f);
        if (fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        fadeTimer.start();
    }

    public void setExamDetails(Exam exam, int attemptId, int userId, String studentName) {
        if (exam != null) {
            this.exam = exam;
            this.examId = exam.getExamId();
            this.attemptId = attemptId;
            this.userId = userId;
            this.studentName = studentName;
            titleLabel.setText("Exam: " + exam.getTitle());
            durationBadge.setText("🕒 Duration: " + exam.getDurationMinutes() + " minutes");
            questionsBadge.setText("📋 Total Questions: " + exam.getTotalQuestions());
        }
        confirmBox.setSelected(false);
        startButton.setEnabled(false);
        startFadeIn();
    }

    private void startExam() {
        if (examView != null && exam != null) {
            examView.startExam(exam, attemptId, userId, studentName);
        }
        navigation.show("examView");
    }

    private void applyTheme() {
        setBackground(ThemeManager.getPageBackground());
        topBar.setBackground(ThemeManager.getPageBackground());
        container.setBackground(ThemeManager.getPageBackground());
        
        ThemeManager.applyThemeRecursively(this);

        card.setCardBackground(ThemeManager.getCardColor());
        card.setCardBorder(ThemeManager.getInputBorder());

        titleLabel.setForeground(ThemeManager.getText());
        sectionLabel.setForeground(ThemeManager.getTextSecondarySoft());
        sectionDivider.setBackground(ThemeManager.getInputBorder());
        sectionAccent.setBackground(ThemeManager.getAccent());

        Color badgeBg = ThemeManager.isDarkMode() ? new Color(30, 58, 138) : new Color(238, 242, 255);
        durationPanel.setPanelColor(badgeBg);
        durationPanel.setPanelBorder(badgeBg);
        questionsPanel.setPanelColor(badgeBg);
        questionsPanel.setPanelBorder(badgeBg);
        
        durationBadge.setForeground(ThemeManager.isDarkMode() ? ThemeManager.getSidebarText() : ThemeManager.getAccent());
        questionsBadge.setForeground(ThemeManager.isDarkMode() ? ThemeManager.getSidebarText() : ThemeManager.getAccent());

        for (InstructionRow row : instructionRows) {
            row.applyTheme();
        }

        confirmBox.setBackground(ThemeManager.getCardColor());
        confirmBox.setForeground(ThemeManager.getText());

        backButton.setForeground(ThemeManager.getAccent());
        backButton.setBackground(ThemeManager.getCardColor());
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        startButton.setForeground(ThemeManager.getSidebarText());
        startButton.setBackground(ThemeManager.getAccent());
        startButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        footerBar.setPanelColor(ThemeManager.isDarkMode() ? new Color(30, 58, 138) : new Color(238, 242, 255));
        footerLeft.setForeground(ThemeManager.isDarkMode() ? ThemeManager.getSidebarText() : ThemeManager.getTextSecondary());
        footerRight.setForeground(ThemeManager.isDarkMode() ? ThemeManager.getSidebarText() : ThemeManager.getAccent());
    }

    public int getExamId() {
        return examId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    private static class InstructionRow extends RoundedPanel {
        private final JLabel leftLabel;
        private final JLabel textLabel;
        private final JLabel rightLabel;
        private Color baseColor;
        private Color hoverColor;
        private boolean hover;

        private InstructionRow(String leftIcon, String text, String rightIcon) {
            super(12);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            leftLabel = new JLabel(leftIcon);
            leftLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
            leftLabel.setPreferredSize(new Dimension(24, 24));

            textLabel = new JLabel(text);
            textLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            textLabel.setHorizontalAlignment(SwingConstants.LEFT);
            textLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            rightLabel = new JLabel(rightIcon);
            rightLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rightLabel.setPreferredSize(new Dimension(24, 24));

            add(leftLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
            add(rightLabel, BorderLayout.EAST);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    applyColors();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    applyColors();
                }
            });
        }

        private void applyTheme() {
            baseColor = ThemeManager.isDarkMode() ? new Color(30, 41, 59) : new Color(249, 250, 251);
            hoverColor = ThemeManager.isDarkMode() ? new Color(45, 57, 74) : new Color(241, 245, 249);
            applyColors();
            setPanelBorder(ThemeManager.getInputBorder());
            leftLabel.setForeground(ThemeManager.getAccent());
            textLabel.setForeground(ThemeManager.getText());
            rightLabel.setForeground(ThemeManager.getSuccess());
        }

        private void applyColors() {
            setPanelColor(hover ? hoverColor : baseColor);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color panelColor = Color.WHITE;
        private Color panelBorder = new Color(229, 231, 235);

        private RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        protected void setPanelColor(Color color) {
            this.panelColor = color;
            repaint();
        }

        protected void setPanelBorder(Color color) {
            this.panelBorder = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(panelColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(panelBorder);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class FadeCard extends RoundedPanel {
        private float alpha = 1f;

        private FadeCard() {
            super(20);
        }

        private float getAlpha() {
            return alpha;
        }

        private void setAlpha(float alpha) {
            this.alpha = alpha;
            repaint();
        }

        private void setCardBackground(Color color) {
            setPanelColor(color);
        }

        private void setCardBorder(Color color) {
            setPanelBorder(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private static class ActionButton extends JButton {
        private final boolean primary;
        private boolean hover;

        private ActionButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (primary) {
                Color bg = hover && isEnabled() ? ThemeManager.getAccentHover() : ThemeManager.getAccent();
                if (!isEnabled()) {
                    bg = ThemeManager.getMuted();
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            } else {
                g2.setColor(hover ? ThemeManager.getHover() : ThemeManager.getCardColor());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(ThemeManager.getAccent());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
            g2.dispose();

            g.setColor(isEnabled() ? getForeground() : ThemeManager.getTextSecondarySoft());
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(getText(), x, y);
        }
    }
}
