package com.exam.util;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public final class ThemeManager {
    private ThemeManager() {
    }

    private static boolean darkMode = false;
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggle() {
        darkMode = !darkMode;
        notifyListeners();
    }

    public static void addThemeListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private static void notifyListeners() {
        for (Runnable listener : listeners) {
            SwingUtilities.invokeLater(listener);
        }
    }

    public static Color getBackground() {
        return darkMode ? new Color(17, 24, 39) : new Color(241, 245, 249);
    }

    public static Color getPageBackground() {
        return darkMode ? new Color(17, 24, 39) : new Color(241, 245, 249);
    }

    public static Color getCardColor() {
        return darkMode ? new Color(30, 41, 59) : Color.WHITE;
    }

    public static Color getText() {
        return darkMode ? new Color(226, 232, 240) : new Color(17, 24, 39);
    }

    public static Color getTextSecondary() {
        return darkMode ? new Color(148, 163, 184) : new Color(75, 85, 99);
    }

    public static Color getTextSecondarySoft() {
        return darkMode ? new Color(148, 163, 184) : new Color(107, 114, 128);
    }

    public static Color getBorder() {
        return darkMode ? new Color(51, 65, 85) : new Color(220, 224, 230);
    }

    public static Color getInputBorder() {
        return darkMode ? new Color(71, 85, 105) : new Color(229, 231, 235);
    }

    public static Color getAccent() {
        return darkMode ? new Color(79, 142, 247) : new Color(37, 99, 235);
    }

    public static Color getAccentHover() {
        return new Color(61, 124, 229);
    }

    public static Color getSidebar() {
        return darkMode ? new Color(15, 23, 42) : new Color(15, 23, 42);
    }

    public static Color getSidebarBottom() {
        return darkMode ? new Color(30, 41, 59) : new Color(30, 41, 59);
    }

    public static Color getSidebarText() {
        return new Color(203, 213, 225);
    }

    public static Color getHover() {
        return darkMode ? new Color(51, 65, 85) : new Color(30, 58, 138);
    }

    public static Color getSuccess() {
        return new Color(34, 197, 94);
    }

    public static Color getWarning() {
        return new Color(245, 158, 11);
    }

    public static Color getDanger() {
        return new Color(239, 68, 68);
    }

    public static Color getMuted() {
        return darkMode ? new Color(100, 116, 139) : new Color(156, 163, 175);
    }

    public static void applyThemeRecursively(java.awt.Component comp) {
        if (comp instanceof javax.swing.JPanel) {
            comp.setBackground(getBackground());
        } else if (comp instanceof javax.swing.JLabel label) {
            label.setForeground(getText());
        } else if (comp instanceof javax.swing.JButton btn) {
            btn.setBackground(getAccent());
            btn.setForeground(getSidebarText());
        } else if (comp instanceof javax.swing.JTextField field) {
            field.setBackground(getCardColor());
            field.setForeground(getText());
            field.setCaretColor(getText());
        } else if (comp instanceof javax.swing.JTextArea area) {
            area.setBackground(getCardColor());
            area.setForeground(getText());
            area.setCaretColor(getText());
        }

        if (comp instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                applyThemeRecursively(child);
            }
        }
    }
}
