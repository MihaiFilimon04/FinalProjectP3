import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class MovieRentalApp {
    // JDBC Connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/movierental";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MovieRentalApp().new StartWindow()); //initiates the app and start window
    }

    // StartWindow Class
    class StartWindow extends JFrame {
        public StartWindow() {
            setTitle("Movie Rental Application");
            setSize(400, 200);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new GridLayout(2, 1));

            JButton loginButton = new JButton("Login");
            JButton registerButton = new JButton("Register");

            loginButton.addActionListener(e -> {
                dispose(); // closes the tab
                new LoginWindow(); // constructor for obv
            });

            registerButton.addActionListener(e -> {
                dispose();
                new RegisterWindow();
            });

            add(loginButton);
            add(registerButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    // LoginWindow Class
    class LoginWindow extends JFrame {
        public LoginWindow() {
            setTitle("Login");
            setSize(400, 200);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new GridLayout(3, 2));

            JLabel usernameLabel = new JLabel("Username:");
            JTextField usernameField = new JTextField();
            JLabel passwordLabel = new JLabel("Password:");
            JPasswordField passwordField = new JPasswordField();
            JButton loginButton = new JButton("Login");

            loginButton.addActionListener(e -> { // it awaits user input, click in this case
                String username = usernameField.getText();
                if(username == null || username.equals("")) {
                    JOptionPane.showMessageDialog(this, "Username is empty", "Error", JOptionPane.ERROR_MESSAGE);
                }else {
                    String password = new String(passwordField.getPassword());
                    if(password == null || password.equals("")) {
                        JOptionPane.showMessageDialog(this, "Password is empty", "Error", JOptionPane.ERROR_MESSAGE);
                    }else{
                        authenticateUser(username, password);
                    }
                }


            });

            add(usernameLabel);
            add(usernameField);
            add(passwordLabel);
            add(passwordField);
            add(new JLabel());
            add(loginButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void authenticateUser(String username, String password) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    boolean isAdmin = rs.getBoolean("is_admin");
                    dispose();
                    if (isAdmin) {
                        new AdminWindow();
                    } else {
                        new UserWindow(username);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // RegisterWindow Class
    class RegisterWindow extends JFrame {
        public RegisterWindow() {
            setTitle("Register");
            setSize(400, 300);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new GridLayout(4, 2));

            JLabel usernameLabel = new JLabel("Username:");
            JTextField usernameField = new JTextField();
            JLabel passwordLabel = new JLabel("Password:");
            JPasswordField passwordField = new JPasswordField();
            JButton registerButton = new JButton("Register");

            registerButton.addActionListener(e -> {
                String username = usernameField.getText();
                if (username == null || username.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Username is empty", "Error", JOptionPane.ERROR_MESSAGE);
                }else {
                    String password = new String(passwordField.getPassword());
                    if (password == null || password.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Password is empty", "Error", JOptionPane.ERROR_MESSAGE);
                    }else{
                        registerUser(username, password);
                    }
                }
            });



            add(usernameLabel);
            add(usernameField);
            add(passwordLabel);
            add(passwordField);
            add(new JLabel());
            add(registerButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void registerUser(String username, String password) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, is_admin) VALUES (?, ?, 0)")) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginWindow();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error registering user", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // AdminWindow Class
    class AdminWindow extends JFrame {
        private JTable movieTable;
        private DefaultTableModel tableModel;

        public AdminWindow() {
            setTitle("Admin Panel");
            setSize(800, 600);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Table setup
            tableModel = new DefaultTableModel(new String[]{"Movie ID", "Title", "Genre", "Release Date", "Available Copies"}, 0);
            movieTable = new JTable(tableModel);
            movieTable.setEnabled(false);
            JScrollPane scrollPane = new JScrollPane(movieTable);
            add(scrollPane, BorderLayout.CENTER);

            // Buttons
            JPanel buttonPanel = new JPanel();
            JButton loadMoviesButton = new JButton("Load Movies");
            JButton addMovieButton = new JButton("Add Movie");
            JButton editMovieButton = new JButton("Edit Movie");
            JButton deleteMovieButton = new JButton("Delete Movie");
            JButton viewHistoryButton = new JButton("View Rent History");
            JButton backButton = new JButton("Back to Start Page");

            loadMoviesButton.addActionListener(e -> {
                Thread backgroundThread = new Thread(this::loadMovies);
                backgroundThread.start(); // Load movies in a background thread
            });

            addMovieButton.addActionListener(e -> new AddMovieWindow());
            editMovieButton.addActionListener(e -> editMovie());
            deleteMovieButton.addActionListener(e -> deleteMovie());
            viewHistoryButton.addActionListener(e -> viewRentHistory());

            backButton.addActionListener(e -> {
                dispose();
                new StartWindow();
            });

            buttonPanel.add(loadMoviesButton);
            buttonPanel.add(addMovieButton);
            buttonPanel.add(editMovieButton);
            buttonPanel.add(deleteMovieButton);
            buttonPanel.add(viewHistoryButton);
            buttonPanel.add(backButton);
            add(buttonPanel, BorderLayout.SOUTH);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void loadMovies() {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM movies")) {

                List<Object[]> movieData = new ArrayList<>();

                while (rs.next()) {
                    movieData.add(new Object[]{
                            rs.getInt("movieID"),
                            rs.getString("title"),
                            rs.getString("genre"),
                            rs.getDate("release_date"),
                            rs.getInt("available_copies")
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0); // Clear the table
                    for (Object[] row : movieData) {
                        tableModel.addRow(row); // Add rows to the table model
                    }
                });

            } catch (SQLException ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error loading movies", "Error", JOptionPane.ERROR_MESSAGE));
            }
        }

        private void editMovie() {
            String movieID = JOptionPane.showInputDialog(this, "Enter Movie ID to edit:");
            if (movieID != null) {
                new EditMovieWindow(Integer.parseInt(movieID));
                loadMovies();
            }
        }

        private void deleteMovie() {
            String movieID = JOptionPane.showInputDialog(this, "Enter Movie ID to delete:");
            if (movieID != null) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM movies WHERE movieID = ?")) {
                    stmt.setInt(1, Integer.parseInt(movieID));
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Movie deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadMovies();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting movie", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void viewRentHistory() {
            JFrame historyFrame = new JFrame("All Rent History");
            historyFrame.setSize(800, 600);
            historyFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            DefaultTableModel historyTableModel = new DefaultTableModel(new String[]{"User", "Movie Name", "Rent Date", "Return Date"}, 0);
            JTable historyTable = new JTable(historyTableModel);
            JScrollPane scrollPane = new JScrollPane(historyTable);

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user, movie_name, rent_date, return_date FROM rentals")) {

                while (rs.next()) {
                    historyTableModel.addRow(new Object[]{rs.getString("user"), rs.getString("movie_name"), rs.getDate("rent_date"), rs.getDate("return_date")});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading rent history.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            historyFrame.add(scrollPane, BorderLayout.CENTER);
            historyFrame.setLocationRelativeTo(null);
            historyFrame.setVisible(true);
        }
    }

    // AddMovieWindow Class
    class AddMovieWindow extends JFrame {
        public AddMovieWindow() {
            setTitle("Add Movie");
            setSize(400, 300);
            setLayout(new GridLayout(5, 2));

            JLabel titleLabel = new JLabel("Title:");
            JTextField titleField = new JTextField();
            JLabel genreLabel = new JLabel("Genre:");
            JTextField genreField = new JTextField();
            JLabel releaseDateLabel = new JLabel("Release Date (YYYY-MM-DD):");
            JTextField releaseDateField = new JTextField();
            JLabel copiesLabel = new JLabel("Available Copies:");
            JTextField copiesField = new JTextField();
            JButton saveButton = new JButton("Save");

            saveButton.addActionListener(e -> {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO movies (title, genre, release_date, available_copies) VALUES (?, ?, ?, ?)")) {
                    stmt.setString(1, titleField.getText());
                    stmt.setString(2, genreField.getText());
                    stmt.setDate(3, Date.valueOf(releaseDateField.getText()));
                    stmt.setInt(4, Integer.parseInt(copiesField.getText()));
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Movie added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error adding movie", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            add(titleLabel);
            add(titleField);
            add(genreLabel);
            add(genreField);
            add(releaseDateLabel);
            add(releaseDateField);
            add(copiesLabel);
            add(copiesField);
            add(new JLabel());
            add(saveButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    // EditMovieWindow Class
    class EditMovieWindow extends JFrame {
        public EditMovieWindow(int movieID) {
            setTitle("Edit Movie");
            setSize(400, 300);
            setLayout(new GridLayout(5, 2));

            JLabel titleLabel = new JLabel("Title:");
            JTextField titleField = new JTextField();
            JLabel genreLabel = new JLabel("Genre:");
            JTextField genreField = new JTextField();
            JLabel releaseDateLabel = new JLabel("Release Date (YYYY-MM-DD):");
            JTextField releaseDateField = new JTextField();
            JLabel copiesLabel = new JLabel("Available Copies:");
            JTextField copiesField = new JTextField();
            JButton saveButton = new JButton("Save");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM movies WHERE movieID = ?")) {
                stmt.setInt(1, movieID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    titleField.setText(rs.getString("title"));
                    genreField.setText(rs.getString("genre"));
                    releaseDateField.setText(rs.getDate("release_date").toString());
                    copiesField.setText(String.valueOf(rs.getInt("available_copies")));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading movie details", "Error", JOptionPane.ERROR_MESSAGE);
            }

            saveButton.addActionListener(e -> {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement("UPDATE movies SET title = ?, genre = ?, release_date = ?, available_copies = ? WHERE movieID = ?")) {
                    stmt.setString(1, titleField.getText());
                    stmt.setString(2, genreField.getText());
                    stmt.setDate(3, Date.valueOf(releaseDateField.getText()));
                    stmt.setInt(4, Integer.parseInt(copiesField.getText()));
                    stmt.setInt(5, movieID);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Movie updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error updating movie", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            add(titleLabel);
            add(titleField);
            add(genreLabel);
            add(genreField);
            add(releaseDateLabel);
            add(releaseDateField);
            add(copiesLabel);
            add(copiesField);
            add(new JLabel());
            add(saveButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }


    // UserWindow Class
// UserWindow Class
class UserWindow extends JFrame {
    private JTable movieTable;
    private DefaultTableModel tableModel;

    public UserWindow(String username) {
        setTitle("User Panel");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Movie ID", "Title", "Genre", "Release Date", "Available Copies"}, 0);
        movieTable = new JTable(tableModel);
        movieTable.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(movieTable);
        loadMovies();

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton rentButton = new JButton("Rent Movie");
        JButton returnButton = new JButton("Return Movie");
        JButton backButton = new JButton("Back to Login");
        JButton historyButton = new JButton("View Rent History");

        rentButton.addActionListener(e -> rentMovie(username));
        returnButton.addActionListener(e -> returnMovie(username));
        backButton.addActionListener(e -> {
            dispose();
            new LoginWindow();
        });
        historyButton.addActionListener(e -> viewRentHistory(username));

        buttonPanel.add(rentButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(backButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadMovies() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM movies")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("movieID"), rs.getString("title"), rs.getString("genre"), rs.getDate("release_date"), rs.getInt("available_copies")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading movies", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rentMovie(String username) {
        String movieID = JOptionPane.showInputDialog(this, "Enter Movie ID to rent:");
        if (movieID == null || movieID.isEmpty()) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement checkStmt = conn.prepareStatement("SELECT title, available_copies FROM movies WHERE movieID = ?")) {
            checkStmt.setInt(1, Integer.parseInt(movieID));
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Movie ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String movieName = rs.getString("title");
            int availableCopies = rs.getInt("available_copies");
            if (availableCopies <= 0) {
                JOptionPane.showMessageDialog(this, "No copies available to rent.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement rentStmt = conn.prepareStatement("UPDATE movies SET available_copies = available_copies - 1 WHERE movieID = ?");
                 PreparedStatement logRentalStmt = conn.prepareStatement("INSERT INTO rentals (movie_name, user, rent_date) VALUES (?, ?, CURRENT_DATE())")) {
                // Update movie availability
                rentStmt.setInt(1, Integer.parseInt(movieID));
                rentStmt.executeUpdate();

                // Log the rental
                logRentalStmt.setString(1, movieName);
                logRentalStmt.setString(2, username);
                logRentalStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Movie rented successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMovies();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing rental.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnMovie(String username) {
        String movieID = JOptionPane.showInputDialog(this, "Enter Movie ID to return:");
        if (movieID == null || movieID.isEmpty()) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement checkRentalStmt = conn.prepareStatement("SELECT id FROM rentals WHERE movie_name = (SELECT title FROM movies WHERE movieID = ?) AND user = ? AND return_date IS NULL");
             PreparedStatement checkMovieStmt = conn.prepareStatement("SELECT movieID FROM movies WHERE movieID = ?")) {

            checkMovieStmt.setInt(1, Integer.parseInt(movieID));
            ResultSet movieRs = checkMovieStmt.executeQuery();
            if (!movieRs.next()) {
                JOptionPane.showMessageDialog(this, "Movie ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            checkRentalStmt.setInt(1, Integer.parseInt(movieID));
            checkRentalStmt.setString(2, username);
            ResultSet rentalRs = checkRentalStmt.executeQuery();
            if (!rentalRs.next()) {
                JOptionPane.showMessageDialog(this, "You have not rented this movie.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement returnStmt = conn.prepareStatement("UPDATE movies SET available_copies = available_copies + 1 WHERE movieID = ?");
                 PreparedStatement updateRentalStmt = conn.prepareStatement("UPDATE rentals SET return_date = CURRENT_DATE() WHERE id = ?")) {
                // Update movie availability
                returnStmt.setInt(1, Integer.parseInt(movieID));
                returnStmt.executeUpdate();

                // Update rental record
                updateRentalStmt.setInt(1, rentalRs.getInt("id"));
                updateRentalStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Movie returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMovies();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing return.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewRentHistory(String username) {
        JFrame historyFrame = new JFrame("Rent History");
        historyFrame.setSize(600, 400);
        historyFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        DefaultTableModel historyTableModel = new DefaultTableModel(new String[]{"Movie Name", "Rent Date", "Return Date"}, 0);
        JTable historyTable = new JTable(historyTableModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT movie_name, rent_date, return_date FROM rentals WHERE user = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                historyTableModel.addRow(new Object[]{rs.getString("movie_name"), rs.getDate("rent_date"), rs.getDate("return_date")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading rent history.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        historyFrame.add(scrollPane, BorderLayout.CENTER);
        historyFrame.setLocationRelativeTo(null);
        historyFrame.setVisible(true);
    }
}

}
