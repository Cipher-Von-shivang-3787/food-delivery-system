package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField addressField = new JTextField(20);
    private final UserDAO userDAO = new UserDAO();

    public RegisterDialog(JFrame parent) {
        super(parent, "Register", true); // true = modal (blocks the parent until closed)
        setSize(400, 300);
        setLocationRelativeTo(parent);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.add(new JLabel("Name:"));     form.add(nameField);
        form.add(new JLabel("Email:"));    form.add(emailField);
        form.add(new JLabel("Password:")); form.add(passwordField);
        form.add(new JLabel("Address:"));  form.add(addressField);

        JButton submit = new JButton("Create Account");
        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(submit);

        JPanel root = new JPanel(new BorderLayout(10, 20));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        root.add(form, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        add(root);

        submit.addActionListener(e -> handleRegister());
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String address = addressField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.",
                    "Invalid email", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.",
                    "Weak password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = userDAO.register(new User(name, email, password, address));
        if (ok) {
            JOptionPane.showMessageDialog(this, "Account created! You can log in now.");
            dispose(); // close this dialog
        } else {
            JOptionPane.showMessageDialog(this, "That email is already registered.",
                    "Registration failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}