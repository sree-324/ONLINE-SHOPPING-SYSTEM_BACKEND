package user;

import java.sql.*;
import java.util.Scanner;

class Print {
    void test() {
        System.out.println("Welcome to the mart!!!");
    }
}
class PrintStatement extends Print {
    void shop() {
        System.out.println("Purchase what you want and feel at your ease");
    }
}
public class UserSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pricedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";

    public static void main(String[] args) {
        PrintStatement d = new PrintStatement();
        d.test();
        d.shop();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database.");

            Scanner scanner = new Scanner(System.in);
            int userId = -1;

            System.out.println("Choose an option:");
            System.out.println("1. Login");
            System.out.println("2. Sign up");
            int option = scanner.nextInt();

            if (option == 1) {
                userId = login(scanner, connection);
            } else if (option == 2) {
                userId = signUp(scanner, connection);
            } else {
                System.out.println("Invalid option. Exiting the program.");
                scanner.close();
                connection.close();
                return;
            }

            if (userId != -1) {
                boolean continueShopping = true;
                while (continueShopping) {
                    displayAllProducts(connection);

                    System.out.println("Enter the product ID to add it to the cart (0 to exit): ");
                    int productId = scanner.nextInt();

                    if (productId == 0) {
                        continueShopping = false;
                    } else {
                        addProductToCart(connection, userId, productId);
                        System.out.println("Product added to the cart.");
                    }
                }

                double totalCartPrice = calculateTotalCartPrice(connection, userId);
                System.out.println("Total Cart Price: $" + totalCartPrice);

                if (simulatePayment()) {
                    System.out.println("Payment successful. ");
                    System.out.println("Order  has been placed.");
                    System.out.println("\n");
                } else {
                    System.out.println("Payment failed. Please try again later.");
                }

                scanner.close();
            }

            connection.close();
            System.out.println("-----------THANK YOU FOR THE SHOPPING-------------");
            System.out.println("Disconnected from the database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    

    private static int login(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("Please enter your username: ");
        String username = scanner.next();

        System.out.println("Please enter your password: ");
        String password = scanner.next();

        String query = "SELECT user_id FROM users WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);

        ResultSet resultSet = preparedStatement.executeQuery();
        int userId = -1;
        if (resultSet.next()) {
            userId = resultSet.getInt("user_id");
            System.out.println("Login successful. Welcome, User ID " + userId);
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
        resultSet.close();
        preparedStatement.close();
        return userId;
    }

    private static int signUp(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("Please enter a username: ");
        String username = scanner.next();

        System.out.println("Please enter a password: ");
        String password = scanner.next();
        
        System.out.println("Please enter a fullname: ");
        String full_name = scanner.next();

        String query = "INSERT INTO users (username, password,full_name) VALUES (?, ?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3,full_name);

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                System.out.println("Sign up successful. Welcome, User ID " + userId);
                return userId;
            }
        }

        System.out.println("Sign up failed. Please try again.");
        return -1;
    }

    private static void displayAllProducts(Connection connection) throws SQLException {
        String query = "SELECT * FROM products";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        System.out.println("Products:");
        while (resultSet.next()) {
            int productId = resultSet.getInt("product_id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            double price = resultSet.getDouble("price");
            int quantity = resultSet.getInt("quantity"); // Get the quantity from the result set

            System.out.println(productId + " | " + name + " | " + description + " | $" + price + " | Quantity: " + quantity);
        }
        resultSet.close();
        statement.close();
    }

    private static void addProductToCart(Connection connection, int userId, int productId) throws SQLException {
    	String checkQuantityQuery = "SELECT quantity FROM products WHERE product_id = ?";
        PreparedStatement checkQuantityStatement = connection.prepareStatement(checkQuantityQuery);
        checkQuantityStatement.setInt(1, productId);
        ResultSet quantityResultSet = checkQuantityStatement.executeQuery();

        if (quantityResultSet.next()) {
            int productQuantity = quantityResultSet.getInt("quantity");
            if (productQuantity <= 0) {
                System.out.println("Product is out of stock. Cannot add to cart.");
                quantityResultSet.close();
                checkQuantityStatement.close();
                return;
            }
        } else {
            System.out.println("Product with ID " + productId + " not found.");
            quantityResultSet.close();
            checkQuantityStatement.close();
            return;
        }

        quantityResultSet.close();
        checkQuantityStatement.close();

        String query = "INSERT INTO cart (user_id, product_id) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, productId);

        preparedStatement.executeUpdate();
        preparedStatement.close();
        String updateQuantityQuery = "UPDATE products SET quantity = quantity - 1 WHERE product_id = ?";
        PreparedStatement updateQuantityStatement = connection.prepareStatement(updateQuantityQuery);
        updateQuantityStatement.setInt(1, productId);
        updateQuantityStatement.executeUpdate();
        updateQuantityStatement.close();

        System.out.println("Product added to the cart.");
    }

    private static double calculateTotalCartPrice(Connection connection, int userId) throws SQLException {
        String query = "SELECT SUM(products.price) AS total_cart_price " +
                       "FROM products " +
                       "JOIN cart ON products.product_id = cart.product_id " +
                       "WHERE cart.user_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userId);

        ResultSet resultSet = preparedStatement.executeQuery();
        double totalCartPrice = 0;
        if (resultSet.next()) {
            totalCartPrice = resultSet.getDouble("total_cart_price");
        }
        resultSet.close();
        preparedStatement.close();
        return totalCartPrice;
    }

    private static boolean simulatePayment() {
        
        return true;
    }
}

