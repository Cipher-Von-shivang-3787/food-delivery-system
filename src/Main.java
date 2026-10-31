import util.DBConnection;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, price FROM menu_items")) {
            System.out.println("Connected!");
            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " + rs.getDouble("price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}