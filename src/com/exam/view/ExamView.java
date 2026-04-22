package com.exam.view;

import com.exam.controller.ExamController;
import com.exam.model.Answer;
import com.exam.model.Exam;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class ExamView extends JPanel {
    private final ExamController examController;
    private final NavigationHelper navigation;
    private final ResultView resultView;

    private Exam exam;
    private int attemptId;
    private int userId;
    private String studentName;

    // Header Components
    private final JLabel examNameLabel = new JLabel("Exam");
    private final JLabel studentNameLabel = new JLabel("Student");
    private final ProfileBadge studentAvatar = new ProfileBadge();
    private final JLabel timerLabel = new JLabel("00:00:00");
    private final JButton submitButton = new PrimaryGradientButton("Submit Exam");
    private final ProgressBar progressBar = new ProgressBar();
    private final JLabel progressPercentLabel = new JLabel("0% Attempted");

    // Center Components
    private final JLabel questionIndexLabel = new JLabel("Q 1 / 1");
    private final JTextArea questionTextArea = new JTextArea();
    private final JPanel optionsContainer = new JPanel();
    private final ButtonGroup optionGroup = new ButtonGroup();
    private final List<OptionCard> optionCards = new ArrayList<>();

    // Palette Components
    private final JPanel paletteGrid = new JPanel(new GridLayout(0, 4, 10, 10));
    private final List<PaletteButton> paletteButtons = new ArrayList<>();
    private final JPanel legendPanel = new JPanel(new GridLayout(2, 2, 0, 8));

    // Bottom Action Components
    private final JButton markReviewBtn = new SecondaryButton("Mark for Review", false);
    private final JButton prevBtn = new SecondaryButton("Previous", false);
    private final JButton clearBtn = new SecondaryButton("Clear Response", true);
    private final JButton saveNextBtn = new PrimaryGradientButton("Save & Next");

    private final List<Question> questions = new ArrayList<>();
    private final List<QuestionState> states = new ArrayList<>();

    private int currentIndex = 0;
    private int remainingSeconds;
    private int totalSeconds;
    private Timer examTimer;

    public ExamView(ExamController examController, NavigationHelper navigation, ResultView resultView) {
        this.examController = examController;
        this.navigation = navigation;
        this.resultView = resultView;
        
        setLayout(new BorderLayout());
        setOpaque(true);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildLeftPalette(), BorderLayout.WEST);
        add(buildCenterArea(), BorderLayout.CENTER);

        wireActions();
        
        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
    }

    private JPanel buildHeader() {
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 32, 10, 32));

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        examNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0; gbc.weightx = 1.0;
        header.add(examNameLabel, gbc);

        JPanel studentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        studentPanel.setOpaque(false);
        studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        studentPanel.add(studentAvatar);
        studentPanel.add(studentNameLabel);
        
        gbc.gridx = 1; gbc.weightx = 0.5;
        header.add(studentPanel, gbc);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);
        
        JPanel timerBox = new RoundedPanel(12);
        timerBox.setBackground(Color.WHITE);
        timerBox.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JLabel timerIcon = new JLabel("🕒");
        timerIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setForeground(new Color(239, 68, 68));
        timerBox.add(timerIcon);
        timerBox.add(timerLabel);
        
        right.add(timerBox);
        right.add(new ThemeToggleButton());
        submitButton.setPreferredSize(new Dimension(140, 42));
        right.add(submitButton);

        gbc.gridx = 2; gbc.weightx = 1.0;
        header.add(right, gbc);

        JPanel progressPanel = new JPanel(new BorderLayout(15, 0));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPercentLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        progressPercentLabel.setForeground(new Color(59, 130, 246));
        progressPanel.add(progressPercentLabel, BorderLayout.EAST);

        headerWrapper.add(header, BorderLayout.NORTH);
        headerWrapper.add(progressPanel, BorderLayout.SOUTH);
        
        return headerWrapper;
    }

    private JPanel buildLeftPalette() {
        JPanel paletteContainer = new JPanel(new BorderLayout());
        paletteContainer.setOpaque(false);
        paletteContainer.setPreferredSize(new Dimension(300, 0));
        paletteContainer.setBorder(BorderFactory.createEmptyBorder(10, 32, 20, 10));

        JPanel card = new RoundedPanel(16);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Question Palette");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setIcon(new javax.swing.ImageIcon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillOval(x, y, 6, 6); g2.fillOval(x+8, y, 6, 6);
                g2.fillOval(x, y+8, 6, 6); g2.fillOval(x+8, y+8, 6, 6);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }
        });
        title.setIconTextGap(10);
        card.add(title, BorderLayout.NORTH);

        paletteGrid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(paletteGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        card.add(legendPanel, BorderLayout.SOUTH);

        paletteContainer.add(card, BorderLayout.CENTER);
        return paletteContainer;
    }

    private void updateLegend() {
        legendPanel.removeAll();
        
        // Row 1
        legendPanel.add(createLegendLabel(new Color(148, 163, 184), "Not Visited")); // #94A3B8
        legendPanel.add(createLegendLabel(new Color(34, 197, 94), "Answered"));    // #22C55E
        
        // Row 2
        legendPanel.add(createLegendLabel(new Color(239, 68, 68), "Not Answered")); // #EF4444
        legendPanel.add(createLegendLabel(new Color(59, 130, 246), "Marked"));      // #3B82F6
        
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private JLabel createLegendLabel(Color color, String text) {
        JLabel label = new JLabel("● " + text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(color);
        return label;
    }

    private JPanel buildCenterArea() {
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setOpaque(false);
        centerArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 32));

        JPanel questionCard = new RoundedPanel(16);
        questionCard.setBackground(Color.WHITE);
        questionCard.setLayout(new BorderLayout());
        questionCard.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JPanel qHeader = new JPanel(new BorderLayout(0, 15));
        qHeader.setOpaque(false);
        questionIndexLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        questionIndexLabel.setForeground(new Color(59, 130, 246));
        
        questionTextArea.setFont(new Font("Segoe UI", Font.BOLD, 19));
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setEditable(false);
        questionTextArea.setOpaque(false);
        questionTextArea.setFocusable(false);
        
        qHeader.add(questionIndexLabel, BorderLayout.NORTH);
        qHeader.add(questionTextArea, BorderLayout.CENTER);
        questionCard.add(qHeader, BorderLayout.NORTH);

        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.Y_AXIS));
        optionsContainer.setOpaque(false);
        optionsContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        questionCard.add(optionsContainer, BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);
        actionBar.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        markReviewBtn.setPreferredSize(new Dimension(170, 44));
        actionBar.add(markReviewBtn, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);
        prevBtn.setPreferredSize(new Dimension(120, 44));
        clearBtn.setPreferredSize(new Dimension(170, 44));
        saveNextBtn.setPreferredSize(new Dimension(170, 44));
        rightActions.add(prevBtn);
        rightActions.add(clearBtn);
        rightActions.add(saveNextBtn);
        actionBar.add(rightActions, BorderLayout.EAST);

        centerArea.add(questionCard, BorderLayout.CENTER);
        centerArea.add(actionBar, BorderLayout.SOUTH);

        return centerArea;
    }

    private void wireActions() {
        saveNextBtn.addActionListener(e -> saveAndNext());
        prevBtn.addActionListener(e -> showPrevious());
        clearBtn.addActionListener(e -> clearResponse());
        markReviewBtn.addActionListener(e -> toggleMarkReview());
        submitButton.addActionListener(e -> submitWithConfirmation());
    }

    public void startExam(Exam exam, int attemptId, int userId, String studentName) {
        this.exam = exam;
        this.attemptId = attemptId;
        this.userId = userId;
        this.studentName = studentName;
        
        examNameLabel.setText(exam.getTitle());
        studentNameLabel.setText(studentName);
        studentAvatar.setInitial(studentName != null ? studentName.substring(0, 1).toUpperCase() : "S");
        
        loadQuestions();
        showQuestion(0);
        
        totalSeconds = exam.getDurationMinutes() * 60;
        remainingSeconds = totalSeconds;
        startTimer();
    }

    private void loadQuestions() {
        questions.clear();
        states.clear();
        paletteButtons.clear();
        paletteGrid.removeAll();
        
        try {
            List<Question> loaded = examController.loadQuestions(exam.getExamId(), exam.isRandomizeQuestions());
            if (loaded != null) questions.addAll(loaded);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < questions.size(); i++) {
            states.add(new QuestionState(buildOptions(questions.get(i))));
            PaletteButton btn = new PaletteButton(i + 1);
            int idx = i;
            btn.addActionListener(e -> showQuestion(idx));
            paletteButtons.add(btn);
            paletteGrid.add(btn);
        }
        paletteGrid.revalidate();
        paletteGrid.repaint();
    }

    private List<OptionItem> buildOptions(Question question) {
        List<OptionItem> items = new ArrayList<>();
        items.add(new OptionItem("A", question.getOptionA()));
        items.add(new OptionItem("B", question.getOptionB()));
        items.add(new OptionItem("C", question.getOptionC()));
        items.add(new OptionItem("D", question.getOptionD()));
        if (exam.isRandomizeQuestions()) Collections.shuffle(items);
        return items;
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentIndex = index;
        Question q = questions.get(index);
        QuestionState s = states.get(index);
        s.visited = true;

        questionIndexLabel.setText("Q " + (index + 1) + " / " + questions.size());
        questionTextArea.setText(q.getQuestionText());

        optionsContainer.removeAll();
        optionGroup.clearSelection();
        optionCards.clear();

        for (OptionItem item : s.options) {
            OptionCard card = new OptionCard(item.label, item.text);
            if (item.label.equalsIgnoreCase(s.selectedOption)) {
                card.setSelected(true);
            }
            card.addActionListener(e -> selectOption(item.label));
            optionGroup.add(card.getRadioButton());
            optionsContainer.add(card);
            optionsContainer.add(javax.swing.Box.createVerticalStrut(12));
            optionCards.add(card);
        }

        markReviewBtn.setText(s.markedReview ? "★ Marked" : "Mark for Review");
        
        updatePalette();
        updateProgress();
        optionsContainer.revalidate();
        optionsContainer.repaint();
    }

    private void selectOption(String option) {
        QuestionState s = states.get(currentIndex);
        s.selectedOption = option;
        s.answered = true;
        updatePalette();
        updateProgress();
        saveAnswer(option);
    }

    private void saveAnswer(String option) {
        Question q = questions.get(currentIndex);
        Answer ans = new Answer(attemptId, q.getQuestionId(), option, states.get(currentIndex).markedReview);
        try {
            examController.saveAnswer(ans);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveAndNext() {
        if (currentIndex < questions.size() - 1) {
            showQuestion(currentIndex + 1);
        }
    }

    private void showPrevious() {
        if (currentIndex > 0) {
            showQuestion(currentIndex - 1);
        }
    }

    private void clearResponse() {
        QuestionState s = states.get(currentIndex);
        s.selectedOption = null;
        s.answered = false;
        s.markedReview = false;
        showQuestion(currentIndex);
    }

    private void toggleMarkReview() {
        QuestionState s = states.get(currentIndex);
        s.markedReview = !s.markedReview;
        markReviewBtn.setText(s.markedReview ? "★ Marked" : "Mark for Review");
        updatePalette();
    }

    private void updatePalette() {
        for (int i = 0; i < paletteButtons.size(); i++) {
            paletteButtons.get(i).updateState(states.get(i), i == currentIndex);
        }
    }

    private void updateProgress() {
        int answered = 0;
        for (QuestionState s : states) if (s.answered) answered++;
        int pct = (questions.size() == 0) ? 0 : (answered * 100 / questions.size());
        progressBar.setProgress(pct);
        progressPercentLabel.setText(pct + "% Attempted");
    }

    private void submitWithConfirmation() {
        int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to submit?", "Submit Exam", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            submitExam(false);
        }
    }

    private void submitExam(boolean auto) {
        stopTimer();
        List<Answer> answers = new ArrayList<>();
        Map<Integer, String> ansMap = new HashMap<>();
        for(int i=0; i<questions.size(); i++) {
            Question q = questions.get(i);
            QuestionState s = states.get(i);
            answers.add(new Answer(attemptId, q.getQuestionId(), s.selectedOption, s.markedReview));
            ansMap.put(q.getQuestionId(), s.selectedOption);
        }
        
        Result result = examController.calculateResult(attemptId, userId, exam.getExamId(), questions, answers, totalSeconds - remainingSeconds);
        try {
            examController.saveResult(result);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        resultView.setSolutionData(questions, ansMap, exam.getTitle(), studentName);
        resultView.setResult(result);
        navigation.show("resultView");
    }

    private void startTimer() {
        stopTimer();
        examTimer = new Timer(1000, e -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                submitExam(true);
            }
        });
        examTimer.start();
    }

    private void stopTimer() {
        if (examTimer != null) examTimer.stop();
    }

    private void updateTimerDisplay() {
        int m = remainingSeconds / 60;
        int s = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d:%02d", m / 60, m % 60, s));
        if (remainingSeconds < 300) timerLabel.setForeground(new Color(239, 68, 68));
    }

    private void applyTheme() {
        boolean dark = ThemeManager.isDarkMode();
        setBackground(dark ? new Color(15, 23, 42) : new Color(248, 250, 252));
        examNameLabel.setForeground(dark ? Color.WHITE : new Color(15, 23, 42));
        studentNameLabel.setForeground(dark ? new Color(241, 245, 249) : new Color(30, 41, 59));
        questionTextArea.setForeground(dark ? Color.WHITE : new Color(15, 23, 42));
        
        ThemeManager.applyThemeRecursively(this);
        // Apply legend colors AFTER theme recursion to avoid override
        updateLegend();
    }

    // --- Custom Components ---

    private static class RoundedPanel extends JPanel {
        private final int radius;
        public RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    private static class ProgressBar extends JPanel {
        private int progress = 0;
        public ProgressBar() { setPreferredSize(new Dimension(0, 8)); setOpaque(false); }
        public void setProgress(int p) { this.progress = p; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(229, 231, 235));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(new Color(59, 130, 246));
            g2.fillRoundRect(0, 0, (int)(getWidth() * (progress/100.0)), getHeight(), 8, 8);
            g2.dispose();
        }
    }

    private static class PaletteButton extends JButton {
        private Color bgColor = new Color(148, 163, 184); // Default Not Visited
        private Color fgColor = Color.WHITE;
        private boolean isActive = false;

        public PaletteButton(int n) {
            super(String.valueOf(n));
            setPreferredSize(new Dimension(42, 42));
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void updateState(QuestionState s, boolean active) {
            this.isActive = active;
            if (active) { 
                bgColor = new Color(59, 130, 246); // Active Blue
                fgColor = Color.WHITE; 
            } else if (s.markedReview) { 
                bgColor = new Color(59, 130, 246); // Marked Blue
                fgColor = Color.WHITE; 
            } else if (s.answered) { 
                bgColor = new Color(34, 197, 94); // Answered Green
                fgColor = Color.WHITE; 
            } else if (s.visited) { 
                bgColor = new Color(239, 68, 68); // Not Answered Red
                fgColor = Color.WHITE; 
            } else { 
                bgColor = new Color(148, 163, 184); // Not Visited Grey
                fgColor = Color.WHITE; 
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (isActive) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 10, 10);
            }
            g2.setColor(fgColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            g2.dispose();
        }
    }

    private static class OptionCard extends JPanel {
        private final javax.swing.JRadioButton radio = new javax.swing.JRadioButton();
        private final JLabel textLabel;
        private boolean hover = false;

        public OptionCard(String label, String text) {
            setLayout(new BorderLayout(15, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            
            radio.setOpaque(false); radio.setFocusPainted(false);
            textLabel = new JLabel(label + ". " + text);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            
            add(radio, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
            
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { radio.setSelected(true); }
            });
        }

        public void setSelected(boolean s) { radio.setSelected(s); repaint(); }
        public void addActionListener(java.awt.event.ActionListener l) { radio.addActionListener(l); }
        public javax.swing.JRadioButton getRadioButton() { return radio; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            boolean dark = ThemeManager.isDarkMode();
            boolean selected = radio.isSelected();
            
            Color bg;
            Color border;
            
            if (selected) {
                bg = dark ? new Color(30, 64, 175) : new Color(238, 242, 255);
                border = new Color(59, 130, 246);
            } else if (hover) {
                bg = dark ? new Color(51, 65, 85) : new Color(248, 250, 252);
                border = new Color(59, 130, 246);
            } else {
                bg = dark ? new Color(51, 65, 85) : Color.WHITE;
                border = dark ? new Color(71, 85, 105) : new Color(229, 231, 235);
            }
            
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(selected ? 2.0f : 1.0f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            
            textLabel.setForeground(dark ? Color.WHITE : new Color(30, 41, 59));
            g2.dispose();
        }
    }

    private static class PrimaryGradientButton extends JButton {
        private double hoverProgress = 0;
        private Timer timer;

        public PrimaryGradientButton(String t) {
            super(t); setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { startAnim(true); }
                @Override public void mouseExited(MouseEvent e) { startAnim(false); }
            });
        }

        private void startAnim(boolean entering) {
            if (timer != null) timer.stop();
            timer = new Timer(15, e -> {
                if (entering) { hoverProgress += 0.15; if (hoverProgress >= 1.0) { hoverProgress = 1.0; timer.stop(); } }
                else { hoverProgress -= 0.15; if (hoverProgress <= 0.0) { hoverProgress = 0.0; timer.stop(); } }
                repaint();
            });
            timer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            double scale = 1.0 + (hoverProgress * 0.02);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2.translate(centerX, centerY);
            g2.scale(scale, scale);
            g2.translate(-centerX, -centerY);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(0.1 + hoverProgress * 0.1)));
            g2.setColor(new Color(59, 130, 246));
            g2.fillRoundRect(2, 4, getWidth()-4, getHeight()-4, 10, 10);
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(59, 130, 246), getWidth(), 0, new Color(37, 99, 235)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            g2.dispose();
        }
    }

    private static class SecondaryButton extends JButton {
        private final boolean danger;
        private double hoverProgress = 0;
        private Timer timer;

        public SecondaryButton(String t, boolean d) {
            super(t); this.danger = d;
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { startAnim(true); }
                @Override public void mouseExited(MouseEvent e) { startAnim(false); }
            });
        }

        private void startAnim(boolean entering) {
            if (timer != null) timer.stop();
            timer = new Timer(15, e -> {
                if (entering) { hoverProgress += 0.15; if (hoverProgress >= 1.0) { hoverProgress = 1.0; timer.stop(); } }
                else { hoverProgress -= 0.15; if (hoverProgress <= 0.0) { hoverProgress = 0.0; timer.stop(); } }
                repaint();
            });
            timer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            double scale = 1.0 + (hoverProgress * 0.02);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2.translate(centerX, centerY);
            g2.scale(scale, scale);
            g2.translate(-centerX, -centerY);

            Color borderColor = danger ? new Color(239, 68, 68) : new Color(59, 130, 246);
            if (hoverProgress > 0) {
                borderColor = danger ? new Color(220, 38, 38) : new Color(37, 99, 235);
            }
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
            g2.setColor(Color.BLACK);
            g2.fillRoundRect(1, 2, getWidth()-2, getHeight()-2, 10, 10);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            if (hoverProgress > 0) {
                Color bg = danger ? new Color(254, 242, 242) : new Color(238, 242, 255);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)hoverProgress));
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            
            g2.setColor(danger ? new Color(239, 68, 68) : new Color(59, 130, 246));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            g2.dispose();
        }
    }

    private static class ProfileBadge extends JPanel {
        private String initial = "A";
        public ProfileBadge() { setPreferredSize(new Dimension(36, 36)); setOpaque(false); }
        public void setInitial(String i) { this.initial = i; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(59, 130, 246));
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, (getWidth()-fm.stringWidth(initial))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            g2.dispose();
        }
    }

    private static class OptionItem {
        private final String label;
        private final String text;
        private OptionItem(String l, String t) { this.label = l; this.text = t; }
    }

    private static class QuestionState {
        private final List<OptionItem> options;
        private String selectedOption;
        private boolean visited = false;
        private boolean markedReview = false;
        private boolean answered = false;
        private QuestionState(List<OptionItem> o) { this.options = o; }
    }
}
