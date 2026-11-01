package ui;

import dao.OrderDAO;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyOrdersDialog extends JDialog {
    private final OrderDAO orderDAO = new OrderDAO();
    private final User user;
    private final DefaultTableModel model;
    private final Thread refresher;

    public MyOrdersDialog(JFrame parent, User user) {
        super(parent, "My Orders", false); // false = does not block the main window
        this.user = user;
        setSize(600, 350);
        setLocationRelativeTo(parent);

        model = new DefaultTableModel(new String[]{"Order #", "Status", "Total (Rs.)", "Placed at"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadOrders();

        // Background thread: re-reads the orders every 3 seconds
        refresher = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(3000);
                    List<Object[]> rows = orderDAO.getOrdersForUser(user.getUserId());
                    // Swing must only be touched on the UI thread
                    SwingUtilities.invokeLater(() -> showRows(rows));
                }
            } catch (InterruptedException e) {
                // dialog closed, so the thread stops
            }
        });
        refresher.setDaemon(true);
        refresher.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                refresher.interrupt();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadOrders() {
        showRows(orderDAO.getOrdersForUser(user.getUserId()));
    }

    private void showRows(List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] r : rows) model.addRow(r);
    }
}