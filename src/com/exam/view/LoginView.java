package com.exam.view;

import com.exam.controller.AuthController;
import com.exam.model.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginView extends JPanel {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final AuthController authController;
    private final StudentDashboardView studentDashboardView;

    private final JTextField loginEmailField = new JTextField(20);
    private final JPasswordField loginPasswordField = new JPasswordField(20);
    private final JComboBox<String> loginRoleBox = new JComboBox<>(new String[]{"AUTO", "STUDENT", "ADMIN"});
    private final JLabel loginErrorLabel = new JLabel(" ");

    private final JTextField regNameField = new JTextField(20);
    private final JTextField regEmailField = new JTextField(20);
    private final JPasswordField regPasswordField = new JPasswordField(20);
    private final JPasswordField regConfirmField = new JPasswordField(20);
    private final JLabel regErrorLabel = new JLabel(" ");

    private final JPanel cardWrap;
    private final JPanel loginPanel;
    private final JPanel registerPanel;
    private boolean isLoginActive = true;

    // Tabs
    private final JLabel loginTabLbl;
    private final JLabel registerTabLbl;
    private final JPanel tabUnderline;

    public LoginView(AuthController authController, StudentDashboardView studentDashboardView) {
        this.authController = authController;
        this.studentDashboardView = studentDashboardView;

        setLayout(new GridBagLayout());
        
        loginPanel = buildLoginPanel();
        registerPanel = buildRegisterPanel();

        // Top icon
        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255)); // #EFF6FF
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(new Color(59, 130, 246)); // #3B82F6
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(14, 18, 28, 20, 4, 4);
                g2.drawLine(22, 18, 34, 18);
                g2.drawLine(24, 42, 32, 42);
                g2.drawLine(28, 38, 28, 42);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(56, 56));
        iconPanel.setMaximumSize(new Dimension(56, 56));
        iconPanel.setOpaque(false);

        // Header text
        JLabel titleLabel = new JLabel("ExamSphere");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(new Color(30, 41, 59)); // #1E293B

        JLabel subtitleLabel = new JLabel("(Exam Management System)");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subtitleLabel.setForeground(new Color(107, 114, 128)); // #6B7280

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        iconPanel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        JPanel blueLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(0, 0, 32, 2, 2, 2);
                g2.dispose();
            }
        };
        blueLine.setPreferredSize(new Dimension(32, 2));
        blueLine.setMaximumSize(new Dimension(32, 2));
        blueLine.setOpaque(false);
        blueLine.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(iconPanel);
        headerPanel.add(Box.createVerticalStrut(12));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(blueLine);

        // Tabs
        loginTabLbl = new JLabel("Login");
        registerTabLbl = new JLabel("Register");
        
        MouseAdapter tabClick = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                isLoginActive = (e.getSource() == loginTabLbl);
                updateTabs();
            }
        };
        
        loginTabLbl.addMouseListener(tabClick);
        registerTabLbl.addMouseListener(tabClick);
        loginTabLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerTabLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        tabUnderline = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(226, 232, 240));
                g2.fillRect(0, getHeight()-2, getWidth(), 2);
                g2.setColor(new Color(37, 99, 235));
                int w = getWidth() / 2;
                int x = isLoginActive ? 0 : w;
                g2.fillRoundRect(x, getHeight()-2, w, 2, 2, 2);
                g2.dispose();
            }
        };
        tabUnderline.setOpaque(false);
        tabUnderline.setLayout(new GridLayout(1, 2));
        
        loginTabLbl.setHorizontalAlignment(JLabel.CENTER);
        registerTabLbl.setHorizontalAlignment(JLabel.CENTER);
        loginTabLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        registerTabLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        tabUnderline.add(loginTabLbl);
        tabUnderline.add(registerTabLbl);

        cardWrap = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20)); // ~0.08 alpha
                g2.fillRoundRect(8, 12, getWidth()-16, getHeight()-16, 20, 20);
                // Card bg
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-12, getHeight()-16, 16, 16);
                g2.dispose();
            }
        };
        cardWrap.setOpaque(false);
        cardWrap.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 40));
        cardWrap.setPreferredSize(new Dimension(460, 560));
        cardWrap.setMaximumSize(new Dimension(460, 560));

        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setOpaque(false);
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(tabUnderline, BorderLayout.SOUTH);

        cardWrap.add(topSection, BorderLayout.NORTH);
        cardWrap.add(loginPanel, BorderLayout.CENTER);

        add(cardWrap);
        updateTabs();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();

        // 1. Background gradient #EEF2FF -> #E0ECFF
        g2.setPaint(new GradientPaint(0, 0, new Color(238, 242, 255), w, h, new Color(224, 236, 255)));
        g2.fillRect(0, 0, w, h);

        // 2. Dotted patterns
        g2.setColor(new Color(148, 163, 184, 50));
        for (int i=0; i<6; i++) {
            for (int j=0; j<4; j++) {
                g2.fillOval(40 + i*16, 80 + j*16, 4, 4);
                g2.fillOval(w - 120 + i*16, h/2 + j*16, 4, 4);
            }
        }

        // 3. Curved shape bottom right
        g2.setColor(new Color(59, 130, 246, 20));
        g2.fillOval(w - 300, h - 200, 500, 500);
        g2.setColor(new Color(37, 99, 235, 15));
        g2.fillOval(w - 150, h - 100, 300, 300);

        g2.dispose();
    }

    private void updateTabs() {
        loginTabLbl.setForeground(isLoginActive ? new Color(37, 99, 235) : new Color(100, 116, 139));
        loginTabLbl.setFont(loginTabLbl.getFont().deriveFont(isLoginActive ? Font.BOLD : Font.PLAIN, 14f));
        
        registerTabLbl.setForeground(!isLoginActive ? new Color(37, 99, 235) : new Color(100, 116, 139));
        registerTabLbl.setFont(registerTabLbl.getFont().deriveFont(!isLoginActive ? Font.BOLD : Font.PLAIN, 14f));
        
        tabUnderline.repaint();

        cardWrap.remove(loginPanel);
        cardWrap.remove(registerPanel);
        cardWrap.add(isLoginActive ? loginPanel : registerPanel, BorderLayout.CENTER);
        
        cardWrap.revalidate();
        cardWrap.repaint();
    }

    private JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        styleInput(loginEmailField);
        styleInput(loginPasswordField);
        
        loginRoleBox.setFont(loginRoleBox.getFont().deriveFont(13f));
        loginRoleBox.setBackground(new Color(248, 250, 252));
        loginRoleBox.setBorder(BorderFactory.createEmptyBorder());
        loginRoleBox.setOpaque(false);
        
        p.add(createInputWrapper("Email", "mail", loginEmailField));
        p.add(Box.createVerticalStrut(10));
        p.add(createInputWrapper("Password", "lock", loginPasswordField));
        p.add(Box.createVerticalStrut(10));
        p.add(createInputWrapper("Login As", "user", loginRoleBox));
        
        // Extra options row
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setOpaque(false);
        rememberMe.setForeground(new Color(100, 116, 139));
        rememberMe.setFont(rememberMe.getFont().deriveFont(12f));
        rememberMe.setFocusPainted(false);
        
        JLabel forgotPass = new JLabel("Forgot password?");
        forgotPass.setForeground(new Color(59, 130, 246));
        forgotPass.setFont(forgotPass.getFont().deriveFont(Font.PLAIN, 12f));
        forgotPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        optionsRow.add(rememberMe, BorderLayout.WEST);
        optionsRow.add(forgotPass, BorderLayout.EAST);
        optionsRow.setMaximumSize(new Dimension(400, 24));

        p.add(Box.createVerticalStrut(8));
        p.add(optionsRow);

        loginErrorLabel.setForeground(new Color(220, 38, 38));
        loginErrorLabel.setFont(loginErrorLabel.getFont().deriveFont(12f));
        loginErrorLabel.setAlignmentX(CENTER_ALIGNMENT);

        p.add(Box.createVerticalStrut(4));
        p.add(loginErrorLabel);
        p.add(Box.createVerticalStrut(4));

        JPanel bottomPnl = new JPanel(new BorderLayout());
        bottomPnl.setOpaque(false);
        bottomPnl.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton loginBtn = createBtn("Login");
        loginBtn.addActionListener(e -> handleLogin());
        bottomPnl.add(loginBtn, BorderLayout.CENTER);

        wrapper.add(p, BorderLayout.CENTER);
        wrapper.add(bottomPnl, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel buildRegisterPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        styleInput(regNameField);
        styleInput(regEmailField);
        styleInput(regPasswordField);
        styleInput(regConfirmField);

        p.add(createInputWrapper("Name", "user", regNameField));
        p.add(Box.createVerticalStrut(6));
        p.add(createInputWrapper("Email", "mail", regEmailField));
        p.add(Box.createVerticalStrut(6));
        p.add(createInputWrapper("Password", "lock", regPasswordField));
        p.add(Box.createVerticalStrut(6));
        p.add(createInputWrapper("Confirm Password", "lock", regConfirmField));

        regErrorLabel.setForeground(new Color(220, 38, 38));
        regErrorLabel.setFont(regErrorLabel.getFont().deriveFont(12f));
        regErrorLabel.setAlignmentX(CENTER_ALIGNMENT);

        p.add(Box.createVerticalStrut(4));
        p.add(regErrorLabel);
        p.add(Box.createVerticalStrut(4));

        JPanel bottomPnl = new JPanel(new BorderLayout());
        bottomPnl.setOpaque(false);
        bottomPnl.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton regBtn = createBtn("Register");
        regBtn.addActionListener(e -> handleRegister());
        bottomPnl.add(regBtn, BorderLayout.CENTER);

        wrapper.add(p, BorderLayout.CENTER);
        wrapper.add(bottomPnl, BorderLayout.SOUTH);

        return wrapper;
    }

    private void styleInput(JTextField field) {
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 12));
        field.setFont(field.getFont().deriveFont(13f));
        field.setForeground(new Color(15, 23, 42));
        field.setOpaque(false);
    }

    private JPanel createInputWrapper(String labelTxt, String iconType, java.awt.Component field) {
        JPanel wrap = new JPanel(new BorderLayout(0, 2));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(400, 54));

        JLabel lbl = new JLabel(labelTxt);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(new Color(51, 65, 85));
        wrap.add(lbl, BorderLayout.NORTH);

        JPanel inputBg = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        inputBg.setOpaque(false);
        inputBg.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 2));

        JPanel iconPnl = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                int y = 11;
                if ("mail".equals(iconType)) {
                    g2.drawRect(2, y, 14, 10);
                    g2.drawLine(2, y, 9, y+5);
                    g2.drawLine(16, y, 9, y+5);
                } else if ("lock".equals(iconType)) {
                    g2.drawRoundRect(4, y, 10, 10, 2, 2);
                    g2.drawArc(6, y-4, 6, 8, 0, 180);
                    g2.fillRect(8, y+3, 2, 4);
                } else if ("user".equals(iconType)) {
                    g2.drawOval(5, y-2, 6, 6);
                    g2.drawArc(2, y+4, 12, 10, 0, 180);
                }
                g2.dispose();
            }
        };
        iconPnl.setOpaque(false);
        iconPnl.setPreferredSize(new Dimension(20, 32));

        inputBg.add(iconPnl, BorderLayout.WEST);
        
        if (field instanceof JComboBox) {
            JPanel pad = new JPanel(new BorderLayout());
            pad.setOpaque(false);
            pad.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            pad.add(field, BorderLayout.CENTER);
            inputBg.add(pad, BorderLayout.CENTER);
        } else {
            inputBg.add(field, BorderLayout.CENTER);
        }

        wrap.add(inputBg, BorderLayout.CENTER);
        return wrap;
    }

    private JButton createBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(59, 130, 246), getWidth(), 0, new Color(37, 99, 235)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(400, 44));
        btn.setPreferredSize(new Dimension(400, 44));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        return btn;
    }

    private void handleLogin() {
        loginErrorLabel.setText(" ");
        String email = loginEmailField.getText();
        String password = new String(loginPasswordField.getPassword());

        String validationError = validateLogin(email, password);
        if (validationError != null) {
            loginErrorLabel.setText(validationError);
            return;
        }

        try {
            String roleChoice = String.valueOf(loginRoleBox.getSelectedItem());
            User user;
            if ("ADMIN".equalsIgnoreCase(roleChoice)) {
                user = authController.loginAdmin(email, password);
            } else if ("STUDENT".equalsIgnoreCase(roleChoice)) {
                user = authController.loginStudent(email, password);
            } else {
                user = authController.loginStudent(email, password);
                if (user == null) {
                    user = authController.loginAdmin(email, password);
                }
            }
            if (user == null) {
                loginErrorLabel.setText(authController.getLastError());
                return;
            }
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                authController.goToView("adminDashboard");
            } else {
                if (studentDashboardView != null) {
                    studentDashboardView.setCurrentStudent(user.getUserId(), user.getFullName());
                }
                authController.goToView("studentDashboard");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        regErrorLabel.setText(" ");
        String name = regNameField.getText();
        String email = regEmailField.getText();
        String password = new String(regPasswordField.getPassword());
        String confirm = new String(regConfirmField.getPassword());

        String validationError = validateRegister(name, email, password, confirm);
        if (validationError != null) {
            regErrorLabel.setText(validationError);
            return;
        }

        User user = new User();
        user.setFullName(name.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(password.trim());

        try {
            int id = authController.registerStudent(user);
            if (id > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful. Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearRegisterFields();
                isLoginActive = true;
                updateTabs();
            } else {
                regErrorLabel.setText(authController.getLastError());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String validateLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) return "Email is required.";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Invalid email format.";
        if (password == null || password.trim().isEmpty()) return "Password is required.";
        return null;
    }

    private String validateRegister(String name, String email, String password, String confirm) {
        if (name == null || name.trim().isEmpty()) return "Name is required.";
        if (email == null || email.trim().isEmpty()) return "Email is required.";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Invalid email format.";
        if (password == null || password.trim().isEmpty()) return "Password is required.";
        if (!password.equals(confirm)) return "Passwords do not match.";
        return null;
    }

    private void clearRegisterFields() {
        regNameField.setText("");
        regEmailField.setText("");
        regPasswordField.setText("");
        regConfirmField.setText("");
    }
}

