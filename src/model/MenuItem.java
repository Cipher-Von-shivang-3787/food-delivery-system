package model;

import java.util.Objects;

public class MenuItem {
    private final int itemId;
    private final String name;
    private final double price;
    private final String restaurantName;

    public MenuItem(int itemId, String name, double price, String restaurantName) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.restaurantName = restaurantName;
    }

    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getRestaurantName() { return restaurantName; }

    // Needed because MenuItem will be a HashMap key in the cart
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem)) return false;
        return itemId == ((MenuItem) o).itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
}