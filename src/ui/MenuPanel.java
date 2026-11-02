package ui;

import dao.MenuDAO;
import model.MenuItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Online Food Delivery - Menu", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Restaurant", "Item", "Price (Rs.)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (MenuItem item : new MenuDAO().getAllItems()) {
            model.addRow(new Object[]{item.getRestaurantName(), item.getName(), item.getPrice()});
        }
        JTable table = new JTable(model);
        table.setRowHeight(26);

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}