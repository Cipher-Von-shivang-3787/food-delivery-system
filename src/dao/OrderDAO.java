package dao;

import model.Cart;
import model.MenuItem;
import model.User;
import util.DBConnection;

import java.sql.*;
import java.util.Map;

public class OrderDAO {

    // Saves the order and its items in one transaction.
    // Returns the new order ID, or -1 if something failed.
    public int placeOrder(User user, Cart cart) {
        String orderSql = "INSERT INTO orders (user_id, total, status) VALUES (?, ?, 'PLACED')";
        String itemSql = "INSERT INTO order_items (order_id, item_id, quantity) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // start the transaction
            try {
                int orderId;
                try (PreparedStatement ps = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, user.getUserId());
                    ps.setDouble(2, cart.getTotal());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No order ID returned");
                        orderId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                    for (Map.Entry<MenuItem, Integer> e : cart.getItems().entrySet()) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, e.getKey().getItemId());
                        ps.setInt(3, e.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit(); // both inserts succeeded, so make them permanent
                return orderId;
            } catch (SQLException ex) {
                con.rollback(); // something failed, so undo everything
                ex.printStackTrace();
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}