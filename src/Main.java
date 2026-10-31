import dao.UserDAO;
import model.User;

public class Main {
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();

        boolean ok = dao.register(new User("Test User", "test@food.com", "pass123", "12 MG Road"));
        System.out.println("Register: " + ok);

        User u = dao.login("test@food.com", "pass123");
        System.out.println("Correct login: " + (u != null ? "Welcome " + u.getName() + " (" + u.getRole() + ")" : "failed"));

        User bad = dao.login("test@food.com", "wrongpass");
        System.out.println("Wrong password: " + (bad == null ? "rejected (correct)" : "BUG"));

        User admin = dao.login("admin@food.com", "admin123");
        System.out.println("Admin login: " + (admin != null ? admin.getRole() : "failed"));
    }
}