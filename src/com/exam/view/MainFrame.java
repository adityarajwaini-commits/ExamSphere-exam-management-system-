package com.exam.view;

import com.exam.controller.AdminController;
import com.exam.controller.AuthController;
import com.exam.controller.ExamController;
import com.exam.dao.AnswerDAO;
import com.exam.dao.AttemptDAO;
import com.exam.dao.ExamDAO;
import com.exam.dao.QuestionDAO;
import com.exam.dao.ResultDAO;
import com.exam.dao.UserDAO;
import com.exam.dao.impl.AnswerDAOImpl;
import com.exam.dao.impl.AttemptDAOImpl;
import com.exam.dao.impl.ExamDAOImpl;
import com.exam.dao.impl.QuestionDAOImpl;
import com.exam.dao.impl.ResultDAOImpl;
import com.exam.dao.impl.UserDAOImpl;
import com.exam.service.AttemptService;
import com.exam.service.AuthService;
import com.exam.service.ExamService;
import com.exam.service.ResultService;
import com.exam.service.impl.AttemptServiceImpl;
import com.exam.service.impl.AuthServiceImpl;
import com.exam.service.impl.ExamServiceImpl;
import com.exam.service.impl.ResultServiceImpl;
import com.exam.util.NavigationHelper;
import com.exam.util.SimpleLogger;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private final NavigationHelper navigation;

    public MainFrame() {
        super("Online Examination System");
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        this.navigation = new NavigationHelper(mainPanel, cardLayout);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        initViews();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    private void initViews() {
        UserDAO userDAO = new UserDAOImpl();
        AuthService authService = new AuthServiceImpl(userDAO);
        try {
            boolean created = authService.ensureDefaultAdminUser();
            if (created) {
                SimpleLogger.log("INFO", "Default admin user seeded: admin@gmail.com");
            }
        } catch (SQLException ex) {
            SimpleLogger.log("WARN", "Default admin seeding skipped: " + ex.getMessage());
        }
        AuthController authController = new AuthController(authService, navigation);

        ExamDAO examDAO = new ExamDAOImpl();
        QuestionDAO questionDAO = new QuestionDAOImpl();
        AttemptDAO attemptDAO = new AttemptDAOImpl();
        AnswerDAO answerDAO = new AnswerDAOImpl();
        ResultDAO resultDAO = new ResultDAOImpl();

        ExamService examService = new ExamServiceImpl(examDAO, questionDAO);
        AttemptService attemptService = new AttemptServiceImpl(attemptDAO, answerDAO);
        ResultService resultService = new ResultServiceImpl(resultDAO);
        ExamController examController = new ExamController(examService, attemptService, resultService, navigation);
        AdminController adminController = new AdminController(examService, resultService, authService, navigation);

        SolutionReviewView solutionReviewView = new SolutionReviewView(navigation);
        ResultView resultView = new ResultView(navigation, solutionReviewView);
        ExamView examView = new ExamView(examController, navigation, resultView);
        InstructionsView instructionsView = new InstructionsView(examController, navigation, examView);
        LeaderboardView studentLeaderboard = new LeaderboardView(examController, navigation, "studentDashboard");
        StudentDashboardView studentDashboard = new StudentDashboardView(examController, navigation,
            instructionsView, studentLeaderboard);
        AdminDashboardView adminDashboard = new AdminDashboardView(adminController, examController);

        navigation.registerView("login", new LoginView(authController, studentDashboard));
        navigation.registerView("studentDashboard", studentDashboard);
        navigation.registerView("adminDashboard", adminDashboard);
        navigation.registerView("instructions", instructionsView);
        navigation.registerView("examView", examView);
        navigation.registerView("resultView", resultView);
        navigation.registerView("solutionReview", solutionReviewView);
        navigation.registerView("leaderboard", studentLeaderboard);
        navigation.show("login");
    }

    public NavigationHelper getNavigation() {
        return navigation;
    }
}
