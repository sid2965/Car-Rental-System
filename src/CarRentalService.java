
/*import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;

class Customer {
    private String name;
    private String address;
    private String phone;
    private String customer_gmail;
    private String password;
    private static List<Customer> customerData = new ArrayList<>();
    Connection connection;

    public Customer(String name, String address, String phone, String customer_gmail, String password,
            Connection connection) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.customer_gmail = customer_gmail;
        this.password = password;
        this.connection = DatabaseConnection.getConnection();

    }

    public boolean signup() {
        for (Customer customer : customerData) {
            if (customer.phone.equals(phone) || customer.customer_gmail.equals(customer_gmail)) {
                System.out.println("Customer already exists");
                return false;
            }
        }

        String query = "INSERT INTO Customer (customer_gmail,name, phone, address, password) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer_gmail);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, phone);
            preparedStatement.setString(4, address);

            preparedStatement.setString(5, password);
            preparedStatement.executeUpdate();

            customerData.add(new Customer(name, address, phone, customer_gmail, password, connection));
            System.out.println("Customer signed up successfully");
            return true;
        } catch (SQLException e) {
            System.out.println("Signup failed due to database error.");
            e.printStackTrace();
        }
        return false;
    }

    public String getCustomer() {
        return customer_gmail;
    }

    public boolean login() {
        String query = "SELECT customer_gmail, password FROM Customer WHERE customer_gmail = ? and password = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer_gmail);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Login successful");
                return true;
            } else {
                System.out.println("Invalid email or password");
            }
        } catch (SQLException e) {
            System.out.println("Login failed due to database error.");
            e.printStackTrace();
        }
        return false;
    }

    public int generateUniqueComplaintID() throws SQLException {
        int complaintID = 0;
        boolean isUnique = false;

        String checkQuery = "SELECT COUNT(*) FROM complaints WHERE complaintID = ?";

        while (!isUnique) {
            complaintID = new Random().nextInt(1000);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, complaintID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        isUnique = true;
                        return complaintID;
                    }
                }
            }
        }

        throw new SQLException("Unable to generate a unique complaint ID");
    }

    public void fileComplaint(String complaint, int vehicle_id) {

        
        int complaintID = 0;
        try {
            complaintID = generateUniqueComplaintID();
        } catch (SQLException e) {
            System.out.println("Error generating unique complaint ID.");
            e.printStackTrace();
            return;
        }

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = currentDate.format(formatter);

        String query = "INSERT INTO complaints (complaintID,customer_gmail,vehicle_id, complaint,date) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, complaintID);
            preparedStatement.setString(2, customer_gmail);

            preparedStatement.setInt(3, vehicle_id);
            preparedStatement.setString(4, complaint);
            preparedStatement.setString(5, date);
            preparedStatement.executeUpdate();
            System.out.println("Complaint filed successfully");
        } catch (SQLException e) {
            System.out.println("Failed to file complaint.");
            e.printStackTrace();
        }
    }

    public void searchVehicle(int vid) {
        String query = "SELECT * FROM Vehicle WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String details = "Vehicle_id: " + rs.getInt("vehicle_id") +
                            ", Model: " + rs.getString("model") +
                            ", Brand: " + rs.getString("brand") +
                            ", Availability: " + rs.getString("availability") +
                            ", Service Date: " + rs.getString("serviceDate");
                    System.out.println(details);
                } else {
                    System.out.println("Vehicle not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void extendBooking(String customer_gmail, int days) {
        String query = "SELECT end_date FROM Booking WHERE customer_gmail = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer_gmail);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Current due date: " + resultSet.getDate("end_date"));
                String updateQuery = "UPDATE Booking SET end_date = DATE_ADD(end_date, INTERVAL ? DAY) WHERE customer_gmail = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setInt(1, days);
                updateStatement.setString(2, customer_gmail);
                updateStatement.executeUpdate();
                System.out.println("Booking extended successfully");
            } else {
                System.out.println("Customer not found");
            }
        } catch (SQLException e) {
            System.out.println("Failed to extend booking.");
            e.printStackTrace();
        }
    }
}

class Booking extends Customer {
    private int bookID;
    // private int vehicleID;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;
    // private Connection connection;

    

    public Booking(int bookID, String name, String address, String phone, String customer_gmail, String password,
            int vehicleID, LocalDate startDate, LocalDate endDate, Connection connection) {
        super(name, address, phone, customer_gmail, password, connection);
        this.bookID = bookID;
        // this.vehicleID = vehicleID;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    public String generateUniqueBookingID(Connection conn) throws SQLException {
        String bookingID;
        boolean isUnique = false;

        String checkQuery = "SELECT COUNT(*) FROM Booking WHERE bookID = ?";

        while (!isUnique) {
            bookingID = String.format("%03d", new Random().nextInt(1000));

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, bookingID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        isUnique = true;
                        return bookingID;
                    }
                }
            }
        }
        return null;

    }

    public void book(String customerEmail, int vid, Date start_date, Date end_date) {
        String query = "SELECT availability FROM Vehicle WHERE vehicle_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            conn.setAutoCommit(false);
            stmt.setInt(1, vid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("availability") == 1) {
                    String bookingID = generateUniqueBookingID(conn);

                    String insertQuery = "INSERT INTO Booking (customer_gmail, bookID, vehicle_id, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
                    String updateQuery = "UPDATE Vehicle SET availability = 0 WHERE vehicle_id = ?";

                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                            PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

                        insertStmt.setString(1, customerEmail);
                        insertStmt.setString(2, bookingID);
                        insertStmt.setInt(3, vid);
                        insertStmt.setDate(4, new java.sql.Date(start_date.getTime()));
                        insertStmt.setDate(5, new java.sql.Date(end_date.getTime()));
                        insertStmt.executeUpdate();

                        updateStmt.setInt(1, vid);
                        updateStmt.executeUpdate();

                        conn.commit();
                        System.out.println("Booking successful with ID: " + bookingID);
                    }
                } else {
                    System.out.println("Vehicle not found or unavailable.");
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long calculateDays() {
        if (endDate.isBefore(startDate)) {
            System.out.println("Invalid Inputs: End date is before start date.");
            return -1;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public void calculateCost(double dailyCost) {
        long numOfDays = calculateDays();
        if (numOfDays > 0) {
            totalCost = dailyCost * numOfDays;
        } else {
            System.out.println("Cannot calculate cost: Invalid booking dates.");
        }
    }

    public void cancelBooking(int bookID) {
        String query = "DELETE FROM Booking WHERE bookID = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, bookID);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Booking canceled successfully.");
            } else {
                System.out.println("Booking not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void generateBill() {
        System.out.println("Gmail: " + getCustomer());
        System.out.println("Booking ID: " + bookID);
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);
        System.out.println("Total Cost: $" + totalCost);
    }
}

class Vehicle {
    private int vehicleID;
    private String model;
    private String brand;
    private boolean availability;
    private double ratePerDay;
    private Date serviceDate;
    private ArrayList<String> vehicleDetails;

    public Vehicle(int vehicleID, String model, String brand, boolean availability, double ratePerDay, Date serviceDate,
            ArrayList<String> vehicleDetails) {
        this.vehicleID = vehicleID;
        this.model = model;
        this.brand = brand;
        this.availability = availability;
        this.ratePerDay = ratePerDay;
        this.serviceDate = serviceDate;
        this.vehicleDetails = vehicleDetails;
    }

    public void updateAvailability(boolean availability) {
        this.availability = availability;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public double getRatePerDay() {
        return ratePerDay;
    }

    public void setRatePerDay(double ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    public Date getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(Date serviceDate) {
        this.serviceDate = serviceDate;
    }

    public ArrayList<String> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(ArrayList<String> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }
}

class Admin extends Vehicle {
    private String admin_gmail;
    // private String password;
    ArrayList<Admin> adminList = new ArrayList<>();

    public Admin(String admin_gmail, String password) {
        super(0, null, null, false, 0.0, null, new ArrayList<>());
        this.admin_gmail = admin_gmail;
        // this.password = password;
    }

    public List<String> viewCustomerDetails() {
        List<String> customerDetails = new ArrayList<>();
        String sql = "SELECT * FROM Customer";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String details = "Gmail: " + rs.getString("customer_gmail") +
                        ", Name: " + rs.getString("name") +
                        ", Phone: " + rs.getString("phone") +
                        ", Address: " + rs.getString("address");
                customerDetails.add(details);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerDetails;
    }

    public boolean addVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO Vehicle (vehicle_id, model, brand, availability, rpd, serviceDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehicle.getVehicleID());
            stmt.setString(2, vehicle.getModel());
            stmt.setString(3, vehicle.getBrand());
            stmt.setBoolean(4, vehicle.isAvailable());
            stmt.setDouble(5, vehicle.getRatePerDay());
            stmt.setDate(6, new java.sql.Date(vehicle.getServiceDate().getTime()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeVehicle(int vehicle_id) {
        String sql = "DELETE FROM Vehicle WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehicle_id);
            stmt.executeUpdate();
            System.out.println("Vehicle with ID " + vehicle_id + " has been removed.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateAvailability(int vehicle_id, boolean availability) {
        String sql = "UPDATE Vehicle SET availability = ? WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, availability);
            stmt.setInt(2, vehicle_id);
            stmt.executeUpdate();
            System.out.println("Vehicle availability updated for ID: " + vehicle_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addOffer(String offerDetails, double discountRate, String applicableDays) {
        String sql = "INSERT INTO offers (offerDetails, discountRate, applicableDays) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, offerDetails);
            stmt.setDouble(2, discountRate);
            stmt.setString(3, applicableDays);
            stmt.executeUpdate();
            System.out.println("Offer added: " + offerDetails);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void viewAllVehicles() {
        String query = "SELECT vehicle_id, model, brand, availability, serviceDate FROM Vehicle";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("Vehicle Details:");
            System.out.println("ID\tModel\tBrand\tAvailability\tService Date");

            while (rs.next()) {
                int vehicleId = rs.getInt("vehicle_id");
                String model = rs.getString("model");
                String brand = rs.getString("brand");
                String availability = rs.getString("availability");
                String serviceDate = rs.getString("serviceDate");

                System.out.println(vehicleId + "\t" + model + "\t" + brand + "\t" +
                        availability + "\t" + serviceDate);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching vehicle details.");
            e.printStackTrace();
        }
    }

    public List<String> viewHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT * FROM Booking";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String record = "Booking ID: " + rs.getInt("bookID") +
                        ", Gmail: " + rs.getString("customer_gmail") +
                        ", Vehicle ID: " + rs.getInt("vehicle_id") +
                        ", Start Date: " + rs.getDate("start_date") +
                        ", End Date: " + rs.getDate("end_date");
                history.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public List<String> viewComplaint() {
        List<String> complaints = new ArrayList<>();
        String sql = "SELECT * FROM complaints";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String complaint = "Complaint ID: " + rs.getInt("complaintID") +
                        ", Gmail: " + rs.getString("customer_gmail") +
                        ", Description: " + rs.getString("complaint") +
                        ", Date: " + rs.getDate("date");
                complaints.add(complaint);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return complaints;
    }

    public boolean login(String username, String password) {
        String sql = "SELECT adminID FROM admins WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.admin_gmail = rs.getString("adminID");
                System.out.println("Admin " + admin_gmail + " logged in successfully.");
                return true;
            } else {
                System.out.println("Login failed. Invalid username or password.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void notifyOverdue() {
        String sql = "SELECT customer_gmail FROM Booking WHERE endDate < CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String gmail = rs.getString("customer_gmail");
                System.out.println("Notifying overdue customer with Gmail: " + gmail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCustomer(String customer_gmail) {
        String sql = "DELETE FROM Customer WHERE customer_gmail = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer_gmail);
            stmt.executeUpdate();
            System.out.println("Customer with Gmail " + customer_gmail + " has been deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class CarRentalService {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DatabaseConnection.getConnection();
            Admin admin = new Admin("sindu.kapoor@gmail.com", "admin123"); // Assume a pre-existing admin
            // Customer customer = null;

            while (true) {
                System.out.println("Welcome to the Car Rental System");
                System.out.println("1. Customer");
                System.out.println("2. Admin");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1) { // Customer section
                    System.out.println("1. Signup");
                    System.out.println("2. Login");
                    int customerChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (customerChoice == 1) { // Signup
                        System.out.print("Enter your Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter your Gmail: ");
                        String gmail = scanner.nextLine();
                        System.out.print("Enter your Address: ");
                        String address = scanner.nextLine();
                        System.out.print("Enter your Phone: ");
                        String phone = scanner.nextLine();
                        System.out.print("Enter your password: ");
                        String password = scanner.nextLine();

                        Customer customer = new Customer(name, address, phone, gmail, password, connection);
                        if (customer.signup()) {
                            System.out.println("Signup successful!");
                        } else {
                            System.out.println("Signup failed. Please try again.");
                        }
                    } else if (customerChoice == 2) { // Login
                        System.out.print("Enter your Gmail: ");
                        String gmail = scanner.nextLine();
                        System.out.print("Enter your password: ");
                        String password = scanner.nextLine();
                        Booking customer = new Booking(0, null, null, null, gmail, password, 0, null, null, connection);

                        if (customer.login()) {
                            System.out.println("Customer logged in successfully.\n");
                            System.out.println("1) Select Vehicle");
                            System.out.println("2) Booking");
                            System.out.println("3) Extend Booking");
                            System.out.println("4) Cancel Booking");
                            System.out.println("5) File Complaint");

                            int loginChoice = scanner.nextInt();
                            scanner.nextLine();

                            switch (loginChoice) {
                                case 1:
                                    System.out.print("Enter vehicle ID: ");
                                    int vechileid = scanner.nextInt();
                                    customer.searchVehicle(vechileid);
                                    break;
                                case 2:
                                    System.out.print("Enter vehicle ID: ");
                                    int vehicleId = scanner.nextInt();
                                    scanner.nextLine();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    System.out.print("Enter start date (yyyy-MM-dd): ");
                                    LocalDate startDate = LocalDate.parse(scanner.nextLine(), formatter);
                                    Date sd = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                    System.out.print("Enter end date (yyyy-MM-dd): ");
                                    LocalDate endDate = LocalDate.parse(scanner.nextLine(), formatter);
                                    Date ed = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                    customer.book(gmail, vehicleId, sd, ed);
                                    break;
                                case 3:
                                    System.out.print("Enter number of days to extend: ");
                                    int days = scanner.nextInt();
                                    customer.extendBooking(gmail, days);
                                    break;
                                case 4:
                                    System.out.print("Enter booking ID: ");
                                    int bookid = scanner.nextInt();
                                    customer.cancelBooking(bookid);
                                    break;

                                case 5:
                                    int vechileid1 = scanner.nextInt();
                                    scanner.nextLine();
                                    String complaint = scanner.nextLine();
                                    customer.fileComplaint(complaint, vechileid1);
                                    break;
                                default:
                                    System.out.println("Invalid option. Please try again.");
                            }
                        } else {
                            System.out.println("Login failed. Invalid credentials.");
                        }
                    }
                } else if (choice == 2) { // Admin section
                    System.out.print("Enter admin username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter admin password: ");
                    String password = scanner.nextLine();
                    if (admin.login(username, password)) {
                        System.out.println("Admin logged in successfully.\n");
                        System.out.println("1) View Customer Details");
                        System.out.println("2) All Vehicles");
                        System.out.println("3) Remove Vehicle");
                        System.out.println("4) View History");
                        System.out.println("5) Delete Customer");
                        System.out.println("6) View Complaints");

                        int adminChoice = scanner.nextInt();
                        scanner.nextLine();

                        switch (adminChoice) {
                            case 1:
                                List<String> customerDetails = admin.viewCustomerDetails();
                                System.out.println("Customer Details: " + customerDetails);
                                break;
                            case 2:
                                admin.viewAllVehicles();
                                break;
                            case 3:
                                System.out.print("Enter Vehicle ID to remove: ");
                                int vehicleIDToRemove = scanner.nextInt();
                                if (admin.removeVehicle(vehicleIDToRemove)) {
                                    System.out.println("Vehicle removed successfully.");
                                }
                                break;
                            case 4:
                                List<String> bookingHistory = admin.viewHistory();
                                System.out.println("Booking History: " + bookingHistory);
                                break;
                            case 5:
                                System.out.print("Enter customer Gmail to delete: ");
                                String gmailToDelete = scanner.nextLine();
                                admin.deleteCustomer(gmailToDelete);
                                break;
                            case 6:
                                List<String> complaints = admin.viewComplaint();
                                System.out.println("Complaints: " + complaints);
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                        }
                    } else {
                        System.out.println("Login failed. Invalid admin credentials.");
                    }
                } else if (choice == 3) { // Exit
                    System.out.println("Exiting the system. Goodbye!");
                    break;
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
*/
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;

class Customer {
    private String name;
    private String address;
    private String phone;
    String customer_gmail;
    private String password;
    private static List<Customer> customerData = new ArrayList<>();
    Connection connection;

    public Customer(String name, String address, String phone, String customer_gmail, String password,
            Connection connection) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.customer_gmail = customer_gmail;
        this.password = password;
        this.connection = DatabaseConnection.getConnection();

    }

    public boolean signup() {
        for (Customer customer : customerData) {
            if (customer.phone.equals(phone) || customer.customer_gmail.equals(customer_gmail)) {
                System.out.println("Customer already exists");
                return false;
            }
        }

        String query = "INSERT INTO Customer (customer_gmail,name, phone, address, password) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer_gmail);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, phone);
            preparedStatement.setString(4, address);

            preparedStatement.setString(5, password);
            preparedStatement.executeUpdate();

            customerData.add(new Customer(name, address, phone, customer_gmail, password, connection));
            System.out.println("Customer signed up successfully");
            return true;
        } catch (SQLException e) {
            System.out.println("Signup failed due to database error.");
            e.printStackTrace();
        }
        return false;
    }

    public String getCustomer() {
        return customer_gmail;
    }

    public boolean login() {
        String query = "SELECT customer_gmail, password FROM Customer WHERE customer_gmail = ? and password = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer_gmail);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Login successful");
                return true;
            } else {
                System.out.println("Invalid email or password");
            }
        } catch (SQLException e) {
            System.out.println("Login failed due to database error.");
            e.printStackTrace();
        }
        return false;
    }

    public int generateUniqueComplaintID() throws SQLException {
        int complaintID = 0;
        boolean isUnique = false;

        String checkQuery = "SELECT COUNT(*) FROM complaints WHERE complaintID = ?";

        while (!isUnique) {
            complaintID = new Random().nextInt(1000);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, complaintID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        isUnique = true;
                        return complaintID;
                    }
                }
            }
        }

        throw new SQLException("Unable to generate a unique complaint ID");
    }

    public void fileComplaint(String complaint, int vehicle_id) {

        /*
         * String alterTableQuery =
         * "ALTER TABLE complaints MODIFY complaintID INT NOT NULL AUTO_INCREMENT";
         * try (Statement statement = connection.createStatement()) {
         * statement.executeUpdate(alterTableQuery);
         * System.out.println("Table modified to set complaintID as AUTO_INCREMENT.");
         * } catch (SQLException e) {
         * System.out.println("Error modifying table: " + e.getMessage());
         * }
         */
        int complaintID = 0;
        try {
            complaintID = generateUniqueComplaintID();
        } catch (SQLException e) {
            System.out.println("Error generating unique complaint ID.");
            e.printStackTrace();
            return;
        }

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = currentDate.format(formatter);

        String query = "INSERT INTO complaints (complaintID,customer_gmail,vehicle_id, complaint,date) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, complaintID);
            preparedStatement.setString(2, customer_gmail);

            preparedStatement.setInt(3, vehicle_id);
            preparedStatement.setString(4, complaint);
            preparedStatement.setString(5, date);
            preparedStatement.executeUpdate();
            System.out.println("Complaint filed successfully");
        } catch (SQLException e) {
            System.out.println("Failed to file complaint.");
            e.printStackTrace();
        }
    }

    public void searchVehicle(int vid) {
        String query = "SELECT * FROM Vehicle WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String details = "Vehicle_id: " + rs.getInt("vehicle_id") +
                            ", Model: " + rs.getString("model") +
                            ", Brand: " + rs.getString("brand") +
                            ", Availability: " + rs.getString("availability") +
                            ", Service Date: " + rs.getString("serviceDate");
                    System.out.println(details);
                } else {
                    System.out.println("Vehicle not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void extendBooking(String customer_gmail, int days) {
        String query = "SELECT end_date FROM Booking WHERE customer_gmail = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer_gmail);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String updateQuery = "UPDATE Booking SET end_date = DATE_ADD(end_date, INTERVAL ? DAY) WHERE customer_gmail = ?";
                // 4 System.out.println("Current due date: " + resultSet.getDate("end_date"));
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setInt(1, days);
                updateStatement.setString(2, customer_gmail);
                updateStatement.executeUpdate();
                System.out.println("Booking extended successfully");
            } else {
                System.out.println("Customer not found");
            }
        } catch (SQLException e) {
            System.out.println("Failed to extend booking.");
            e.printStackTrace();
        }
    }
}

class Booking extends Customer {
    private int bookID;
    // private int vehicleID;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;
    // private Connection connection;

    /*
     * public Booking(Connection connection) {
     * this.connection = connection;
     * }
     */

    public Booking(int bookID, String name, String address, String phone, String customer_gmail, String password,
            int vehicleID, LocalDate startDate, LocalDate endDate, Connection connection) {
        super(name, address, phone, customer_gmail, password, connection);
        this.bookID = bookID;
        // this.vehicleID = vehicleID;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    public String generateUniqueBookingID(Connection conn) throws SQLException {
        String bookingID;
        boolean isUnique = false;

        String checkQuery = "SELECT COUNT(*) FROM Booking WHERE bookID = ?";

        while (!isUnique) {
            bookingID = String.format("%03d", new Random().nextInt(1000));

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, bookingID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        isUnique = true;
                        return bookingID;
                    }
                }
            }
        }
        return null;
    }

    public void book(String customerEmail, int vid, Date startDate, Date endDate) {
        String query = "SELECT availability FROM Vehicle WHERE vehicle_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            conn.setAutoCommit(false);
            stmt.setInt(1, vid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("availability") == 1) {
                    String bookingID = generateUniqueBookingID(conn);

                    String insertQuery = "INSERT INTO Booking (customer_gmail, bookID, vehicle_id, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
                    String updateQuery = "UPDATE Vehicle SET availability = 0 WHERE vehicle_id = ?";

                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                            PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

                        insertStmt.setString(1, customerEmail);
                        insertStmt.setString(2, bookingID);
                        insertStmt.setInt(3, vid);
                        insertStmt.setDate(4, new java.sql.Date(startDate.getTime()));
                        insertStmt.setDate(5, new java.sql.Date(endDate.getTime()));
                        insertStmt.executeUpdate();

                        updateStmt.setInt(1, vid);
                        updateStmt.executeUpdate();

                        conn.commit();
                        System.out.println("Booking successful with ID: " + bookingID);
                        System.out.println("Bill");
                        generateBill(startDate, endDate, vid);
                    }
                } else {
                    System.out.println("Vehicle not found or unavailable.");
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long calculateDays(Date startDate, Date endDate) {
        if (endDate.before(startDate)) {
            System.out.println("Invalid Inputs: End date is before start date.");
            return -1;
        }
        return ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant());
    }

    public double calculateCost(Date startDate, Date endDate, int vehicleId) {
        long numOfDays = calculateDays(startDate, endDate);
        if (numOfDays <= 0) {
            System.out.println("Cannot calculate cost: Invalid booking dates.");
            return 0.0;
        }

        String query = "SELECT  rpd FROM Vehicle WHERE vehicle_id = ?";
        double dailyCost = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dailyCost = rs.getDouble("rpd");
                } else {
                    System.out.println("Vehicle not found.");
                    return 0.0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalCost = dailyCost * numOfDays;
        return totalCost;
    }

    public void generateBill(Date startDate, Date endDate, int vid) {
        double totalCost = calculateCost(startDate, endDate, vid);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Gmail: " + customer_gmail);

        System.out.println("Start Date: " + dateFormat.format(startDate));
        System.out.println("End Date: " + dateFormat.format(endDate));
        System.out.println("Total Cost: Rs." + totalCost);
    }

    /*
     * private String getCustomerEmail() {
     * return customer_gmail;
     * }
     */

    public void cancelBooking(int bookID) {
        String query = "DELETE FROM Booking WHERE bookID = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, bookID);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Booking canceled successfully.");
            } else {
                System.out.println("Booking not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewAllVehicles() {
        String query = "SELECT vehicle_id, model, brand, availability, serviceDate FROM Vehicle";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("Vehicle Details:");
            System.out.println("ID\tModel\tBrand\tAvailability\tService Date");

            while (rs.next()) {
                int vehicleId = rs.getInt("vehicle_id");
                String model = rs.getString("model");
                String brand = rs.getString("brand");
                String availability = rs.getString("availability");
                String serviceDate = rs.getString("serviceDate");

                System.out.println(vehicleId + "\t" + model + "\t" + brand + "\t" +
                        availability + "\t" + serviceDate);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching vehicle details.");
            e.printStackTrace();
        }
    }

}

class Vehicle {
    private int vehicleID;
    private String model;
    private String brand;
    private boolean availability;
    private double ratePerDay;
    private Date serviceDate;
    private ArrayList<String> vehicleDetails;

    public Vehicle(int vehicleID, String model, String brand, boolean availability, double ratePerDay, Date serviceDate,
            ArrayList<String> vehicleDetails) {
        this.vehicleID = vehicleID;
        this.model = model;
        this.brand = brand;
        this.availability = availability;
        this.ratePerDay = ratePerDay;
        this.serviceDate = serviceDate != null ? serviceDate : new Date();
        this.vehicleDetails = vehicleDetails;
    }

    public Vehicle(int vehicleID2, String model2, String brand2, boolean availability2, double ratePerDay2,
            Date sqlServiceDate) {
        // TODO Auto-generated constructor stub
    }

    public void updateAvailability(boolean availability) {
        this.availability = availability;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public double getRatePerDay() {
        return ratePerDay;
    }

    public void setRatePerDay(double ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    public Date getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(Date serviceDate) {
        this.serviceDate = serviceDate;
    }

    public ArrayList<String> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(ArrayList<String> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }
}

class Admin extends Vehicle {
    private String admin_gmail;
    // private String password;
    ArrayList<Admin> adminList = new ArrayList<>();

    public Admin(String admin_gmail, String password) {
        super(0, null, null, false, 0.0, null, new ArrayList<>());
        this.admin_gmail = admin_gmail;
        // this.password = password;
    }

    public List<String> viewCustomerDetails() {
        List<String> customerDetails = new ArrayList<>();
        String sql = "SELECT * FROM Customer";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            System.out.println("Gmail                   Name               Phone      Address ");
            while (rs.next()) {
                // System.out.println("Gmail Name Phone Address ");
                String details = rs.getString("customer_gmail") +
                        "  " + rs.getString("name") +
                        "  " + rs.getString("phone") +
                        "  " + rs.getString("address");
                customerDetails.add(details);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerDetails;
    }

    public boolean addVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO Vehicle (vehicle_id, model, brand, availability, rpd, serviceDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehicle.getVehicleID());
            stmt.setString(2, vehicle.getModel());
            stmt.setString(3, vehicle.getBrand());
            stmt.setBoolean(4, vehicle.isAvailable());
            stmt.setDouble(5, vehicle.getRatePerDay());
            stmt.setDate(6, new java.sql.Date(vehicle.getServiceDate().getTime()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeVehicle(int vehicle_id) {
        String sql = "DELETE FROM Vehicle WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehicle_id);
            stmt.executeUpdate();
            System.out.println("Vehicle with ID " + vehicle_id + " has been removed.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateAvailability(int vehicle_id, boolean availability) {
        String sql = "UPDATE Vehicle SET availability = ? WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, availability);
            stmt.setInt(2, vehicle_id);
            stmt.executeUpdate();
            System.out.println("Vehicle availability updated for ID: " + vehicle_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addOffer(String offerDetails, double discountRate, String applicableDays) {
        String sql = "INSERT INTO offers (offerDetails, discountRate, applicableDays) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, offerDetails);
            stmt.setDouble(2, discountRate);
            stmt.setString(3, applicableDays);
            stmt.executeUpdate();
            System.out.println("Offer added: " + offerDetails);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void viewAllVehicles() {
        String query = "SELECT vehicle_id, model, brand, availability, serviceDate FROM Vehicle";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("Vehicle Details:");
            System.out.println("ID\tModel\tBrand\tAvailability\tService Date");

            while (rs.next()) {
                int vehicleId = rs.getInt("vehicle_id");
                String model = rs.getString("model");
                String brand = rs.getString("brand");
                String availability = rs.getString("availability");
                String serviceDate = rs.getString("serviceDate");

                System.out.println(vehicleId + "\t" + model + "\t" + brand + "\t" +
                        availability + "\t" + serviceDate);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching vehicle details.");
            e.printStackTrace();
        }
    }

    public List<String> viewHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT * FROM Booking";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            System.out.println("BookId " + "\t" + "Gmail" + "\t" + "vehicleID" + "\t" +
                    "startdate" + "\t" + "endDate");
            while (rs.next()) {
                String record = rs.getInt("bookID") +
                        "\t" + rs.getString("customer_gmail") +
                        "\t" + rs.getInt("vehicle_id") +
                        "\t" + rs.getDate("start_date") +
                        "\t " + rs.getDate("end_date");
                history.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public List<String> viewComplaint() {
        List<String> complaints = new ArrayList<>();
        String sql = "SELECT * FROM complaints";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            System.out.println("complaintId " + "\t" + "Gmail" + "\t" + "description" + "\t" +
                    "date" + "\t");
            while (rs.next()) {
                String complaint = rs.getInt("complaintID") +
                        "\t " + rs.getString("customer_gmail") +
                        "\t" + rs.getString("complaint") +
                        "\t" + rs.getDate("date");
                complaints.add(complaint);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return complaints;
    }

    public boolean login(String username, String password) {
        String sql = "SELECT adminID FROM admins WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.admin_gmail = rs.getString("adminID");
                System.out.println("Admin " + admin_gmail + " logged in successfully.");
                return true;
            } else {
                System.out.println("Login failed. Invalid username or password.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void notifyOverdue() {
        String sql = "SELECT customer_gmail FROM Booking WHERE endDate < CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String gmail = rs.getString("customer_gmail");
                System.out.println("Notifying overdue customer with Gmail: " + gmail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCustomer(String customer_gmail) {
        String sql = "DELETE FROM Customer WHERE customer_gmail = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer_gmail);
            stmt.executeUpdate();
            System.out.println("Customer with Gmail " + customer_gmail + " has been deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class CarRentalService {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            Connection connection = DatabaseConnection.getConnection();
            Admin admin = new Admin("sindu.kapoor@gmail.com", "admin123");

            while (true) {
                System.out.println("Welcome to the Car Rental System");
                System.out.println("1. Customer");
                System.out.println("2. Admin");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1) {
                    while (true) {
                        System.out.println("\nCustomer Menu:");
                        System.out.println("1. Signup");
                        System.out.println("2. Login");
                        System.out.println("3. Exit to Main Menu");
                        System.out.print("Choose an option: ");
                        int customerChoice = scanner.nextInt();
                        scanner.nextLine();

                        if (customerChoice == 1) {
                            System.out.print("Enter your Name: ");
                            String name = scanner.nextLine();
                            System.out.print("Enter your Gmail: ");
                            String gmail = scanner.nextLine();
                            System.out.print("Enter your Address: ");
                            String address = scanner.nextLine();
                            System.out.print("Enter your Phone: ");
                            String phone = scanner.nextLine();
                            System.out.print("Enter your password: ");
                            String password = scanner.nextLine();

                            Customer customer = new Customer(name, address, phone, gmail, password, connection);
                            if (customer.signup()) {
                                System.out.println("Signup successful!");
                            } else {
                                System.out.println("Signup failed. Please try again.");
                            }
                        } else if (customerChoice == 2) {
                            System.out.print("Enter your Gmail: ");
                            String gmail = scanner.nextLine();
                            System.out.print("Enter your password: ");
                            String password = scanner.nextLine();
                            Booking customer = new Booking(0, null, null, null, gmail, password, 0, null, null,
                                    connection);

                            if (customer.login()) {
                                System.out.println("Customer logged in successfully.\n");
                                while (true) {
                                    System.out.println("\nCustomer Actions:");
                                    System.out.println("1) Select Vehicle");
                                    System.out.println("2) Booking");
                                    System.out.println("3) Extend Booking");
                                    System.out.println("4) Cancel Booking");
                                    System.out.println("5) File Complaint");
                                    System.out.println("6)View all vehicles");
                                    System.out.println("7) Logout");
                                    System.out.print("\nEnter Choice:");

                                    int loginChoice = scanner.nextInt();
                                    scanner.nextLine();

                                    if (loginChoice == 7)
                                        break;

                                    switch (loginChoice) {
                                        case 1:
                                            System.out.print("Enter vehicle ID: ");
                                            int vechileid = scanner.nextInt();
                                            customer.searchVehicle(vechileid);
                                            break;
                                        case 2:
                                            System.out.print("Enter vehicle ID: ");
                                            int vehicleId = scanner.nextInt();
                                            scanner.nextLine();
                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                            System.out.print("Enter start date (yyyy-MM-dd): ");
                                            LocalDate startDate = LocalDate.parse(scanner.nextLine(), formatter);
                                            Date sd = Date
                                                    .from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                            System.out.print("Enter end date (yyyy-MM-dd): ");
                                            LocalDate endDate = LocalDate.parse(scanner.nextLine(), formatter);
                                            Date ed = Date
                                                    .from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                            customer.book(gmail, vehicleId, sd, ed);
                                            break;
                                        case 3:
                                            System.out.print("Enter number of days to extend: ");
                                            int days = scanner.nextInt();
                                            customer.extendBooking(gmail, days);
                                            break;
                                        case 4:
                                            System.out.print("Enter booking ID: ");
                                            int bookid = scanner.nextInt();
                                            customer.cancelBooking(bookid);
                                            break;
                                        case 5:
                                            System.out.print("Enter vehicle ID: ");
                                            int vechileid1 = scanner.nextInt();
                                            scanner.nextLine();
                                            System.out.print("Enter complaint: ");
                                            String complaint = scanner.nextLine();
                                            customer.fileComplaint(complaint, vechileid1);
                                            break;
                                        case 6:
                                            customer.viewAllVehicles();
                                            break;

                                        /*
                                         * case 7: // Option for Generating Bill
                                         * System.out.print("Enter Start Date (yyyy-MM-dd): ");
                                         * LocalDate billStartDate = LocalDate.parse(scanner.nextLine(),
                                         * DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                         * System.out.print("Enter End Date (yyyy-MM-dd): ");
                                         * LocalDate billEndDate = LocalDate.parse(scanner.nextLine(),
                                         * DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                         * System.out.print("Enter Vehicle ID for billing: ");
                                         * int billVehicleId = scanner.nextInt();
                                         * scanner.nextLine(); // consume newline
                                         * 
                                         * // Converting LocalDate to java.util.Date for compatibility with
                                         * // generateBill
                                         * Date start = Date.from(
                                         * billStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                         * Date end = Date
                                         * .from(billEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                         * 
                                         * customer.generateBill(start, end, billVehicleId);
                                         * break;
                                         */

                                        default:
                                            System.out.println("Invalid option. Please try again.");
                                    }
                                }
                            } else {
                                System.out.println("Login failed. Invalid credentials.");
                            }
                        } else if (customerChoice == 3) {
                            break;
                        } else {
                            System.out.println("Invalid option. Please try again.");
                        }
                    }
                } else if (choice == 2) {
                    System.out.print("Enter admin username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter admin password: ");
                    String password = scanner.nextLine();
                    if (admin.login(username, password)) {
                        // 1 System.out.println("Admin logged in successfully.\n");
                        while (true) {
                            System.out.println("\nAdmin Menu:");
                            System.out.println("1) View Customer Details");
                            System.out.println("2) All Vehicles");
                            System.out.println("3) Remove Vehicle");
                            System.out.println("4) View History");
                            System.out.println("5) Delete Customer");
                            System.out.println("6) View Complaints");
                            System.out.println("7) Add offer");

                            System.out.println("8) Logout");
                            System.out.print("\nEnter Choice:");
                            int adminChoice = scanner.nextInt();
                            scanner.nextLine();

                            if (adminChoice == 8)
                                break;

                            switch (adminChoice) {
                                case 1:
                                    List<String> customerDetails = admin.viewCustomerDetails();
                                    // System.out.println("Customer Details: " + customerDetails);
                                    for (String det : customerDetails) {
                                        System.out.println(det);
                                    }
                                    break;
                                case 2:
                                    admin.viewAllVehicles();
                                    break;
                                case 3:
                                    System.out.print("Enter Vehicle ID to remove: ");
                                    int vehicleIDToRemove = scanner.nextInt();
                                    if (admin.removeVehicle(vehicleIDToRemove)) {
                                        System.out.println("Vehicle removed successfully.");
                                    }
                                    break;
                                case 4:
                                    List<String> bookingHistory = admin.viewHistory();
                                    // System.out.println("Booking History: " + bookingHistory);Strig
                                    for (String book : bookingHistory) {
                                        System.out.println(book);
                                    }
                                    break;
                                case 5:
                                    System.out.print("Enter customer Gmail to delete: ");
                                    String gmailToDelete = scanner.nextLine();
                                    admin.deleteCustomer(gmailToDelete);
                                    break;
                                case 6:
                                    List<String> complaints = admin.viewComplaint();
                                    // System.out.println("Complaints: " + complaints);
                                    for (String compl : complaints) {
                                        System.out.println(compl);
                                    }
                                    break;
                                case 7:

                                    System.out.print("Enter Offer Details: ");
                                    String offerDetails = scanner.nextLine();
                                    System.out.print("Enter Discount Rate (e.g., 10 for 10%): ");
                                    double discountRate = scanner.nextDouble();
                                    scanner.nextLine(); // consume newline
                                    System.out.print(
                                            "Enter Applicable Days (e.g., 'Weekdays', 'Weekends', 'Holidays'): ");
                                    String applicableDays = scanner.nextLine();

                                    if (admin.addOffer(offerDetails, discountRate, applicableDays)) {
                                        System.out.println("Offer added successfully.");
                                    } else {
                                        System.out.println("Failed to add offer. Please try again.");
                                    }
                                    break;

                                /*
                                 * case 8:
                                 * System.out.print("Enter Vehicle ID: ");
                                 * int vehicleID = scanner.nextInt();
                                 * scanner.nextLine(); // consume newline
                                 * System.out.print("Enter Vehicle Model: ");
                                 * String model = scanner.nextLine();
                                 * System.out.print("Enter Vehicle Brand: ");
                                 * String brand = scanner.nextLine();
                                 * System.out.print("Is the vehicle available? (true/false): ");
                                 * boolean availability = scanner.nextBoolean();
                                 * System.out.print("Enter Rate Per Day (RPD): ");
                                 * double ratePerDay = scanner.nextDouble();
                                 * scanner.nextLine(); // consume newline
                                 * System.out.print("Enter Service Date (yyyy-MM-dd): ");
                                 * String serviceDateStr = scanner.nextLine();
                                 * DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                 * LocalDate serviceDate = LocalDate.parse(serviceDateStr, formatter);
                                 * Date sqlServiceDate = Date
                                 * .from(serviceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                 * 
                                 * Vehicle vehicle = new Vehicle(vehicleID, model, brand, availability,
                                 * ratePerDay,
                                 * sqlServiceDate);
                                 * if (admin.addVehicle(vehicle)) {
                                 * System.out.println("Vehicle added successfully.");
                                 * } else {
                                 * System.out.println("Failed to add vehicle. Please try again.");
                                 * }
                                 * break;
                                 */

                                default:
                                    System.out.println("Invalid option. Please try again.");
                            }
                        }
                    } else {
                        System.out.println("Login failed. Invalid admin credentials.");
                    }
                } else if (choice == 3) {
                    System.out.println("Exiting the system. Goodbye!");
                    break;
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
