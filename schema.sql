CREATE DATABASE IF NOT EXISTS food_delivery;
USE food_delivery;

CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('CUSTOMER','ADMIN','DELIVERY') DEFAULT 'CUSTOMER',
  address VARCHAR(255)
);

CREATE TABLE restaurants (
  restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  location VARCHAR(150)
);

CREATE TABLE menu_items (
  item_id INT AUTO_INCREMENT PRIMARY KEY,
  restaurant_id INT,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(8,2) NOT NULL,
  available BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
);

CREATE TABLE orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  delivery_id INT NULL,
  status ENUM('PLACED','PREPARING','OUT_FOR_DELIVERY','DELIVERED') DEFAULT 'PLACED',
  total DECIMAL(10,2),
  order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (delivery_id) REFERENCES users(user_id)
);

CREATE TABLE order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  item_id INT NOT NULL,
  quantity INT NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(order_id),
  FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

INSERT INTO restaurants (name, location) VALUES
 ('Spice Hub', 'Main Market'),
 ('Pizza Corner', 'City Center');

INSERT INTO menu_items (restaurant_id, name, price) VALUES
 (1, 'Paneer Butter Masala', 220.00),
 (1, 'Butter Naan', 40.00),
 (2, 'Margherita Pizza', 250.00),
 (2, 'Garlic Bread', 120.00);

INSERT INTO users (name, email, password, role) VALUES
 ('Admin', 'admin@food.com', 'admin123', 'ADMIN'),
 ('Ravi Rider', 'ravi@food.com', 'ravi123', 'DELIVERY');