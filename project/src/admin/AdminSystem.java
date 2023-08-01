package admin;

import java.sql.*;
import java.util.Scanner;
abstract class AbstractAdminSystem {
    abstract boolean isAdminLogin(Scanner scanner, Connection connection) throws SQLException;

}
class MySQLDatabase {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pricedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";
    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}

public class AdminSystem extends AbstractAdminSystem {

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = MySQLDatabase.getConnection();
            System.out.println("Connected to the database.");

            Scanner scanner = new Scanner(System.in);
            AdminSystem adminSystem = new AdminSystem();

            if (adminSystem.isAdminLogin(scanner, connection)) {
                System.out.println("Admin login successful.");

                int option;
                do {
                    // Display admin options menu
                    System.out.println("Select an option:");
                    System.out.println("1. Add a product");
                    System.out.println("2. View products");
                    System.out.println("0. Exit");
                    option = scanner.nextInt();

                    switch (option) {
                        case 1:
                            adminSystem.addProductMenu(scanner, connection);
                            break;
                        case 2:
                            adminSystem.viewProducts(connection);
                            break;
                        case 0:
                            System.out.println("Exiting...");
                            break;
                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                } while (option != 0);
            } else {
                System.out.println("Invalid admin credentials. Exiting...");
            }            connection.close();
            System.out.println("Disconnected from the database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    boolean isAdminLogin(Scanner scanner, Connection connection) throws SQLException {

        System.out.println("Please enter your admin username: ");
        String adminUsername = scanner.nextLine();

        System.out.println("Please enter your admin password: ");
        String adminPassword = scanner.nextLine();

        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, adminUsername);
        preparedStatement.setString(2, adminPassword);

        ResultSet resultSet = preparedStatement.executeQuery();
        boolean isAdmin = resultSet.next();
        resultSet.close();
        preparedStatement.close();
        return isAdmin;
    }

    private void addProductMenu(Scanner scanner, Connection connection) {
        System.out.println("Enter product details to add (name, description, price, quantity): ");
        scanner.nextLine(); 
        String name = scanner.nextLine();
        String description = scanner.nextLine();
        double price = scanner.nextDouble();
        int quantity = scanner.nextInt();

        try {
            addProduct(connection, name, description, price, quantity);
            displayAddedProduct(name, description, price, quantity);
        } catch (SQLException e) {
            System.out.println("Failed to add the product. Error: " + e.getMessage());
        }
    }

    private void viewProducts(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM products";
            ResultSet resultSet = statement.executeQuery(query);

            System.out.println("Products in the database:");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");

                System.out.println("Name: " + name);
                System.out.println("Description: " + description);
                System.out.println("Price: $" + price);
                System.out.println("Quantity: " + quantity);
                System.out.println();
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Error fetching products. Error: " + e.getMessage());
        }
    }

    private void addProduct(Connection connection, String name, String description, double price, int quantity) throws SQLException {
        String query = "INSERT INTO products (name, description, price,quantity) VALUES (?, ?, ?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, description);
        preparedStatement.setDouble(3, price);
        preparedStatement.setInt(4, quantity);

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Product added successfully.");
        } else {
            System.out.println("Failed to add the product.");
        }
        preparedStatement.close();
    }

    private void displayAddedProduct(String name, String description, double price, int quantity) {
        System.out.println("Added Product in Cart:");
        System.out.println("Name: " + name);
        System.out.println("Description: " + description);
        System.out.println("Price: $" + price);
        System.out.println("Quantity: " + quantity);
        System.out.println();
    }
}
