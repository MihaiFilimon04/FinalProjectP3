//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.sql.*;
//
//public class MovieRentalAppTest {
//
//    // Test case for StartWindow constructor
//    @Test
//    void testStartWindowConstructor() {
//        SwingUtilities.invokeLater(() -> {
//            MovieRentalApp.StartWindow startWindow = new MovieRentalApp().new StartWindow();
//            assertNotNull(startWindow);
//            assertEquals("Movie Rental Application", startWindow.getTitle());
//        });
//    }
//
//    // Test case for LoginWindow constructor
//    @Test
//    void testLoginWindowConstructor() {
//        SwingUtilities.invokeLater(() -> {
//            MovieRentalApp.LoginWindow loginWindow = new MovieRentalApp().new LoginWindow();
//            assertNotNull(loginWindow);
//            assertEquals("Login", loginWindow.getTitle());
//        });
//    }
//
//    // Test case for RegisterWindow constructor
//    @Test
//    void testRegisterWindowConstructor() {
//        SwingUtilities.invokeLater(() -> {
//            MovieRentalApp.RegisterWindow registerWindow = new MovieRentalApp().new RegisterWindow();
//            assertNotNull(registerWindow);
//            assertEquals("Register", registerWindow.getTitle());
//        });
//    }
//
//    // Test case for AdminWindow constructor
//    @Test
//    void testAdminWindowConstructor() {
//        SwingUtilities.invokeLater(() -> {
//            MovieRentalApp.AdminWindow adminWindow = new MovieRentalApp().new AdminWindow();
//            assertNotNull(adminWindow);
//            assertEquals("Admin Panel", adminWindow.getTitle());
//        });
//    }
//
//    // Test case for UserWindow constructor
//    @Test
//    void testUserWindowConstructor() {
//        SwingUtilities.invokeLater(() -> {
//            MovieRentalApp.UserWindow userWindow = new MovieRentalApp().new UserWindow("testUser");
//            assertNotNull(userWindow);
//            assertEquals("User Panel", userWindow.getTitle());
//        });
//    }
//
//    // Test case for loading movies in AdminWindow
//    @Test
//    void testLoadMovies() {
//        MovieRentalApp.AdminWindow adminWindow = new MovieRentalApp().new AdminWindow();
//        assertDoesNotThrow(adminWindow::loadMovies, "Loading movies should not throw an exception");
//    }
//
//    // Test case for renting a movie in UserWindow
//    @Test
//    void testRentMovie() {
//        MovieRentalApp.UserWindow userWindow = new MovieRentalApp().new UserWindow("testUser");
//        assertDoesNotThrow(() -> userWindow.rentMovie("testUser"), "Renting a movie should not throw an exception");
//    }
//
//    // Test case for returning a movie in UserWindow
//    @Test
//    void testReturnMovie() {
//        MovieRentalApp.UserWindow userWindow = new MovieRentalApp().new UserWindow("testUser");
//        assertDoesNotThrow(() -> userWindow.returnMovie("testUser"), "Returning a movie should not throw an exception");
//    }
//
//    // Test case for viewing rent history in UserWindow
//    @Test
//    void testViewRentHistory() {
//        MovieRentalApp.UserWindow userWindow = new MovieRentalApp().new UserWindow("testUser");
//        assertDoesNotThrow(() -> userWindow.viewRentHistory("testUser"), "Viewing rent history should not throw an exception");
//    }
//
//    // Test case for viewing rent history in AdminWindow
//    @Test
//    void testViewAllRentHistory() {
//        MovieRentalApp.AdminWindow adminWindow = new MovieRentalApp().new AdminWindow();
//        assertDoesNotThrow(adminWindow::viewRentHistory, "Viewing all rent history should not throw an exception");
//    }
//}
