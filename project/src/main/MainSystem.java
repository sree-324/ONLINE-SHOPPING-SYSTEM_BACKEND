
package main;

import java.util.Scanner;

public class MainSystem {
    public static void main(String[] args) {
        @SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
        System.out.println("Hey Buddy");
        System.out.println("--------ONLINE SHOPPING MART----------");
        System.out.println("Are you an admin or a user? (admin/user): ");
        String userType = scanner.nextLine();

        if ("admin".equalsIgnoreCase(userType)) {
            admin.AdminSystem.main(args); 
        } else if ("user".equalsIgnoreCase(userType)) {
            user.UserSystem.main(args);
            System.out.println("User system not implemented yet.");
        } else {
            System.out.println("Invalid user type. Exiting...");
        }
    }
}
