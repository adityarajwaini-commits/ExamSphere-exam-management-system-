package com.exam.util;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

public class NavigationHelper {
    private final JPanel container;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> views;

    public NavigationHelper(JPanel container, CardLayout cardLayout) {
        this.container = container;
        this.cardLayout = cardLayout;
        this.views = new HashMap<>();
    }

    public void registerView(String name, JPanel panel) {
        if (!views.containsKey(name)) {
            views.put(name, panel);
            container.add(panel, name);
        }
    }

    public void show(String name) {
        cardLayout.show(container, name);
    }

    public JPanel getContainer() {
        return container;
    }
}
