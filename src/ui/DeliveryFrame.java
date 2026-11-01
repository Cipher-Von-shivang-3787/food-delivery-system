package ui;

import dao.OrderDAO;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeliveryFrame extends JFrame {
    private final OrderDAO orderDAO = new OrderDAO();
    private final User rider;
    private final DefaultTableModel model;
    private final JTable table;

    public DeliveryFrame(User rider) {
        this.rider = rider;
        setTitle("Food Delivery - Delivery Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 450);
        setLocationRelativeTo(null);

        JLabel welcome = new JLabel("Rider: " + rider.getName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        top.add(welcome, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        model = new DefaultTableModel(
                new String[]{"Order #", "Customer", "Address", "Status", "Total (Rs.)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton outBtn = new JButton("Mark Out for Delivery");
        JButton doneBtn = new JButton("Mark Delivered");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(outBtn);
        buttons.add(doneBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        outBtn.addActionListener(e -> changeStatus("PREPARING", "OUT_FOR_DELIVERY"));
        doneBtn.addActionListener(e -> changeStatus("OUT_FOR_DELIVERY", "DELIVERED"));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        showRows(orderDAO.getOrdersForDelivery(rider.getUserId()));
        startRefresher();
    }

    // Moves the selected order from one status to the next, only if the current status is right
    private void changeStatus(String requiredCurrent, String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String current = model.getValueAt(row, 3).toString();
        if (!current.equals(requiredCurrent)) {
            JOptionPane.showMessageDialog(this,
                    "This order is " + current + ". It must be " + requiredCurrent + " first.",
                    "Wrong status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int orderId = (int) model.getValueAt(row, 0);
        orderDAO.updateStatus(orderId, newStatus);
        showRows(orderDAO.getOrdersForDelivery(rider.getUserId()));
    }

    // Rebuilds the table but keeps the selected order selected
    private void showRows(List<Object[]> rows) {
        int selectedId = -1;
        int sel = table.getSelectedRow();
        if (sel != -1) selectedId = (int) model.getValueAt(sel, 0);

        model.setRowCount(0);
        int reselect = -1;
        for (int i = 0; i < rows.size(); i++) {
            model.addRow(rows.get(i));
            if ((int) rows.get(i)[0] == selectedId) reselect = i;
        }
        if (reselect != -1) table.setRowSelectionInterval(reselect, reselect);
    }

    private void startRefresher() {
        Thread t = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(3000);
                    List<Object[]> rows = orderDAO.getOrdersForDelivery(rider.getUserId());
                    SwingUtilities.invokeLater(() -> showRows(rows));
                }
            } catch (InterruptedException e) {
                // stop quietly
            }
        });
        t.setDaemon(true);
        t.start();
    }
}