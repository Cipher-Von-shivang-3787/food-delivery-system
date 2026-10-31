package model;

public class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String role;
    private String address;

    public User(int userId, String name, String email, String password, String role, String address) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.address = address;
    }


    public User(String name, String email, String password, String address) {
        this(0, name, email, password, "CUSTOMER", address);
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
}