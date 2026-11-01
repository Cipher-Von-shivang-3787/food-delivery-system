package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Food Delivery - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 280);
        setLocationRelativeTo(null); // center on screen

        JLabel title = new JLabel("Online Food Delivery", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(loginBtn);
        buttons.add(registerBtn);

        JPanel root = new JPanel(new BorderLayout(10, 20));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        add(root);

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> new RegisterDialog(this).setVisible(true));
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.login(email, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid email or password.",
                    "Login failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        switch (user.getRole()) {
            case "CUSTOMER":
                new MenuFrame(user).setVisible(true);
                break;
            case "ADMIN":
                new AdminFrame(user).setVisible(true);
                break;
            case "DELIVERY":
                new DeliveryFrame(user).setVisible(true);
                break;
            default:
                new LoginFrame().setVisible(true);
        }
    }
}