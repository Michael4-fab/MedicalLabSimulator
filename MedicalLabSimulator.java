
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class MedicalLabSimulator {

    // --------------------------
    // CONFIG & UTIL 
    // --------------------------
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Entry point for running in console
    public static void main(String[] args) {
        MedicalLabSimulator app = new MedicalLabSimulator();
        app.launch();
    }

    // Launcher: shows top-level choices (includes Tests & Prices top-right item)
    void launch() {
        printHeader();

        while (true) {
            // Top-right feature shown as menu option "T"
            System.out.println("\n[ T ] Tests & Prices (top-right)   [ P ] Patient   [ D ] Practitioner   [ Q ] Quit");
            System.out.print("Choose an option: ");
            String cmd = scanner.nextLine().trim().toUpperCase();

            switch (cmd) {
                case "T":
                    showTestsAndPrices();   // Option 1 you asked for (simple list)
                    break;
                case "P":
                    patientMainMenu();
                    break;
                case "D":
                    practitionerMainMenu();
                    break;
                case "Q":
                    printlnCenter("Goodbye — Fab's Medical Lab Console");
                    return;
                default:
                    System.out.println("Unknown option. Press T, P, D or Q.");
            }
        }
    }

    // --------------------------
    // UI: header + helpers (STYLE D)
    // --------------------------
    private void printHeader() {
        System.out.println("========================================");
        System.out.println("       FAB'S MEDICAL LAB - CONSOLE      ");
        System.out.println("========================================");
    }

    private void printlnCenter(String s) {
        System.out.println("\n>>> " + s + "\n");
    }

    // --------------------------
    // TESTS & PRICES (Option 1)
    // --------------------------
    // This matches the "top-right" Tests & Prices feature in the GUI.
    private void showTestsAndPrices() {

        System.out.println("\n── TESTS & PRICES ───────────────────────");
        System.out.println("1. Full Blood Count (FBC) — Purpose: checks red cells, white cells, platelets; used to detect infections, anemia, etc");
        System.out.println("   PRICE: ₦7,100");
        System.out.println();
        System.out.println("2. Malaria Parasite Test — Purpose: detects malaria parasites in blood");
        System.out.println("   PRICE: ₦3,500");
        System.out.println();
        System.out.println("3. HIV Screening Test — Purpose: initial screening for HIV antibodies/antigens");
        System.out.println("   PRICE: ₦6,000");
        System.out.println();
        System.out.println("4. Genotype Test — Purpose: determine blood genotype (AA, AS, SS, etc.)");
        System.out.println("   PRICE: ₦4,200");
        System.out.println();
        System.out.println("5. Blood Group Test — Purpose: determine ABO/Rh blood group");
        System.out.println("   PRICE: ₦1,700");
        System.out.println();
        System.out.println("6. Urinalysis — Purpose: check urine for infection, glucose, proteins");
        System.out.println("   PRICE: ₦2,000");
        System.out.println();
        System.out.println("7. Cholesterol Test — Purpose: measure total cholesterol & lipids");
        System.out.println("   PRICE: ₦6,500");
        System.out.println();
        System.out.println("8. Blood Sugar (FBS/RBS) — Purpose: check fasting/random blood glucose");
        System.out.println("   PRICE: ₦2,100");
        System.out.println("──────────────────────────────────────────\n");

        System.out.print("Press ENTER to return to the main menu...");
        scanner.nextLine();
    }

    // --------------------------
    // PATIENT FLOW
    // --------------------------
    private void patientMainMenu() {
        while (true) {
            System.out.println("\n==== PATIENT MENU ====");
            System.out.println("1. Register New Patient");
            System.out.println("2. Patient Login");
            System.out.println("0. Back to Main");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1":
                    registerPatientConsole();
                    break;
                case "2":
                    patientLoginConsole();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // Register (console) — mirrors GUI register dialog
    private void registerPatientConsole() {
        printlnCenter("REGISTER NEW PATIENT");

        try (Connection conn = connect()) {
            System.out.print("Full name: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Age: ");
            String ageStr = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            // Validate
            if (fullName.isEmpty() || ageStr.isEmpty() || email.isEmpty() || password.isEmpty()) {
                System.out.println("❌ All fields are required.");
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                System.out.println("❌ Invalid age.");
                return;
            }

            // Prevent duplicate email
            PreparedStatement check = conn.prepareStatement("SELECT email FROM patients WHERE email = ?");
            check.setString(1, email);
            ResultSet r = check.executeQuery();
            if (r.next()) {
                System.out.println("❌ Email already registered.");
                return;
            }

            // Generate patient id (same logic as GUI)
            String newId = generatePatientId(conn);

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO patients (patient_id, full_name, age, email, password) VALUES (?, ?, ?, ?, ?)"
            );
            insert.setString(1, newId);
            insert.setString(2, fullName);
            insert.setInt(3, age);
            insert.setString(4, email);
            insert.setString(5, password);
            insert.executeUpdate();

            System.out.println("✅ Registered successfully! Your Patient ID: " + newId);

            // Send email (best-effort)
            try {
                EmailSender.sendEmail(email, "Registration Successful - Fab's Medical Lab",
                        "Hello " + fullName + ",\n\nYour Patient ID is: " + newId);
                System.out.println("Info: confirmation email sent.");
            } catch (Exception ex) {
                System.out.println("Info: email sending failed (" + ex.getMessage() + ")");
            }

        } catch (SQLIntegrityConstraintViolationException scve) {
            System.out.println("❌ Error: duplicate key or constraint violation.");
        } catch (Exception ex) {
            System.out.println("❌ Error registering: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void patientLoginConsole() {
        printlnCenter("PATIENT LOGIN");

        try (Connection conn = connect()) {
            System.out.print("Patient ID: ");
            String pid = scanner.nextLine().trim();
            System.out.print("Password: ");
            String pw = scanner.nextLine().trim();

            if (pid.isEmpty() || pw.isEmpty()) {
                System.out.println("❌ Please fill all fields.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM patients WHERE patient_id=? AND password=?");
            ps.setString(1, pid);
            ps.setString(2, pw);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Login successful. Welcome " + rs.getString("full_name"));
                patientActionsConsole(pid);
            } else {
                System.out.println("❌ Invalid credentials.");
            }

        } catch (Exception ex) {
            System.out.println("❌ Login error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void patientActionsConsole(String patientId) {
        while (true) {
            System.out.println("\n--- Patient Dashboard ---");
            System.out.println("1. View Practitioners");
            System.out.println("2. Book Appointment");
            System.out.println("3. View My Appointments");
            System.out.println("4. View My Billing");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1":
                    listPractitionersConsole();
                    break;
                case "2":
                    bookAppointmentConsole(patientId);
                    break;
                case "3":
                    viewPatientAppointmentsConsole(patientId);
                    break;
                case "4":
                    viewPatientBillingConsole(patientId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // --------------------------
    // PRACTITIONER FLOW
    // --------------------------
    private void practitionerMainMenu() {
        while (true) {
            System.out.println("\n==== PRACTITIONER MENU ====");
            System.out.println("1. Register Practitioner");
            System.out.println("2. Practitioner Login");
            System.out.println("0. Back to Main");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1":
                    registerPractitionerConsole();
                    break;
                case "2":
                    practitionerLoginConsole();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void registerPractitionerConsole() {
        printlnCenter("REGISTER PRACTITIONER");

        try (Connection conn = connect()) {
            System.out.print("Practitioner ID: ");
            String pid = scanner.nextLine().trim();

            System.out.print("Full Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Specialty: ");
            String specialty = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            // Basic checks
            if (pid.isEmpty() || name.isEmpty() || specialty.isEmpty() || email.isEmpty() || password.isEmpty()) {
                System.out.println("❌ All fields required.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO practitioner (practitioner_id, full_name, specialty, email, password, availability) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, pid);
            ps.setString(2, name);
            ps.setString(3, specialty);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.setString(6, "Available"); // default to Available
            ps.executeUpdate();

            System.out.println("✅ Practitioner registered: " + name);
        } catch (SQLIntegrityConstraintViolationException scve) {
            System.out.println("❌ Practitioner ID or email already exists.");
        } catch (Exception ex) {
            System.out.println("❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void practitionerLoginConsole() {
        printlnCenter("PRACTITIONER LOGIN");

        try (Connection conn = connect()) {
            System.out.print("Practitioner ID: ");
            String pid = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            if (pid.isEmpty() || password.isEmpty()) {
                System.out.println("❌ Fill all fields.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM practitioner WHERE practitioner_id=? AND password=?");
            ps.setString(1, pid);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Welcome Dr. " + rs.getString("full_name"));
                practitionerActionsConsole(pid);
            } else {
                System.out.println("❌ Invalid login.");
            }

        } catch (Exception ex) {
            System.out.println("❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void practitionerActionsConsole(String practitionerId) {
        while (true) {
            System.out.println("\n--- Practitioner Dashboard ---");
            System.out.println("1. View Patients");
            System.out.println("2. Manage Appointments");
            System.out.println("3. Create Billing");
            System.out.println("4. Toggle Availability (ON/OFF)");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1":
                    viewPatientsConsole();
                    break;
                case "2":
                    manageAppointmentsConsole(practitionerId);
                    break;
                case "3":
                    createBillingConsole(practitionerId);
                    break;
                case "4":
                    toggleAvailabilityConsole(practitionerId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // --------------------------
    // Practitioner availability toggle (ON/OFF)
    // --------------------------
    private void toggleAvailabilityConsole(String practitionerId) {
        printlnCenter("TOGGLE AVAILABILITY");

        try (Connection conn = connect()) {
            // Get current availability
            PreparedStatement get = conn.prepareStatement("SELECT availability FROM practitioner WHERE practitioner_id=?");
            get.setString(1, practitionerId);
            ResultSet rs = get.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Practitioner not found.");
                return;
            }
            String current = rs.getString("availability");
            System.out.println("Current availability: " + current);

            // Toggle
            String newVal = current.equalsIgnoreCase("Available") ? "Unavailable" : "Available";

            PreparedStatement upd = conn.prepareStatement("UPDATE practitioner SET availability=? WHERE practitioner_id=?");
            upd.setString(1, newVal);
            upd.setString(2, practitionerId);
            upd.executeUpdate();

            System.out.println("✅ Availability updated to: " + newVal);
        } catch (Exception ex) {
            System.out.println("❌ Error toggling availability: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --------------------------
    // APPOINTMENTS (patient booking)
    // --------------------------
    private void bookAppointmentConsole(String patientId) {
        printlnCenter("BOOK AN APPOINTMENT");

        try (Connection conn = connect()) {
            // Show available practitioners
            PreparedStatement ps = conn.prepareStatement("SELECT practitioner_id, full_name, specialty, availability FROM practitioner");
            ResultSet rs = ps.executeQuery();

            System.out.println("Available practitioners:");
            while (rs.next()) {
                System.out.printf(" - %s | %s | %s | %s%n",
                        rs.getString("practitioner_id"),
                        rs.getString("full_name"),
                        rs.getString("specialty"),
                        rs.getString("availability"));
            }

            System.out.print("Enter Practitioner ID (exact): ");
            String practitionerId = scanner.nextLine().trim();

            // Verify availability
            PreparedStatement check = conn.prepareStatement("SELECT availability FROM practitioner WHERE practitioner_id=?");
            check.setString(1, practitionerId);
            ResultSet r2 = check.executeQuery();
            if (!r2.next()) {
                System.out.println("❌ Practitioner not found.");
                return;
            }
            String availability = r2.getString("availability");
            if (!"Available".equalsIgnoreCase(availability)) {
                System.out.println("⚠️ Practitioner is not available right now.");
                return;
            }

            System.out.print("Enter date & time (yyyy-MM-dd HH:mm): ");
            String dt = scanner.nextLine().trim();

            LocalDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(dt, fmt);
            } catch (Exception ex) {
                System.out.println("⚠️ Invalid date format. Use yyyy-MM-dd HH:mm");
                return;
            }

            if (dateTime.isBefore(LocalDateTime.now())) {
                System.out.println("⚠️ Cannot book in the past.");
                return;
            }

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO appointment (patient_id, practitioner_id, date_time, status) VALUES (?, ?, ?, 'Pending')"
            );
            insert.setString(1, patientId);
            insert.setString(2, practitionerId);
            insert.setTimestamp(3, Timestamp.valueOf(dateTime));
            insert.executeUpdate();

            System.out.println("✅ Appointment requested. Status: Pending.");
        } catch (Exception ex) {
            System.out.println("❌ Booking error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Appointment viewing for patient (console)
    private void viewPatientAppointmentsConsole(String patientId) {
        printlnCenter("MY APPOINTMENTS");

        try (Connection conn = connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT appointment_id, practitioner_id, date_time, status FROM appointment WHERE patient_id=? ORDER BY date_time DESC");
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();

            System.out.println("ID | Practitioner | Date/Time           | Status");
            System.out.println("--------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s%n",
                        rs.getInt("appointment_id"),
                        rs.getString("practitioner_id"),
                        rs.getTimestamp("date_time").toLocalDateTime().format(fmt),
                        rs.getString("status"));
            }
        } catch (Exception ex) {
            System.out.println("❌ Error fetching appointments: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --------------------------
    // APPOINTMENT management for practitioner
    // --------------------------
    private void manageAppointmentsConsole(String practitionerId) {
        printlnCenter("MANAGE APPOINTMENTS");

        try (Connection conn = connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT appointment_id, patient_id, date_time, status FROM appointment WHERE practitioner_id=? ORDER BY date_time DESC");
            ps.setString(1, practitionerId);
            ResultSet rs = ps.executeQuery();

            System.out.println("ID | Patient | Date/Time           | Status");
            System.out.println("--------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s%n",
                        rs.getInt("appointment_id"),
                        rs.getString("patient_id"),
                        rs.getTimestamp("date_time").toLocalDateTime().format(fmt),
                        rs.getString("status"));
            }

            System.out.print("\nEnter appointment ID to manage (0 to cancel): ");
            String sel = scanner.nextLine().trim();
            int id = Integer.parseInt(sel);
            if (id == 0) return;

            System.out.println("1. Accept  2. Decline  3. Reschedule  0. Cancel");
            System.out.print("Action: ");
            String act = scanner.nextLine().trim();

            // get patient email for notifications
            PreparedStatement emailPs = conn.prepareStatement(
                    "SELECT p.email FROM patients p JOIN appointment a ON p.patient_id=a.patient_id WHERE a.appointment_id=?"
            );
            emailPs.setInt(1, id);
            ResultSet emailRs = emailPs.executeQuery();
            String patientEmail = "";
            if (emailRs.next()) patientEmail = emailRs.getString("email");

            switch (act) {
                case "1": // Accept
                    PreparedStatement acc = conn.prepareStatement("UPDATE appointment SET status=? WHERE appointment_id=?");
                    acc.setString(1, "Accepted");
                    acc.setInt(2, id);
                    acc.executeUpdate();
                    System.out.println("✅ Appointment accepted.");
                    if (!patientEmail.isEmpty()) EmailSender.sendEmail(patientEmail, "Appointment Accepted", "Your appointment has been accepted.");
                    break;
                case "2": // Decline
                    PreparedStatement dec = conn.prepareStatement("UPDATE appointment SET status=? WHERE appointment_id=?");
                    dec.setString(1, "Declined");
                    dec.setInt(2, id);
                    dec.executeUpdate();
                    System.out.println("✅ Appointment declined.");
                    if (!patientEmail.isEmpty()) EmailSender.sendEmail(patientEmail, "Appointment Declined", "Your appointment has been declined. Please reschedule.");
                    break;
                case "3": // Reschedule
                    System.out.print("Enter new date & time (yyyy-MM-dd HH:mm): ");
                    String newDt = scanner.nextLine().trim();
                    LocalDateTime newDate;
                    try {
                        newDate = LocalDateTime.parse(newDt, fmt);
                    } catch (Exception ex) {
                        System.out.println("⚠️ Invalid date format.");
                        return;
                    }
                    PreparedStatement resch = conn.prepareStatement("UPDATE appointment SET date_time=?, status=? WHERE appointment_id=?");
                    resch.setTimestamp(1, Timestamp.valueOf(newDate));
                    resch.setString(2, "Rescheduled");
                    resch.setInt(3, id);
                    resch.executeUpdate();
                    System.out.println("✅ Appointment rescheduled.");
                    if (!patientEmail.isEmpty()) EmailSender.sendEmail(patientEmail, "Appointment Rescheduled", "Your appointment has been rescheduled to: " + newDate.format(fmt));
                    break;
                default:
                    System.out.println("Cancelled.");
            }

        } catch (Exception ex) {
            System.out.println("❌ Manage error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --------------------------
    // BILLING
    // --------------------------
    private void createBillingConsole(String practitionerId) {
        printlnCenter("CREATE BILLING");

        try (Connection conn = connect()) {
            System.out.print("Patient ID: ");
            String patientId = scanner.nextLine().trim();
            System.out.print("Amount (number only): ");
            String amtStr = scanner.nextLine().trim();
            System.out.print("Description: ");
            String desc = scanner.nextLine().trim();

            double amt = Double.parseDouble(amtStr);

            PreparedStatement ins = conn.prepareStatement("INSERT INTO billing (patient_id, amount, description) VALUES (?, ?, ?)");
            ins.setString(1, patientId);
            ins.setDouble(2, amt);
            ins.setString(3, desc);
            ins.executeUpdate();

            System.out.println("✅ Billing record created for " + patientId + " — ₦" + String.format("%.2f", amt));
        } catch (Exception ex) {
            System.out.println("❌ Billing error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void viewPatientBillingConsole(String patientId) {
        printlnCenter("MY BILLING");

        try (Connection conn = connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT amount, description FROM billing WHERE patient_id=? ORDER BY billing_id DESC");
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();

            System.out.println("Amount (₦) | Description");
            System.out.println("-----------------------------");
            while (rs.next()) {
                System.out.printf("₦%.2f | %s%n", rs.getDouble("amount"), rs.getString("description"));
            }
        } catch (Exception ex) {
            System.out.println("❌ Billing fetch error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --------------------------
    // ADMIN / VIEW HELPERS
    // --------------------------
    private void listPractitionersConsole() {
        printlnCenter("PRACTITIONERS LIST");

        try (Connection conn = connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT practitioner_id, full_name, specialty, availability FROM practitioner");
            ResultSet rs = ps.executeQuery();
            System.out.println("ID | Name | Specialty | Availability");
            System.out.println("-------------------------------------");
            while (rs.next()) {
                System.out.printf("%s | %s | %s | %s%n",
                        rs.getString("practitioner_id"),
                        rs.getString("full_name"),
                        rs.getString("specialty"),
                        rs.getString("availability"));
            }
        } catch (Exception ex) {
            System.out.println("❌ Error loading practitioners: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void viewPatientsConsole() {
        printlnCenter("REGISTERED PATIENTS");

        try (Connection conn = connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT patient_id, full_name, email FROM patients");
            ResultSet rs = ps.executeQuery();
            System.out.println("ID | Name | Email");
            System.out.println("------------------------");
            while (rs.next()) {
                System.out.printf("%s | %s | %s%n",
                        rs.getString("patient_id"),
                        rs.getString("full_name"),
                        rs.getString("email"));
            }
        } catch (Exception ex) {
            System.out.println("❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --------------------------
    // UTIL: generate patient id (same logic as GUI)
    // --------------------------
    private String generatePatientId(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT patient_id FROM patients ORDER BY patient_id DESC LIMIT 1");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String last = rs.getString(1);
            if (last != null && last.matches("PATIENT\\d+")) {
                int n = Integer.parseInt(last.replace("PATIENT", "")) + 1;
                return String.format("PATIENT%03d", n);
            }
        }
        return "PATIENT001";
    }

    // --------------------------
    // DB CONNECTOR (expects your sqlconnector class)
    // --------------------------
    private Connection connect() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        sqlconnector sc = new sqlconnector(); // your existing helper
        return DriverManager.getConnection(
        sqlconnector.getDbUrl(),
        sqlconnector.getUsername(),
        sqlconnector.getPassword()
);}
}

                

