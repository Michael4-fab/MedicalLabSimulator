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

/**
 * PractitionerDashboard.java
 * Updated: Manage Appointments now supports Accept / Decline / Reschedule
 * and sends email notifications via EmailSender.
 *
 * Keeps all other functionality unchanged.
 */
public class PractitionerDashboard {

    private final String practitionerId;
    // Use same formatter throughout
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PractitionerDashboard(String practitionerId) {
        this.practitionerId = practitionerId;
    }

    // =========================
    // MAIN DASHBOARD (unchanged)
    // =========================
    public void show(Stage stage) {
        Label title = new Label("PRACTITIONER DASHBOARD");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.GOLD);

        String name = getPractitionerName(practitionerId);
        String status = getAvailability(practitionerId);

        Label info = new Label("Welcome, Dr. " + name + " (" + practitionerId + ")");
        info.setTextFill(Color.WHITE);

        if (status == null) status = "Unavailable";
        Label availLabel = new Label("Status: " + status);
        availLabel.setTextFill(status.equalsIgnoreCase("Available") ? Color.LIGHTGREEN : Color.RED);

        Button manageAppointments = new Button("Manage Appointments");
        Button viewPatients = new Button("View Patients");
        Button createBilling = new Button("Create Billing");
        Button manageAvailability = new Button("Change Availability");
        Button logout = new Button("Logout");

        for (Button b : new Button[]{manageAppointments, viewPatients, createBilling, manageAvailability}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        }
        logout.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");

        // wire actions
        manageAppointments.setOnAction(e -> openManageAppointments(stage)); // <--- fixed manager
        viewPatients.setOnAction(e -> showAllPatients());
        createBilling.setOnAction(e -> openCreateBilling());
        manageAvailability.setOnAction(e -> toggleAvailability(availLabel));
        logout.setOnAction(e -> new MainMenu().start(stage));

        VBox root = new VBox(12, title, info, availLabel,
                manageAppointments, viewPatients, createBilling, manageAvailability, logout);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(root, 600, 500));
        stage.setTitle("Practitioner Dashboard");
        stage.show();
    }

    // -------------------------
    // helper DB methods (unchanged)
    // -------------------------
    private String getPractitionerName(String id) {
        try (Connection conn = sqlconnector.connect()) {
            String sql = "SELECT full_name FROM practitioner WHERE practitioner_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("full_name");
        } catch (SQLException e) {
            System.out.println("Error fetching practitioner name: " + e.getMessage());
        }
        return "Unknown";
    }

    private String getAvailability(String id) {
        try (Connection conn = sqlconnector.connect()) {
            String sql = "SELECT availability FROM practitioner WHERE practitioner_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("availability");
        } catch (SQLException e) {
            System.out.println("Error fetching availability: " + e.getMessage());
        }
        return "Unavailable";
    }

    private void toggleAvailability(Label availLabel) {
        try (Connection conn = sqlconnector.connect()) {
            String current = getAvailability(practitionerId);
            if (current == null) current = "Unavailable";
            String newStatus = current.equalsIgnoreCase("Available") ? "Unavailable" : "Available";

            String sql = "UPDATE practitioner SET availability=? WHERE practitioner_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setString(2, practitionerId);
            ps.executeUpdate();

            availLabel.setText("Status: " + newStatus);
            availLabel.setTextFill(newStatus.equalsIgnoreCase("Available") ? Color.LIGHTGREEN : Color.RED);

            new Alert(Alert.AlertType.INFORMATION, "Availability changed to " + newStatus).show();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error updating availability: " + e.getMessage()).show();
        }
    }

    // =========================
    // VIEW ALL PATIENTS (unchanged)
    // =========================
    private void showAllPatients() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = sqlconnector.connect()) {
            String sql = "SELECT patient_id, full_name, email, age FROM patients";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("patient_id")).append(" | ")
                        .append(rs.getString("full_name")).append(" | ")
                        .append(rs.getString("email")).append(" | Age: ")
                        .append(rs.getInt("age")).append("\n");
            }
        } catch (SQLException ex) {
            sb.append("Error: ").append(ex.getMessage());
        }

        Stage s = new Stage();
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);

        Button back = new Button("Back");
        back.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
        back.setOnAction(e -> s.close());

        VBox v = new VBox(10, new Label("Registered Patients:"), ta, back);
        v.setPadding(new Insets(10));
        v.setStyle("-fx-background-color: black;");
        ((Label) v.getChildren().get(0)).setTextFill(Color.GOLD);

        s.setScene(new Scene(v, 600, 400));
        s.show();
    }

    // =========================
    // CREATE BILLING (unchanged)
    // =========================
    private void openCreateBilling() {
        Stage s = new Stage();
        s.setTitle("Create Billing Record");

        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Billing Description");
        descArea.setPrefRowCount(4);

        Label message = new Label();
        message.setTextFill(Color.RED);

        Button saveBtn = new Button("Save Billing");
        Button backBtn = new Button("Back");

        saveBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");
        backBtn.setOnAction(e -> s.close());

        saveBtn.setOnAction(e -> {
            String pid = patientIdField.getText().trim();
            String amount = amountField.getText().trim();
            String desc = descArea.getText().trim();

            if (pid.isEmpty() || amount.isEmpty() || desc.isEmpty()) {
                message.setText("❌ Please fill all fields.");
                return;
            }

            try (Connection conn = sqlconnector.connect()) {
                String sql = "INSERT INTO billing (patient_id, amount, description, date_created) VALUES (?, ?, ?, NOW())";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, pid);
                ps.setString(2, amount);
                ps.setString(3, desc);
                ps.executeUpdate();

                message.setTextFill(Color.LIGHTGREEN);
                message.setText("✅ Billing saved successfully!");
                patientIdField.clear();
                amountField.clear();
                descArea.clear();

            } catch (SQLException ex) {
                message.setText("⚠️ Error: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(10,
                new Label("Enter Billing Details:"),
                patientIdField, amountField, descArea, saveBtn, message, backBtn
        );
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        s.setScene(new Scene(layout, 500, 420));
        s.show();
    }

    // =========================
    // MANAGE APPOINTMENTS 
    // - Supports Accept / Decline / Reschedule
    // - Sends email notifications using EmailSender
    // - Refreshes list after actions
    // =========================
    private void openManageAppointments(Stage parentStage) {
        Stage s = new Stage();
        s.setTitle("Manage Appointments - " + practitionerId);

        Label header = new Label("Appointments for: " + practitionerId);
        header.setTextFill(Color.GOLD);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setStyle("-fx-control-inner-background: black; -fx-text-fill: white;");

        Button refresh = new Button("Refresh");
        Button actBtn = new Button("Accept / Decline / Reschedule");
        Button back = new Button("Back");

        refresh.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
        actBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
        back.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");

        // Refresh load: lists upcoming and pending appointments for this practitioner
        refresh.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            try (Connection conn = sqlconnector.connect()) {
                String sql = "SELECT appointment_id, patient_id, date_time, status FROM appointment WHERE practitioner_id=? ORDER BY date_time DESC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, practitionerId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("appointment_id");
                    String pid = rs.getString("patient_id");
                    Timestamp ts = rs.getTimestamp("date_time");
                    String status = rs.getString("status");
                    sb.append("ID: ").append(id)
                      .append(" | Patient: ").append(pid)
                      .append(" | Date: ").append(ts)
                      .append(" | Status: ").append(status == null ? "Pending" : status)
                      .append("\n");
                }
                if (sb.length() == 0) sb.append("No appointments found for you.");
            } catch (SQLException ex) {
                sb.setLength(0);
                sb.append("Error loading appointments: ").append(ex.getMessage());
            }
            ta.setText(sb.toString());
        });

        // Action button: ask user for appointment id then choice, then perform DB update + email
        actBtn.setOnAction(e -> {
            TextInputDialog idDialog = new TextInputDialog();
            idDialog.setHeaderText("Enter Appointment ID to manage");
            String idStr = idDialog.showAndWait().orElse("").trim();
            if (idStr.isEmpty()) return;

            int apptId;
            try {
                apptId = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid appointment ID").show();
                return;
            }

            ChoiceDialog<String> choice = new ChoiceDialog<>("Accept", "Accept", "Decline", "Reschedule");
            choice.setHeaderText("Choose action");
            String action = choice.showAndWait().orElse("");
            if (action.isEmpty()) return;

            try (Connection conn = sqlconnector.connect()) {
                // get patient email before update
                String emailQ = "SELECT p.email FROM patients p JOIN appointment a ON p.patient_id = a.patient_id WHERE a.appointment_id = ?";
                PreparedStatement ep = conn.prepareStatement(emailQ);
                ep.setInt(1, apptId);
                ResultSet ers = ep.executeQuery();
                String patientEmail = ers.next() ? ers.getString("email") : null;

                if (action.equalsIgnoreCase("Accept")) {
                    String u = "UPDATE appointment SET status='Accepted' WHERE appointment_id=?";
                    PreparedStatement ups = conn.prepareStatement(u);
                    ups.setInt(1, apptId);
                    ups.executeUpdate();

                    // notify patient
                    if (patientEmail != null && !patientEmail.isEmpty()) {
                        EmailSender.sendEmail(patientEmail,
                                "Appointment Accepted",
                                "Hello,\n\nYour appointment (ID: " + apptId + ") has been accepted by Dr. " +
                                        getPractitionerName(practitionerId) + ".\n\nSee you then.\nFAB'S MEDICAL LAB");
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Appointment accepted and patient notified (if email exists).").show();

                } else if (action.equalsIgnoreCase("Decline")) {
                    String u = "UPDATE appointment SET status='Declined' WHERE appointment_id=?";
                    PreparedStatement ups = conn.prepareStatement(u);
                    ups.setInt(1, apptId);
                    ups.executeUpdate();

                    if (patientEmail != null && !patientEmail.isEmpty()) {
                        EmailSender.sendEmail(patientEmail,
                                "Appointment Declined",
                                "Hello,\n\nWe are sorry to inform you that your appointment (ID: " + apptId +
                                        ") has been declined by Dr. " + getPractitionerName(practitionerId) +
                                        ". Please reschedule at your convenience.\nFAB'S MEDICAL LAB");
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Appointment declined and patient notified (if email exists).").show();

                } else if (action.equalsIgnoreCase("Reschedule")) {
                    TextInputDialog dateDialog = new TextInputDialog();
                    dateDialog.setHeaderText("Enter new date & time (yyyy-MM-dd HH:mm)");
                    String newDate = dateDialog.showAndWait().orElse("").trim();
                    if (newDate.isEmpty()) return;

                    LocalDateTime dt = LocalDateTime.parse(newDate, fmt); // throws if invalid
                    String u = "UPDATE appointment SET date_time=?, status='Rescheduled' WHERE appointment_id=?";
                    PreparedStatement ups = conn.prepareStatement(u);
                    ups.setTimestamp(1, Timestamp.valueOf(dt));
                    ups.setInt(2, apptId);
                    ups.executeUpdate();

                    if (patientEmail != null && !patientEmail.isEmpty()) {
                        EmailSender.sendEmail(patientEmail,
                                "Appointment Rescheduled",
                                "Hello,\n\nYour appointment (ID: " + apptId + ") has been rescheduled to: " +
                                        newDate + ".\nFAB'S MEDICAL LAB");
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Appointment rescheduled and patient notified (if email exists).").show();
                }

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            }

            // reload list
            refresh.fire();
        });

        back.setOnAction(e -> s.close());

        VBox root = new VBox(10, header, ta, refresh, actBtn, back);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: black;");

        s.setScene(new Scene(root, 760, 520));
        s.show();

        // initial load
        refresh.fire();
    }

    // helper method used by manageAppointments
    private void updateAppointment(Connection conn, int id, String status) throws SQLException {
        String sql = "UPDATE appointment SET status=? WHERE appointment_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    }
}
