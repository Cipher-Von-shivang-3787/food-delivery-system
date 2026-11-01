package dao;

import model.Cart;
import model.MenuItem;
import model.User;
import util.DBConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
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
                        if (!keys.next())
                            throw new SQLException("No order ID returned");
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

    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Each row: {orderId, status, total, time}
    public List<Object[]> getOrdersForUser(int userId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT order_id, status, total, order_time FROM orders "
                + "WHERE user_id = ? ORDER BY order_id DESC";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] {
                            rs.getInt("order_id"),
                            rs.getString("status"),
                            String.format("%.2f", rs.getDouble("total")),
                            rs.getTimestamp("order_time").toString()
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
        // Each row: {orderId, customer, status, total, time, deliveryPerson}
    public List<Object[]> getAllOrders() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT o.order_id, u.name AS customer, o.status, o.total, o.order_time, "
                   + "d.name AS delivery "
                   + "FROM orders o JOIN users u ON o.user_id = u.user_id "
                   + "LEFT JOIN users d ON o.delivery_id = d.user_id "
                   + "ORDER BY o.order_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String delivery = rs.getString("delivery");
                rows.add(new Object[]{
                    rs.getInt("order_id"),
                    rs.getString("customer"),
                    rs.getString("status"),
                    String.format("%.2f", rs.getDouble("total")),
                    rs.getTimestamp("order_time").toString(),
                    delivery == null ? "Unassigned" : delivery
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
        public Map<Integer, String> getDeliveryPeople() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT user_id, name FROM users WHERE role = 'DELIVERY' ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getInt("user_id"), rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // Only works on orders still in PLACED status
    public boolean assignOrder(int orderId, int deliveryId) {
        String sql = "UPDATE orders SET delivery_id = ?, status = 'PREPARING' "
                   + "WHERE order_id = ? AND status = 'PLACED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deliveryId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Each row: {orderId, customer, address, status, total}
    public List<Object[]> getOrdersForDelivery(int deliveryId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT o.order_id, u.name, u.address, o.status, o.total "
                   + "FROM orders o JOIN users u ON o.user_id = u.user_id "
                   + "WHERE o.delivery_id = ? ORDER BY o.order_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deliveryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String address = rs.getString("address");
                    rows.add(new Object[]{
                        rs.getInt("order_id"),
                        rs.getString("name"),
                        address == null ? "-" : address,
                        rs.getString("status"),
                        String.format("%.2f", rs.getDouble("total"))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}