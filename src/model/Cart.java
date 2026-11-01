package model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    // Key = the dish, Value = quantity.
    // LinkedHashMap is a HashMap that remembers insertion order, so rows don't jump around.
    private final Map<MenuItem, Integer> items = new LinkedHashMap<>();

    public void add(MenuItem item) {
        items.merge(item, 1, Integer::sum); // if present: quantity + 1, else: put 1
    }

    public void removeOne(MenuItem item) {
        Integer qty = items.get(item);
        if (qty == null) return;
        if (qty <= 1) {
            items.remove(item);
        } else {
            items.put(item, qty - 1);
        }
    }

    public void clear() {
        items.clear();
    }

    public double getTotal() {
        double total = 0;
        for (Map.Entry<MenuItem, Integer> e : items.entrySet()) {
            total += e.getKey().getPrice() * e.getValue();
        }
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Read-only view so other classes can't modify the cart directly
    public Map<MenuItem, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }
}