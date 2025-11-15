/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.sql.*;   // For connecting and working with the MySQL database
import java.time.LocalDateTime;   // For handling date and time (appointments)
import java.time.format.DateTimeFormatter;
import java.util.Scanner;  // For reading user input from the console
import java.sql.Connection;  // To Accept SQL Connection

/**
 *
 * @author ENEMONA MICHAEL ENYO-OJO 2022/244802
 */
public class MedicalLabSimulator {

    // private static final String Database_URL = "jdbc:mysql://localhost:3306/MedicalLabSimulator";
    //private static final String Database_Username = "root";  
    //private static final String Database_Password = "5550555@Cc";
    
    
    // ========== DATABASE CONNECTION DETAILS ==========
    // These will be fetched from another class (sqlconnector)
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ========== MAIN MENU (ENTRY POINT) ==========
    public void start() {
        System.out.println("========================");
        System.out.println("FAB'S MEDICAL LAB SYSTEM");
        System.out.println("========================");

        // Ask the user if they are a patient or a doctor/practitioner
        System.out.print("Are you a (P)atient or (D)octor/Practitioner? ");
        String role = scanner.nextLine().trim().toLowerCase();

        // Redirect user to the correct menu based on their role
        if (role.equals("p")) {
            patientMenu();
        } else if (role.equals("d")) {
            practitionerMenu();
        } else {
            System.out.println(" Invalid input. Please restart and enter P or D.");
        }
    }

    // ================================
    // ====== PATIENT SECTION =========
    // ================================
    private void patientMenu() {
        while (true) {
            // Patient options
            System.out.println("\n==== PATIENT MENU ====");
            System.out.println("1. Register as new patient");
            System.out.println("2. Login as patient");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            // Perform action based on user input
            switch (choice) {
                case "1":
                    registerPatient(); // Register new patient
                    break;
                case "2":
                    patientLogin(); // Login for existing patients
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return; // Exit menu
                default:
                    System.out.println(" Invalid option.");
            }
        }
    }

    // -------- Register New Patient --------
    private void registerPatient() {
        try (Connection conn = connect()) { // Connect to the database
            // Collect patient details
            System.out.print("Enter Patient ID: ");
            String patientId = scanner.nextLine();
            System.out.print("Enter Full Name: ");
            String fullName = scanner.nextLine();
            System.out.print("Enter Age: ");
            int age = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            // SQL command to insert patient details into database
            String sql = "INSERT INTO patients (patient_id, full_name, age, email, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, patientId);
            ps.setString(2, fullName);
            ps.setInt(3, age);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.executeUpdate();

            System.out.println(" Patient registered successfully!");
        } catch (SQLIntegrityConstraintViolationException e) {
            // Handles duplicate ID or email error
            System.out.println(" Error: Patient ID or email already exists.");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    // -------- Patient Login --------
    private void patientLogin() {
        try (Connection conn = connect()) {
            // Get login credentials
            System.out.print("Enter Patient ID: ");
            String pid = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            // Check credentials in database
            String sql = "SELECT * FROM patients WHERE patient_id=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pid);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            // If login successful, open patient actions
            if (rs.next()) {
                System.out.println(" Welcome, " + rs.getString("full_name") + "!");
                patientActions(pid);
            } else {
                System.out.println(" Invalid credentials.");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    // -------- Patient Actions After Login --------
    private void patientActions(String patientId) {
        while (true) {
            System.out.println("\n==== PATIENT ACTIONS ====");
            System.out.println("1. View Practitioners");
            System.out.println("2. Schedule Appointment");
            System.out.println("3. View My Appointments");
            System.out.println("4. View My Billing");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            // Menu actions
            switch (choice) {
                case "1":
                    viewPractitioners();
                    break;
                case "2":
                    scheduleAppointment(patientId);
                    break;
                case "3":
                    viewPatientAppointments(patientId);
                    break;
                case "4":
                    viewPatientBilling(patientId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println(" Invalid choice.");
            }
        }
    }

    // ==============================
    // ==== PRACTITIONER SECTION ====
    // ==============================
    private void practitionerMenu() {
        while (true) {
            // Practitioner options
            System.out.println("\n==== PRACTITIONER MENU ====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    registerPractitioner();
                    break;
                case "2":
                    practitionerLogin();
                    break;
                case "0":
                    return;
                default:
                    System.out.println(" Invalid choice.");
            }
        }
    }

    // -------- Register New Practitioner --------
    private void registerPractitioner() {
        try (Connection conn = connect()) {
            // Collect practitioner details
            System.out.print("Enter Practitioner ID: ");
            String practitionerId = scanner.nextLine();
            System.out.print("Enter Full Name: ");
            String fullName = scanner.nextLine();
            System.out.print("Enter Specialty: ");
            String specialty = scanner.nextLine();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            // Insert practitioner into database
            String sql = "INSERT INTO practitioner (practitioner_id, full_name, specialty, email, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, practitionerId);
            ps.setString(2, fullName);
            ps.setString(3, specialty);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.executeUpdate();

            System.out.println(" Practitioner registered successfully!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println(" Error: Practitioner ID or email already exists.");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    // -------- Practitioner Login --------
    private void practitionerLogin() {
        try (Connection conn = connect()) {
            System.out.print("Enter Practitioner ID: ");
            String did = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            // Verify login credentials
            String sql = "SELECT * FROM practitioner WHERE practitioner_id=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, did);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println(" Welcome Dr. " + rs.getString("full_name") + "!");
                practitionerActions(did);
            } else {
                System.out.println(" Invalid login details.");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    // -------- Practitioner Actions After Login --------
    private void practitionerActions(String practitionerId) {
        while (true) {
            System.out.println("\n==== PRACTITIONER ACTIONS ====");
            System.out.println("1. View Patients");
            System.out.println("2. Create Billing");
            System.out.println("3. Manage Appointments");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewPatients();
                    break;
                case "2":
                    createBilling(practitionerId);
                    break;
                case "3":
                    manageAppointments(practitionerId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println(" Invalid choice.");
            }
        }
    }

    //==================================
// ======== SHARED FUNCTIONS ========
// ==================================
    // Function to view all practitioners available in the system
    private void viewPractitioners() {
        try (Connection conn = connect()) { // Establish database connection
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM practitioner"); // Execute query to fetch all practitioners
            System.out.println("\n=== Practitioners List ===");
            while (rs.next()) { // Loop through all rows in the result set
                // Print practitioner ID, full name, and specialty
                System.out.println(rs.getString("practitioner_id") + " | "
                        + rs.getString("full_name") + " | "
                        + rs.getString("specialty"));
            }
        } catch (Exception e) {
            e.getMessage(); // Handles any database or query errors
        }
    }

    // Function to schedule an appointment for a patient
    private void scheduleAppointment(String patientId) {
        try (Connection conn = connect()) { // Connect to database
            // Ask for practitioner ID
            System.out.print("Enter Practitioner ID: ");
            String practitionerId = scanner.nextLine();

            // Ask for appointment date and time in the required format
            System.out.print("Enter Appointment Date & Time (yyyy-MM-dd HH:mm): ");
            LocalDateTime dateTime = LocalDateTime.parse(scanner.nextLine(), fmt); // Convert input string to LocalDateTime

            // SQL query to insert appointment details into the database
            String sql = "INSERT INTO appointment (patient_id, practitioner_id, date_time) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, patientId); // Set patient ID
            ps.setString(2, practitionerId); // Set practitioner ID
            ps.setTimestamp(3, Timestamp.valueOf(dateTime)); // Convert LocalDateTime to SQL Timestamp
            ps.executeUpdate(); // Execute the insert query

            System.out.println(" Appointment scheduled successfully!");
        } catch (Exception e) {
            e.getMessage(); // Handle any errors

        }
    }

    // Function for a patient to view their own appointments
    private void viewPatientAppointments(String patientId) {
        try (Connection conn = connect()) { // Connect to database
            // Query to fetch appointments for a specific patient
            String sql = "SELECT * FROM appointment WHERE patient_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, patientId); // Bind patient ID to query
            ResultSet rs = ps.executeQuery(); // Execute query

            System.out.println("\n=== Your Appointments ===");
            while (rs.next()) { // Loop through result set
                // Display practitioner ID and appointment date/time
                System.out.println("With Practitioner: " + rs.getString("practitioner_id")
                        + " | Date: " + rs.getTimestamp("date_time"));
            }
        } catch (Exception e) {
            e.getMessage(); // Handle any error

        }
    }

    // Function for a patient to view their billing details
    private void viewPatientBilling(String patientId) {
        try (Connection conn = connect()) { // Connect to database
            // Query to get billing information for a specific patient
            String sql = "SELECT * FROM billing WHERE patient_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, patientId); // Bind patient ID
            ResultSet rs = ps.executeQuery(); // Execute query

            System.out.println("\n=== Billing Records ===");
            while (rs.next()) { // Loop through result set
                // Display billing amount and description
                System.out.println("₦" + rs.getDouble("amount") + " | " + rs.getString("description"));
            }
        } catch (Exception e) {
            e.getMessage(); // Handle exceptions
        }
    }

    // Function for practitioners to view all registered patients
    private void viewPatients() {
        try (Connection conn = connect()) { // Connect to database
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM patients"); // Fetch all patients
            System.out.println("\n=== Registered Patients ===");
            while (rs.next()) { // Loop through result set
                // Display patient ID and full name
                System.out.println(rs.getString("patient_id") + " | " + rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.getMessage(); // Handle errors
        }
    }

    // Function to manage appointments: Accept, Decline, or Reschedule
    private void manageAppointments(String practitionerId) {
        try (Connection conn = connect()) {  // Try-with-resources ensures the connection closes automatically

            // SQL query to get all appointments for the specific practitioner
            String sql = "SELECT * FROM appointment WHERE practitioner_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, practitionerId);  // Set practitioner_id parameter
            ResultSet rs = ps.executeQuery(); // Execute the query and get results

            System.out.println("\n=== Manage Your Appointments ===");
            // Display each appointment record for this practitioner
            while (rs.next()) {
                System.out.println("Appointment ID: " + rs.getInt("appointment_id")
                        + " | Patient: " + rs.getString("patient_id")
                        + " | Date: " + rs.getTimestamp("date_time")
                        + " | Status: " + rs.getString("status"));
            }

            // Prompt practitioner to choose an appointment to manage
            System.out.print("\nEnter Appointment ID to manage (or 0 to exit): ");
            int id = Integer.parseInt(scanner.nextLine());
            if (id == 0) { // Exit if user enters 0
                return;
            }

            // Show management options
            System.out.println("1. Accept");
            System.out.println("2. Decline");
            System.out.println("3. Reschedule");
            System.out.print("Choose action: ");
            int action = Integer.parseInt(scanner.nextLine());

            // Declare variables for reuse later
            String updateSql = "";
            PreparedStatement updatePs;
            String newStatus = "";
            String patientEmail = "";
            String subject = "";
            String message = "";

            // Fetch patient's email so the system can send notifications
            String emailQuery = "SELECT p.email FROM patients p JOIN appointment a ON p.patient_id=a.patient_id WHERE a.appointment_id=?";
            PreparedStatement emailPs = conn.prepareStatement(emailQuery);
            emailPs.setInt(1, id);
            ResultSet emailRs = emailPs.executeQuery();
            if (emailRs.next()) {
                patientEmail = emailRs.getString("email");  // Get email of the patient for notifications
            }

            // Handle the practitioner's chosen action
            switch (action) {
                case 1: // ===== ACCEPT APPOINTMENT =====
                    newStatus = "Accepted";
                    updateSql = "UPDATE appointment SET status=? WHERE appointment_id=?";
                    updatePs = conn.prepareStatement(updateSql);
                    updatePs.setString(1, newStatus);
                    updatePs.setInt(2, id);
                    updatePs.executeUpdate();  // Update status in DB

                    // Send notification email
                    subject = "Appointment Accepted";
                    message = "Congratulations! Your appointment has been accepted by your practitioner. "
                            + "Please make sure to meet up on the scheduled date to avoid rescheduling. "
                            + "Thank you for choosing FAB'S MEDICAL LAB.";
                    EmailSender.sendEmail(patientEmail, subject, message);
                    System.out.println("Appointment accepted and email sent.");
                    break;

                case 2: // ===== DECLINE APPOINTMENT =====
                    newStatus = "Declined";
                    updateSql = "UPDATE appointment SET status=? WHERE appointment_id=?";
                    updatePs = conn.prepareStatement(updateSql);
                    updatePs.setString(1, newStatus);
                    updatePs.setInt(2, id);
                    updatePs.executeUpdate();  // Update status in DB

                    // Send notification email
                    subject = "Appointment Declined";
                    message = "We’re sorry for the inconvenience, but the date you chose is occupied. "
                            + "Your appointment has been declined. Please reschedule. "
                            + "Thanks for choosing FAB'S MEDICAL LAB.";
                    EmailSender.sendEmail(patientEmail, subject, message);
                    System.out.println("Appointment declined. Email sent to patient.");
                    break;

                case 3: // ===== RESCHEDULE APPOINTMENT =====
                    System.out.print("Enter new date and time (yyyy-MM-dd HH:mm): ");
                    LocalDateTime newDateTime = LocalDateTime.parse(scanner.nextLine(), fmt);

                    newStatus = "Rescheduled";
                    updateSql = "UPDATE appointment SET date_time=?, status=? WHERE appointment_id=?";
                    updatePs = conn.prepareStatement(updateSql);
                    updatePs.setTimestamp(1, Timestamp.valueOf(newDateTime));
                    updatePs.setString(2, newStatus);
                    updatePs.setInt(3, id);
                    updatePs.executeUpdate();  // Update both date and status

                    // Send email notification
                    subject = "Appointment Rescheduled";
                    message = "Your appointment has been rescheduled to: " + newDateTime.toString()
                            + "Thanks for choosing FAB'S MEDICAL LAB.";
                    EmailSender.sendEmail(patientEmail, subject, message);
                    System.out.println("Appointment rescheduled and email sent.");
                    break;

                default:
                    System.out.println("Invalid action.");
            }
        } catch (Exception e) {
            // Print any error message for debugging
            System.out.println("Error: " + e.getMessage());
        }
    }

// =============================
// === HELPER METHODS BELOW ===
// =============================
// This method updates the status and notes of an appointment (used internally)
    private void updateAppointmentStatus(Connection conn, String appointmentId, String status, String notes) throws Exception {
        String sql = "UPDATE appointment SET status=?, notes=? WHERE appointment_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);         // Set new status (e.g. "Accepted", "Declined")
        ps.setString(2, notes);          // Add any notes if provided
        ps.setString(3, appointmentId);  // Identify which appointment to update
        ps.executeUpdate();              // Run the update command
        System.out.println("✅ Appointment " + status);  // Confirmation message
    }

// This method reschedules an appointment to a new date and marks it as "Rescheduled"
    private void rescheduleAppointment(Connection conn, String appointmentId, LocalDateTime newDate) throws Exception {
        String sql = "UPDATE appointment SET appointment_time=?, status='Rescheduled' WHERE appointment_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, Timestamp.valueOf(newDate));  // Set the new date/time
        ps.setString(2, appointmentId);                  // Target the specific appointment
        ps.executeUpdate();                              // Execute update in DB
        System.out.println("✅ Appointment rescheduled!");  // Confirmation output
    }

    // Function for a practitioner to create a billing record for a patient
    private void createBilling(String practitionerId) {
        try (Connection conn = connect()) { // Connect to database
            // Ask for patient details and billing info
            System.out.print("Enter Patient ID: ");
            String patientId = scanner.nextLine();
            System.out.print("Enter Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter Description: ");
            String desc = scanner.nextLine();

            // Insert billing record into billing table
            String sql = "INSERT INTO billing (patient_id, amount, description) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, patientId); // Set patient ID
            ps.setDouble(2, amount); // Set amount
            ps.setString(3, desc); // Set description
            ps.executeUpdate(); // Execute insert command

            System.out.println(" Billing record created successfully!");
        } catch (Exception e) {
            e.getMessage(); // Handle exceptions
        }
    }

    // ========== DATABASE CONNECTION FUNCTION ==========
    private Connection connect() throws Exception {
        // Load MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Get database details from another class (sqlconnector)
        sqlconnector sqlconnector1 = new sqlconnector();

        // Return a connection to the MySQL database
        return DriverManager.getConnection(sqlconnector1.dbUrl, sqlconnector1.username, sqlconnector1.password);
    }
}
