package dao;

import model.MenuItem;
import util.DBConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    public List<MenuItem> getAllItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT m.item_id, m.name, m.price, r.name AS restaurant "
                + "FROM menu_items m JOIN restaurants r ON m.restaurant_id = r.restaurant_id "
                + "WHERE m.available = TRUE ORDER BY r.name, m.name";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("restaurant")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Each row: {id, restaurant, name, price, available}. Includes hidden items.
    public List<Object[]> getAllItemsForAdmin() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT m.item_id, r.name AS restaurant, m.name, m.price, m.available "
                + "FROM menu_items m JOIN restaurants r ON m.restaurant_id = r.restaurant_id "
                + "ORDER BY r.name, m.name";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[] {
                        rs.getInt("item_id"),
                        rs.getString("restaurant"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getBoolean("available") ? "Yes" : "No"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public Map<Integer, String> getRestaurants() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT restaurant_id, name FROM restaurants ORDER BY name";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                map.put(rs.getInt("restaurant_id"), rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean addItem(int restaurantId, String name, double price) {
        String sql = "INSERT INTO menu_items (restaurant_id, name, price) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, name);
            ps.setDouble(3, price);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateItem(int itemId, String name, double price) {
        String sql = "UPDATE menu_items SET name = ?, price = ? WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, itemId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setAvailable(int itemId, boolean available) {
        String sql = "UPDATE menu_items SET available = ? WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, itemId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}