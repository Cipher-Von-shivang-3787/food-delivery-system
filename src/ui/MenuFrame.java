package ui;

import dao.MenuDAO;
import model.MenuItem;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MenuFrame extends JFrame {
    private final User user;
    private final MenuDAO menuDAO = new MenuDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<MenuItem> menuItems;

    public MenuFrame(User user) {
        this.user = user;
        setTitle("Food Delivery - Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);

        JLabel welcome = new JLabel("Welcome, " + user.getName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        top.add(welcome, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // Table that users cannot edit by typing in cells
        tableModel = new DefaultTableModel(new String[]{"ID", "Restaurant", "Item", "Price (Rs.)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        loadMenu();
    }

    private void loadMenu() {
        tableModel.setRowCount(0); // clear old rows
        menuItems = menuDAO.getAllItems();
        for (MenuItem item : menuItems) {
            tableModel.addRow(new Object[]{
                item.getItemId(),
                item.getRestaurantName(),
                item.getName(),
                item.getPrice()
            });
        }
    }
}