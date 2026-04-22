package com.exam.view;

import com.exam.model.Question;
import com.exam.model.Result;
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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class ResultView extends JPanel {
    private final NavigationHelper navigation;
    private final SolutionReviewView solutionReviewView;
    private Result currentResult;
    private List<Question> questions;
    private Map<Integer, String> answerMap;
    private String examTitle;
    private String studentName;

    // Components
    private final JLabel scoreValueLabel = new JLabel("0 / 0", SwingConstants.CENTER);
    private final JLabel percentageLabel = new JLabel("0%", SwingConstants.CENTER);
    private final JLabel badgeLabel = new JLabel("Good", SwingConstants.CENTER);
    private final TrophyIcon trophyIcon = new TrophyIcon();

    private final StatCard correctCard = new StatCard("Correct Answers", "0", ThemeManager.getSuccess());
    private final StatCard wrongCard = new StatCard("Wrong Answers", "0", ThemeManager.getDanger());
    private final StatCard skippedCard = new StatCard("Skipped", "0", ThemeManager.getWarning());
    private final StatCard timeCard = new StatCard("Time Taken", "0s", ThemeManager.getAccent());

    private final JPanel performanceCard = new JPanel(new BorderLayout());
    private final JLabel performanceTitle = new JLabel("Great effort!");
    private final JLabel performanceSubtitle = new JLabel("You've completed the exam successfully.");

    private final JPanel topBar = new JPanel(new BorderLayout());
    private final JPanel mainCard = new RoundedPanel(20);
    private final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

    public ResultView(NavigationHelper navigation, SolutionReviewView solutionReviewView) {
        this.navigation = navigation;
        this.solutionReviewView = solutionReviewView;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        buildHeader();
        buildMainContent();
        buildButtons();

        add(topBar, BorderLayout.NORTH);
        add(mainCard, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
    }

    private void buildHeader() {
        // Highlighted title panel
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Bottom accent line under "Result Summary"
                g2.setColor(new Color(59, 130, 246)); // #3B82F6
                // Height 2px, Width 60px
                // Positioned roughly under the first label (Result Summary)
                // GridLayout divides height by 2. So first label is in [0, height/2]
                g2.fillRoundRect(0, getHeight()/2 - 2, 60, 2, 2, 2);
                g2.dispose();
            }
        };
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Result Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel subtitle = new JLabel("Detailed performance breakdown");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titlePanel.add(title);
        titlePanel.add(subtitle);

        topBar.setOpaque(true); // Soft background tint
        topBar.setBackground(new Color(248, 250, 252));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(new ThemeToggleButton(), BorderLayout.EAST);
    }

    private void buildMainContent() {
        mainCard.setLayout(new BorderLayout(0, 30));
        mainCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        gridPanel.setOpaque(false);

        // Left Side - Achievement Display
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
        leftSide.setOpaque(false);

        trophyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        trophyIcon.setPreferredSize(new Dimension(60, 70));
        trophyIcon.setMaximumSize(new Dimension(60, 70));

        scoreValueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        scoreValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        percentageLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        percentageLabel.setForeground(ThemeManager.getSuccess());
        percentageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(new Color(230, 249, 240));
        badgeLabel.setForeground(ThemeManager.getSuccess());
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        badgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftSide.add(javax.swing.Box.createVerticalGlue());
        leftSide.add(trophyIcon);
        leftSide.add(javax.swing.Box.createVerticalStrut(15));
        leftSide.add(scoreValueLabel);
        leftSide.add(javax.swing.Box.createVerticalStrut(5));
        leftSide.add(percentageLabel);
        leftSide.add(javax.swing.Box.createVerticalStrut(10));
        leftSide.add(badgeLabel);
        leftSide.add(javax.swing.Box.createVerticalGlue());

        // Right Side - Stats
        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setOpaque(false);

        rightSide.add(correctCard);
        rightSide.add(javax.swing.Box.createVerticalStrut(12));
        rightSide.add(wrongCard);
        rightSide.add(javax.swing.Box.createVerticalStrut(12));
        rightSide.add(skippedCard);
        rightSide.add(javax.swing.Box.createVerticalStrut(12));
        rightSide.add(timeCard);

        gridPanel.add(leftSide);
        gridPanel.add(rightSide);

        // Performance Card
        performanceCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        performanceTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        performanceSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel perfText = new JPanel(new GridLayout(2, 1, 0, 4));
        perfText.setOpaque(false);
        perfText.add(performanceTitle);
        perfText.add(performanceSubtitle);

        JLabel perfTrophy = new JLabel("🏆");
        perfTrophy.setFont(new Font("SansSerif", Font.PLAIN, 32));

        performanceCard.add(perfText, BorderLayout.WEST);
        performanceCard.add(perfTrophy, BorderLayout.EAST);

        mainCard.add(gridPanel, BorderLayout.CENTER);
        mainCard.add(performanceCard, BorderLayout.SOUTH);
    }

    private void buildButtons() {
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton viewSolutions = createStyledButton("View Solutions", ThemeManager.getAccent(), true);
        JButton exportCsv = createStyledButton("Export CSV", ThemeManager.getSuccess(), false);
        JButton backToDashboard = createStyledButton("Back to Dashboard", ThemeManager.getAccent(), false);

        viewSolutions.addActionListener(e -> showSolutions());
        exportCsv.addActionListener(e -> exportResult());
        backToDashboard.addActionListener(e -> navigation.show("studentDashboard"));

        buttonsPanel.add(viewSolutions);
        buttonsPanel.add(exportCsv);
        buttonsPanel.add(backToDashboard);
    }

    private JButton createStyledButton(String text, Color color, boolean outline) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (outline) {
                    g2.setColor(ThemeManager.getCardColor());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                } else {
                    g2.setColor(color.equals(ThemeManager.getSuccess()) ? new Color(220, 252, 231) : new Color(224, 242, 254));
                    if (ThemeManager.isDarkMode()) {
                        g2.setColor(color.darker().darker());
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(color);
        if (ThemeManager.isDarkMode() && !outline) {
            btn.setForeground(Color.WHITE);
        }
        btn.setPreferredSize(new Dimension(180, 42));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void setResult(Result result) {
        if (result == null) return;
        this.currentResult = result;

        int totalMarks = result.getTotalQuestions();
        if (this.questions != null) {
            totalMarks = 0;
            for (Question q : this.questions) {
                totalMarks += q.getMarks();
            }
        } else if (result.getPercentage().doubleValue() > 0 && result.getScore() > 0) {
            totalMarks = (int) Math.round(result.getScore() * 100.0 / result.getPercentage().doubleValue());
        } else if (result.getScore() > 0) {
            totalMarks = result.getScore();
        }

        scoreValueLabel.setText(result.getScore() + " / " + totalMarks);
        percentageLabel.setText(result.getPercentage() + "%");
        correctCard.setValue(String.valueOf(result.getCorrectCount()));
        wrongCard.setValue(String.valueOf(result.getWrongCount()));
        skippedCard.setValue(String.valueOf(result.getSkippedCount()));
        timeCard.setValue(result.getTimeTakenSeconds() + "s");

        double p = result.getPercentage().doubleValue();
        if (p >= 80) {
            badgeLabel.setText("Excellent");
            performanceTitle.setText("Exceptional work!");
        } else if (p >= 60) {
            badgeLabel.setText("Good");
            performanceTitle.setText("Great effort!");
        } else {
            badgeLabel.setText("Keep Practicing");
            performanceTitle.setText("Room for improvement");
        }

        trophyIcon.startAnimation();
    }

    public void setSolutionData(List<Question> questions, Map<Integer, String> answerMap,
                                 String examTitle, String studentName) {
        this.questions = questions;
        this.answerMap = answerMap;
        this.examTitle = examTitle;
        this.studentName = studentName;
    }

    private void showSolutions() {
        if (solutionReviewView == null || questions == null || answerMap == null) return;
        solutionReviewView.setData(questions, answerMap, examTitle, studentName);
        navigation.show("solutionReview");
    }

    private void exportResult() {
        if (currentResult == null) return;
        File file = new File("results_export.csv");
        boolean exists = file.exists();
        try (FileWriter writer = new FileWriter(file, true)) {
            if (!exists) {
                writer.write("attempt_id,user_id,exam_id,score,percentage,correct,wrong,skipped,time_taken_seconds\n");
            }
            writer.write(currentResult.getAttemptId() + "," + currentResult.getUserId() + ","
                    + currentResult.getExamId() + "," + currentResult.getScore() + ","
                    + currentResult.getPercentage() + "," + currentResult.getCorrectCount() + ","
                    + currentResult.getWrongCount() + "," + currentResult.getSkippedCount() + ","
                    + currentResult.getTimeTakenSeconds() + "\n");
            SimpleLogger.log("INFO", "Result exported CSV for attemptId=" + currentResult.getAttemptId());
        } catch (IOException ex) {
            SimpleLogger.log("ERROR", "Result export failed: " + ex.getMessage());
        }
    }

    private void applyTheme() {
        boolean dark = ThemeManager.isDarkMode();
        setBackground(dark ? new Color(15, 23, 42) : new Color(245, 247, 251));
        mainCard.setBackground(ThemeManager.getCardColor());
        performanceCard.setBackground(dark ? new Color(30, 58, 138, 40) : new Color(230, 249, 240));

        topBar.setBackground(dark ? new Color(30, 41, 59) : new Color(248, 250, 252));
        
        Color txt = ThemeManager.getText();
        Color txtSec = ThemeManager.getTextSecondary();

        scoreValueLabel.setForeground(txt);
        performanceTitle.setForeground(txt);
        performanceSubtitle.setForeground(txtSec);

        correctCard.applyTheme();
        wrongCard.applyTheme();
        skippedCard.applyTheme();
        timeCard.applyTheme();

        for (Component c : topBar.getComponents()) {
            if (c instanceof JPanel p) {
                for (Component sub : p.getComponents()) {
                    if (sub instanceof JLabel l) {
                        l.setForeground(sub.getFont().getSize() > 14 ? txt : txtSec);
                    }
                }
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        public RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class StatCard extends JPanel {
        private final String label;
        private String value;
        private final Color color;
        private final JLabel valLabel = new JLabel();
        private final JLabel nameLabel = new JLabel();

        public StatCard(String label, String value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            nameLabel.setText(label);
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

            valLabel.setText(value);
            valLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            valLabel.setForeground(color);

            add(nameLabel, BorderLayout.WEST);
            add(valLabel, BorderLayout.EAST);
        }

        public void setValue(String val) {
            this.value = val;
            valLabel.setText(val);
        }

        public void applyTheme() {
            nameLabel.setForeground(ThemeManager.getTextSecondary());
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ThemeManager.isDarkMode() ? new Color(30, 41, 59) : new Color(249, 250, 251));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class TrophyIcon extends JPanel {
        private double scale = 0.9;
        private Timer timer;

        public TrophyIcon() { setOpaque(false); }

        public void startAnimation() {
            scale = 0.9;
            if (timer != null) timer.stop();
            timer = new Timer(15, e -> {
                scale += 0.01;
                if (scale >= 1.0) { scale = 1.0; timer.stop(); }
                repaint();
            });
            timer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            g2.translate(w/2, h/2);
            g2.scale(scale, scale);
            g2.translate(-w/2, -h/2);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ThemeManager.isDarkMode() ? 0.4f : 0.25f));
            g2.setColor(new Color(250, 204, 21));
            g2.fillOval(5, 5, w-10, h-10);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(250, 204, 21), 0, h, new Color(234, 179, 8)));
            
            int cupW = 30;
            int cupH = 30;
            int startX = (w - cupW) / 2;
            int startY = (h - cupH) / 2 - 5;
            
            int[] cx = {startX + 5, startX + 25, startX + 20, startX + 10};
            int[] cy = {startY + 5, startY + 5, startY + 25, startY + 25};
            g2.fillPolygon(cx, cy, 4);
            g2.fillRoundRect(startX + 2, startY, 26, 12, 10, 10);
            
            g2.fillRect(startX + 13, startY + 25, 4, 10);
            g2.fillRoundRect(startX + 5, startY + 35, 20, 5, 4, 4);
            
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(startX - 2, startY + 2, 10, 15, 90, 180);
            g2.drawArc(startX + 22, startY + 2, 10, 15, 270, 180);

            g2.dispose();
        }
    }
}
