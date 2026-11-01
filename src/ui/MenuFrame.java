package ui;

import dao.MenuDAO;
import model.Cart;
import model.MenuItem;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuFrame extends JFrame {
    private final User user;
    private final MenuDAO menuDAO = new MenuDAO();
    private final Cart cart = new Cart();

    private final DefaultTableModel menuModel;
    private final JTable menuTable;
    private List<MenuItem> menuItems = new ArrayList<>();

    private final DefaultTableModel cartModel;
    private final JTable cartTable;
    private final List<MenuItem> cartRows = new ArrayList<>(); // row index -> item
    private final JLabel totalLabel = new JLabel("Total: Rs. 0.00");

    public MenuFrame(User user) {
        this.user = user;
        setTitle("Food Delivery - Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 500);
        setLocationRelativeTo(null);

        // ---- Top bar ----
        JLabel welcome = new JLabel("Welcome, " + user.getName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        top.add(welcome, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // ---- Left: menu table + Add button ----
        menuModel = new DefaultTableModel(new String[]{"ID", "Restaurant", "Item", "Price (Rs.)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(26);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addBtn = new JButton("Add to Cart");
        JPanel menuButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtons.add(addBtn);

        JPanel left = new JPanel(new BorderLayout());
        left.setBorder(BorderFactory.createTitledBorder("Menu"));
        left.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        left.add(menuButtons, BorderLayout.SOUTH);

        // ---- Right: cart table + total + buttons ----
        cartModel = new DefaultTableModel(new String[]{"Item", "Qty", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(26);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton removeBtn = new JButton("Remove One");
        JButton clearBtn = new JButton("Clear Cart");
        JButton orderBtn = new JButton("Place Order");

        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cartButtons.add(removeBtn);
        cartButtons.add(clearBtn);
        cartButtons.add(orderBtn);

        JPanel cartBottom = new JPanel(new GridLayout(2, 1));
        cartBottom.add(totalLabel);
        cartBottom.add(cartButtons);

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Your Cart"));
        right.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        right.add(cartBottom, BorderLayout.SOUTH);

        // ---- Split the window into left and right ----
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.55);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        // ---- Button actions ----
        addBtn.addActionListener(e -> addSelectedToCart());
        removeBtn.addActionListener(e -> removeSelectedFromCart());
        clearBtn.addActionListener(e -> {
            cart.clear();
            refreshCart();
        });
        orderBtn.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your cart is empty.",
                        "Nothing to order", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Order placement is the next step
            JOptionPane.showMessageDialog(this,
                    String.format("Order placement comes next. Total: Rs. %.2f", cart.getTotal()));
        });
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        loadMenu();
    }

    private void loadMenu() {
        menuModel.setRowCount(0);
        menuItems = menuDAO.getAllItems();
        for (MenuItem item : menuItems) {
            menuModel.addRow(new Object[]{
                item.getItemId(), item.getRestaurantName(), item.getName(), item.getPrice()
            });
        }
    }

    private void addSelectedToCart() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a dish from the menu first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cart.add(menuItems.get(row)); // row index matches list index
        refreshCart();
    }

    private void removeSelectedFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item in the cart first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cart.removeOne(cartRows.get(row));
        refreshCart();
    }

    // Rebuilds the cart table from the Cart object
    private void refreshCart() {
        cartModel.setRowCount(0);
        cartRows.clear();
        for (Map.Entry<MenuItem, Integer> e : cart.getItems().entrySet()) {
            MenuItem item = e.getKey();
            int qty = e.getValue();
            cartRows.add(item);
            cartModel.addRow(new Object[]{
                item.getName(), qty, String.format("%.2f", item.getPrice() * qty)
            });
        }
        totalLabel.setText(String.format("Total: Rs. %.2f", cart.getTotal()));
    }
}