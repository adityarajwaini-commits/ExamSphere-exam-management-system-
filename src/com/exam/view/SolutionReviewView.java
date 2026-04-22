package com.exam.view;

import com.exam.model.Question;
import com.exam.util.NavigationHelper;
import com.exam.util.ThemeManager;
import com.exam.util.ThemeToggleButton;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

public class SolutionReviewView extends JPanel {
    private final NavigationHelper navigation;
    private final JPanel listPanel = new JPanel(new GridBagLayout());
    private final JLabel titleLabel = new JLabel("Solution Review");
    private final JLabel subtitleLabel = new JLabel("");
    private final JLabel correctBadge = new JLabel("Correct: 0");
    private final JLabel wrongBadge = new JLabel("Wrong: 0");
    private final JLabel skippedBadge = new JLabel("Skipped: 0");

    private List<Question> questions = new ArrayList<>();
    private Map<Integer, String> answerMap;

    public SolutionReviewView(NavigationHelper navigation) {
        this.navigation = navigation;
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildList(), BorderLayout.CENTER);

        ThemeManager.addThemeListener(this::applyTheme);
        ThemeManager.addThemeListener(this::repaint);
        applyTheme();
    }

    public void setData(List<Question> questions, Map<Integer, String> answerMap,
                        String examTitle, String studentName) {
        this.questions = questions == null ? new ArrayList<>() : questions;
        this.answerMap = answerMap;
        subtitleLabel.setText((examTitle == null ? "" : examTitle)
                + (studentName == null || studentName.isEmpty() ? "" : " | " + studentName));
        rebuildList();
        animateCards();
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JButton back = new ChevronButton();
        back.addActionListener(e -> navigation.show("resultView"));

        JPanel titles = new JPanel(new GridLayout(2, 1, 2, 2));
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        titles.add(titleLabel);
        titles.add(subtitleLabel);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badges.add(badge(correctBadge, ThemeManager.getSuccess()));
        badges.add(badge(wrongBadge, ThemeManager.getDanger()));
        badges.add(badge(skippedBadge, ThemeManager.getMuted()));
        badges.add(new ThemeToggleButton());

        top.add(back, BorderLayout.WEST);
        top.add(titles, BorderLayout.CENTER);
        top.add(badges, BorderLayout.EAST);
        return top;
    }

    private JScrollPane buildList() {
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void rebuildList() {
        listPanel.removeAll();
        int correct = 0;
        int wrong = 0;
        int skipped = 0;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 16, 8, 16);

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String selected = answerMap == null ? null : answerMap.get(q.getQuestionId());
            if (selected == null || selected.isEmpty()) {
                skipped++;
            } else if (q.getCorrectOption().equalsIgnoreCase(selected)) {
                correct++;
            } else {
                wrong++;
            }
            QuestionCard card = new QuestionCard(i + 1, q, selected);
            listPanel.add(card, gbc);
            gbc.gridy++;
        }

        correctBadge.setText("Correct: " + correct);
        wrongBadge.setText("Wrong: " + wrong);
        skippedBadge.setText("Skipped: " + skipped);

        listPanel.revalidate();
        listPanel.repaint();
    }

    private void animateCards() {
        List<QuestionCard> cards = new ArrayList<>();
        for (int i = 0; i < listPanel.getComponentCount(); i++) {
            if (listPanel.getComponent(i) instanceof QuestionCard card) {
                card.setAlpha(0f);
                card.setYOffset(12);
                cards.add(card);
            }
        }

        Timer timer = new Timer(30, null);
        final int[] index = {0};
        timer.addActionListener((ActionEvent e) -> {
            if (index[0] >= cards.size()) {
                timer.stop();
                return;
            }
            QuestionCard card = cards.get(index[0]);
            float nextAlpha = Math.min(1f, card.getAlpha() + 0.25f);
            int nextOffset = Math.max(0, card.getYOffset() - 4);
            card.setAlpha(nextAlpha);
            card.setYOffset(nextOffset);
            if (nextAlpha >= 1f && nextOffset == 0) {
                index[0]++;
            }
        });
        timer.start();
    }

    private JLabel badge(JLabel label, Color color) {
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(ThemeManager.getSidebarText());
        label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return label;
    }

    private static class ChevronButton extends JButton {
        private ChevronButton() {
            setPreferredSize(new Dimension(36, 28));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ThemeManager.getText());
            g2.setStroke(new BasicStroke(2f));
            int midY = getHeight() / 2;
            g2.drawLine(20, midY - 6, 10, midY);
            g2.drawLine(10, midY, 20, midY + 6);
            g2.dispose();
        }
    }

    private static class QuestionCard extends JPanel {
        private final int number;
        private final Question question;
        private final String selected;
        private float alpha = 1f;
        private int yOffset;

        private QuestionCard(int number, Question question, String selected) {
            this.number = number;
            this.question = question;
            this.selected = selected;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            setPreferredSize(new Dimension(800, 220));
        }

        private void setAlpha(float alpha) {
            this.alpha = alpha;
            repaint();
        }

        private void setYOffset(int yOffset) {
            this.yOffset = yOffset;
            repaint();
        }

        private float getAlpha() {
            return alpha;
        }

        private int getYOffset() {
            return yOffset;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.translate(0, yOffset);

            int width = getWidth();
            int height = getHeight() - 1;
            g2.setColor(ThemeManager.getCardColor());
            g2.fillRoundRect(0, 0, width, height, 16, 16);
            g2.setColor(ThemeManager.getBorder());
            g2.drawRoundRect(0, 0, width - 1, height - 1, 16, 16);

            g2.setColor(ThemeManager.getText());
            g2.setFont(getFont().deriveFont(14f));
            drawWrappedText(g2, "Q " + number + ": " + question.getQuestionText(), 16, 28, width - 32);

            int y = 70;
            y = drawOption(g2, y, "A", question.getOptionA());
            y = drawOption(g2, y, "B", question.getOptionB());
            y = drawOption(g2, y, "C", question.getOptionC());
            drawOption(g2, y, "D", question.getOptionD());

            g2.setColor(ThemeManager.getBorder());
            g2.drawLine(16, height - 16, width - 16, height - 16);

            g2.dispose();
            super.paintComponent(g);
        }

        private int drawOption(Graphics2D g2, int y, String label, String text) {
            String selectedOption = selected == null ? "" : selected;
            boolean isCorrect = label.equalsIgnoreCase(question.getCorrectOption());
            boolean isSelected = label.equalsIgnoreCase(selectedOption);

            Color bg = ThemeManager.getCardColor();
            Color fg = ThemeManager.getText();

            if (isCorrect) {
                bg = ThemeManager.getSuccess();
                fg = ThemeManager.getSidebarText();
            }
            if (isSelected && !isCorrect) {
                bg = ThemeManager.getDanger();
                fg = ThemeManager.getSidebarText();
            }
            if (selectedOption.isEmpty() && !isCorrect) {
                fg = ThemeManager.getTextSecondary();
            }

            int width = getWidth() - 32;
            g2.setColor(bg);
            g2.fillRoundRect(16, y, width, 28, 14, 14);
            g2.setColor(fg);
            g2.drawString(label + ". " + text, 28, y + 19);

            if (isCorrect) {
                drawCheck(g2, width + 4, y + 8, fg);
            } else if (isSelected && !isCorrect) {
                drawX(g2, width + 4, y + 8, fg);
            }

            return y + 36;
        }

        private void drawCheck(Graphics2D g2, int x, int y, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, y + 6, x + 6, y + 12);
            g2.drawLine(x + 6, y + 12, x + 16, y);
        }

        private void drawX(Graphics2D g2, int x, int y, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, y, x + 16, y + 16);
            g2.drawLine(x + 16, y, x, y + 16);
        }

        private void drawWrappedText(Graphics2D g2, String text, int x, int y, int width) {
            FontMetrics fm = g2.getFontMetrics();
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            int lineHeight = fm.getHeight();
            int drawY = y;
            for (String word : words) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(candidate) > width) {
                    g2.drawString(line.toString(), x, drawY);
                    line = new StringBuilder(word);
                    drawY += lineHeight;
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            if (line.length() > 0) {
                g2.drawString(line.toString(), x, drawY);
            }
        }
    }

    private void applyTheme() {
        setBackground(ThemeManager.getBackground());
        listPanel.setBackground(ThemeManager.getBackground());
        titleLabel.setForeground(ThemeManager.getText());
        subtitleLabel.setForeground(ThemeManager.getTextSecondary());
        
        ThemeManager.applyThemeRecursively(this);
        
        correctBadge.setForeground(ThemeManager.getSidebarText());
        wrongBadge.setForeground(ThemeManager.getSidebarText());
        skippedBadge.setForeground(ThemeManager.getSidebarText());
        repaint();
    }
}
