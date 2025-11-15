import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PatientDashboard {

    private final String patientId;  // Logged-in patient’s ID
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PatientDashboard(String patientId) {
        this.patientId = patientId;
    }

    // ===============================
    // MAIN PATIENT DASHBOARD
    // ===============================
    public void show(Stage stage) {

        // ===== HEADER =====
        Label title = new Label("PATIENT DASHBOARD");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.GOLD);

        Label info = new Label("Logged in as: " + patientId);
        info.setTextFill(Color.WHITE);

        // ===== BUTTONS =====
        Button viewAppointments = new Button("View My Appointments");
        Button bookAppointment = new Button("Book Appointment");
        Button viewBilling = new Button("View Billing");
        Button back = new Button("Back to Main Menu");
        Button logout = new Button("Logout");

        // Style main action buttons
        for (Button b : new Button[]{viewAppointments, bookAppointment, viewBilling}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        }

        // Style navigation buttons
        back.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        logout.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");

        Label feedback = new Label();
        feedback.setTextFill(Color.WHITE);

        // ===============================
        // VIEW APPOINTMENTS
        // ===============================
        viewAppointments.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            try (Connection conn = sqlconnector.connect()) {
                String sql = "SELECT * FROM appointment WHERE patient_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    sb.append("Appointment ID: ").append(rs.getInt("appointment_id"))
                      .append("\nPractitioner ID: ").append(rs.getString("practitioner_id"))
                      .append("\nDate & Time: ").append(rs.getTimestamp("date_time"))
                      .append("\nStatus: ").append(rs.getString("status"))
                      .append("\n-----------------------------\n");
                }

                if (sb.length() == 0) sb.append("You have no booked appointments.");
            } catch (Exception ex) {
                sb.append("⚠️ Error: ").append(ex.getMessage());
            }
            showTextWindow("My Appointment", sb.toString());
        });

        // ===============================
        // BOOK APPOINTMENT
        // ===============================
        bookAppointment.setOnAction(e -> {
            // Open AppointmentManager, passing patientId
            AppointmentManager manager = new AppointmentManager();
            manager.open(stage, patientId);
        });

        // ===============================
        // VIEW BILLING
        // ===============================
        viewBilling.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            try (Connection conn = sqlconnector.connect()) {
                String sql = "SELECT * FROM billing WHERE patient_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    sb.append("Amount: ₦").append(rs.getDouble("amount"))
                      .append("\nDescription: ").append(rs.getString("description"))
                      .append("\nDate: ").append(rs.getTimestamp("date_created"))
                      .append("\n-----------------------------\n");
                }

                if (sb.length() == 0) sb.append("No billing records found.");
            } catch (Exception ex) {
                sb.append("⚠️ Error: " + ex.getMessage());
            }
            showTextWindow("Billing Records", sb.toString());
        });

        // ===============================
        // BACK BUTTON — Return to Main Menu
        // ===============================
        back.setOnAction(e -> new MainMenu().start(stage));

        // ===============================
        // LOGOUT BUTTON
        // ===============================
        logout.setOnAction(e -> {
            MainMenu menu = new MainMenu();
            menu.start(stage);
        });

        // ===============================
        // LAYOUT
        // ===============================
        VBox root = new VBox(12, title, info, viewAppointments, bookAppointment, viewBilling, feedback, back, logout);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(root, 600, 500));
        stage.setTitle("Patient Dashboard");
        stage.show();
    }

    // ===============================
    // POPUP TEXT WINDOW FOR INFO DISPLAY
    // ===============================
    private void showTextWindow(String title, String content) {
        Stage popup = new Stage();
        TextArea area = new TextArea(content);
        area.setEditable(false);

        Label heading = new Label(title);
        heading.setTextFill(Color.GOLD);
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        VBox layout = new VBox(10, heading, area);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: black;");

        popup.setScene(new Scene(layout, 600, 400));
        popup.setTitle(title);
        popup.show();
    }
}
