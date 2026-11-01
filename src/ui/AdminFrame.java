package ui;

import dao.MenuDAO;
import dao.OrderDAO;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminFrame extends JFrame {
    private final MenuDAO menuDAO = new MenuDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final DefaultTableModel menuModel;
    private final JTable menuTable;
    private final DefaultTableModel orderModel;
    private final JTable orderTable;
    private final JComboBox<String> riderBox = new JComboBox<>();
    private final List<Integer> riderIds = new ArrayList<>();

    private final JComboBox<String> restaurantBox = new JComboBox<>();
    private final List<Integer> restaurantIds = new ArrayList<>(); // same order as the combo box
    private final JTextField nameField = new JTextField(15);
    private final JTextField priceField = new JTextField(6);

    public AdminFrame(User admin) {
        setTitle("Food Delivery - Admin Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);

        // ---- Top bar ----
        JLabel welcome = new JLabel("Admin: " + admin.getName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        top.add(welcome, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // ---- Tab 1: Manage Menu ----
        menuModel = new DefaultTableModel(
                new String[] { "ID", "Restaurant", "Item", "Price (Rs.)", "Available" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(26);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        loadRestaurants();

        JButton addBtn = new JButton("Add Item");
        JButton updateBtn = new JButton("Update Selected");
        JButton toggleBtn = new JButton("Remove / Restore");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Add new item, or select a row to edit"));
        form.add(new JLabel("Restaurant:"));
        form.add(restaurantBox);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Price:"));
        form.add(priceField);
        form.add(addBtn);
        form.add(updateBtn);
        form.add(toggleBtn);

        JPanel menuTab = new JPanel(new BorderLayout());
        menuTab.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        menuTab.add(form, BorderLayout.SOUTH);

                // ---- Tab 2: All Orders ----
        orderModel = new DefaultTableModel(
                new String[]{"Order #", "Customer", "Status", "Total (Rs.)", "Placed at", "Delivery"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderTable = new JTable(orderModel);
        orderTable.setRowHeight(26);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        loadRiders();
        JButton assignBtn = new JButton("Assign to Rider");
        JPanel assignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assignPanel.setBorder(BorderFactory.createTitledBorder("Select a PLACED order, choose a rider"));
        assignPanel.add(new JLabel("Rider:"));
        assignPanel.add(riderBox);
        assignPanel.add(assignBtn);
        assignBtn.addActionListener(e -> assignSelectedOrder());

        JPanel ordersTab = new JPanel(new BorderLayout());
        ordersTab.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        ordersTab.add(assignPanel, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Menu", menuTab);
        tabs.addTab("All Orders", ordersTab);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // ---- Actions ----
        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateItem());
        toggleBtn.addActionListener(e -> toggleAvailability());
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        // When a row is selected, copy its name and price into the form
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                fillFormFromSelection();
        });

        loadMenu();
        showOrders(orderDAO.getAllOrders());
        startOrderRefresher();
    }

    // ---------- Loading data ----------
    private void loadRestaurants() {
        for (Map.Entry<Integer, String> e : menuDAO.getRestaurants().entrySet()) {
            restaurantIds.add(e.getKey());
            restaurantBox.addItem(e.getValue());
        }
    }

    private void loadMenu() {
        menuModel.setRowCount(0);
        for (Object[] row : menuDAO.getAllItemsForAdmin())
            menuModel.addRow(row);
    }

        private void showOrders(List<Object[]> rows) {
        int selectedId = -1;
        int sel = orderTable.getSelectedRow();
        if (sel != -1) selectedId = (int) orderModel.getValueAt(sel, 0);

        orderModel.setRowCount(0);
        int reselect = -1;
        for (int i = 0; i < rows.size(); i++) {
            orderModel.addRow(rows.get(i));
            if ((int) rows.get(i)[0] == selectedId) reselect = i;
        }
        if (reselect != -1) orderTable.setRowSelectionInterval(reselect, reselect);
    }

    // Background thread: refreshes the orders list every 3 seconds
    private void startOrderRefresher() {
        Thread t = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(3000);
                    List<Object[]> rows = orderDAO.getAllOrders();
                    SwingUtilities.invokeLater(() -> showOrders(rows));
                }
            } catch (InterruptedException e) {
                // stop quietly
            }
        });
        t.setDaemon(true);
        t.start();
    }
        private void loadRiders() {
        for (Map.Entry<Integer, String> e : orderDAO.getDeliveryPeople().entrySet()) {
            riderIds.add(e.getKey());
            riderBox.addItem(e.getValue());
        }
    }

    private void assignSelectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row == -1 || riderIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an order and make sure a rider exists.",
                    "Cannot assign", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String status = orderModel.getValueAt(row, 2).toString();
        if (!status.equals("PLACED")) {
            JOptionPane.showMessageDialog(this, "Only PLACED orders can be assigned.",
                    "Cannot assign", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int orderId = (int) orderModel.getValueAt(row, 0);
        int riderId = riderIds.get(riderBox.getSelectedIndex());
        if (orderDAO.assignOrder(orderId, riderId)) {
            showOrders(orderDAO.getAllOrders());
        } else {
            JOptionPane.showMessageDialog(this, "Could not assign the order.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    } 
    // ---------- Menu actions ----------
    private void fillFormFromSelection() {
        int row = menuTable.getSelectedRow();
        if (row == -1)
            return;
        nameField.setText(menuModel.getValueAt(row, 2).toString());
        priceField.setText(menuModel.getValueAt(row, 3).toString());
    }

    // Returns the price, or -1 if the input is invalid (and shows a message)
    private double readPrice() {
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0)
                throw new NumberFormatException();
            return price;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid price greater than 0.",
                    "Invalid price", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
    }

    private void addItem() {
        String name = nameField.getText().trim();
        if (name.isEmpty() || restaurantIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter an item name.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double price = readPrice();
        if (price < 0)
            return;

        int restaurantId = restaurantIds.get(restaurantBox.getSelectedIndex());
        if (menuDAO.addItem(restaurantId, name, price)) {
            nameField.setText("");
            priceField.setText("");
            loadMenu();
        } else {
            JOptionPane.showMessageDialog(this, "Could not add the item.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItem() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item in the table first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item name cannot be empty.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double price = readPrice();
        if (price < 0)
            return;

        int itemId = (int) menuModel.getValueAt(row, 0);
        if (menuDAO.updateItem(itemId, name, price)) {
            loadMenu();
        } else {
            JOptionPane.showMessageDialog(this, "Could not update the item.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleAvailability() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item in the table first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int itemId = (int) menuModel.getValueAt(row, 0);
        boolean currentlyAvailable = "Yes".equals(menuModel.getValueAt(row, 4));
        menuDAO.setAvailable(itemId, !currentlyAvailable);
        loadMenu();
    }
}