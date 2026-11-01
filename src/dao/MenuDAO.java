package dao;

import model.MenuItem;
import util.DBConnection;

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
}