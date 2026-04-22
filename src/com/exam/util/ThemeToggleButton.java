package com.exam.util;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class ThemeToggleButton extends JButton {
    public ThemeToggleButton() {
        setPreferredSize(new Dimension(36, 28));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setToolTipText("Toggle theme");
        addActionListener(e -> ThemeManager.toggle());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ThemeManager.getText());
        g2.setStroke(new BasicStroke(2f));

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        if (ThemeManager.isDarkMode()) {
            g2.drawOval(cx - 6, cy - 6, 12, 12);
            g2.setColor(ThemeManager.getBackground());
            g2.fillOval(cx - 2, cy - 6, 12, 12);
        } else {
            g2.drawOval(cx - 6, cy - 6, 12, 12);
            g2.drawLine(cx, cy - 10, cx, cy - 14);
            g2.drawLine(cx, cy + 10, cx, cy + 14);
            g2.drawLine(cx - 10, cy, cx - 14, cy);
            g2.drawLine(cx + 10, cy, cx + 14, cy);
        }
        g2.dispose();
    }
}
