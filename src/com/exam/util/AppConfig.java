package com.exam.util;

public final class AppConfig {
    private AppConfig() {
    }

    public static final String DB_URL = "jdbc:mysql://localhost:3306/online_exam?useSSL=false&serverTimezone=UTC";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "Aditya@123";

    public static final String THEME_DEFAULT = "LIGHT";
}
