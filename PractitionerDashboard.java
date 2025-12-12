
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

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
        // --- START REPLACE BLOCK: practitioner availability label + toggle ---
        String status = getAvailability(practitionerId);

        Label info = new Label("Welcome, Dr. " + name + " (" + practitionerId + ")");
        info.setTextFill(Color.WHITE);

// Status label (text + color)
        Label availLabel = new Label("Status: " + status);
        availLabel.setTextFill(status.equalsIgnoreCase("Available") ? Color.LIGHTGREEN : Color.RED);

// Create a sleek ToggleButton styled like a switch (no textual "button" look)
        ToggleButton availabilityToggle = new ToggleButton();
        availabilityToggle.setFocusTraversable(false);
        availabilityToggle.setPrefSize(56, 26);
        availabilityToggle.setStyle("-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: transparent;");

// helper to update the small switch visual (green/red)
        Runnable applyVisual = () -> {
            boolean sel = availabilityToggle.isSelected();
            if (sel) {
                // ON look
                availabilityToggle.setStyle(
                        "-fx-background-color: linear-gradient(#4CAF50, #43A047);"
                        + "-fx-background-radius: 16; -fx-border-radius: 16;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2,0,0,1);"
                );
            } else {
                // OFF look
                availabilityToggle.setStyle(
                        "-fx-background-color: linear-gradient(#d9534f, #c0392b);"
                        + "-fx-background-radius: 16; -fx-border-radius: 16;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 2,0,0,1);"
                );
            }
        };

// initialize toggle state from DB value
        boolean initiallyAvailable = "Available".equalsIgnoreCase(status);
        availabilityToggle.setSelected(initiallyAvailable);
        applyVisual.run();

// show textual label beside it (small)
        Label toggleText = new Label(initiallyAvailable ? "Available" : "Unavailable");
        toggleText.setTextFill(initiallyAvailable ? Color.LIGHTGREEN : Color.RED);
        toggleText.setStyle("-fx-font-weight: bold;");

// When user toggles: update DB and UI
        availabilityToggle.selectedProperty().addListener((obs, was, now) -> {
            String newStatus = now ? "Available" : "Unavailable";

            // optimistic UI update
            toggleText.setText(newStatus);
            toggleText.setTextFill(now ? Color.LIGHTGREEN : Color.RED);
            availLabel.setText("Status: " + newStatus);
            availLabel.setTextFill(now ? Color.LIGHTGREEN : Color.RED);
            applyVisual.run();

            // persist change to DB
            try (Connection conn = sqlconnector.connect()) {
                String sql = "UPDATE practitioner SET availability=? WHERE practitioner_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, newStatus);
                ps.setString(2, practitionerId);
                int updated = ps.executeUpdate();

                if (updated == 0) {
                    // nothing updated — revert UI and inform user
                    availabilityToggle.setSelected(was);
                    toggleText.setText(was ? "Available" : "Unavailable");
                    toggleText.setTextFill(was ? Color.LIGHTGREEN : Color.RED);
                    availLabel.setText("Status: " + (was ? "Available" : "Unavailable"));
                    availLabel.setTextFill(was ? Color.LIGHTGREEN : Color.RED);
                    applyVisual.run();
                    new Alert(Alert.AlertType.ERROR, "Could not update availability in database.").show();
                }
            } catch (Exception ex) {
                // revert toggle on error
                availabilityToggle.setSelected(was);
                toggleText.setText(was ? "Available" : "Unavailable");
                toggleText.setTextFill(was ? Color.LIGHTGREEN : Color.RED);
                availLabel.setText("Status: " + (was ? "Available" : "Unavailable"));
                availLabel.setTextFill(was ? Color.LIGHTGREEN : Color.RED);
                applyVisual.run();
                new Alert(Alert.AlertType.ERROR, "Error updating availability: " + ex.getMessage()).show();
            }
        });

// Put toggle and text into an HBox so layout stays tidy
        HBox availabilityBox = new HBox(8, availabilityToggle, toggleText);
        availabilityBox.setAlignment(Pos.CENTER);

// --- END REPLACE BLOCK ---
        Button manageAppointments = new Button("Manage Appointments");
        Button viewPatients = new Button("View Patients");
        Button createBilling = new Button("Create Billing");

        Button logout = new Button("Logout");

        for (Button b : new Button[]{manageAppointments, viewPatients, createBilling,}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        }
        logout.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");

        // wire actions
        manageAppointments.setOnAction(e -> openManageAppointments(stage)); // <--- fixed manager
        viewPatients.setOnAction(e -> showAllPatients());
        createBilling.setOnAction(e -> openCreateBilling());
        logout.setOnAction(e -> new MainMenu().start(stage));

        VBox root = new VBox(12, title, info, availLabel,
                manageAppointments, viewPatients, createBilling, availabilityBox, logout);
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
            if (rs.next()) {
                return rs.getString("full_name");
            }
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
            if (rs.next()) {
                return rs.getString("availability");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching availability: " + e.getMessage());
        }
        return "Unavailable";
    }

    private void toggleAvailability(Label availLabel) {
        try (Connection conn = sqlconnector.connect()) {
            String current = getAvailability(practitionerId);
            if (current == null) {
                current = "Unavailable";
            }
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
// VIEW ALL PATIENTS  (TABLE VIEW VERSION)
// =========================
    private void showAllPatients() {

        Stage s = new Stage();
        s.setTitle("Registered Patients");

        Label header = new Label("Registered Patients");
        header.setTextFill(Color.GOLD);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // TABLE SETUP
        TableView<PatientRow> table = new TableView<>();
        table.setStyle("-fx-background-color: black; -fx-border-color: gold; -fx-border-width: 2;");

        TableColumn<PatientRow, String> colId = new TableColumn<>("Patient ID");
        TableColumn<PatientRow, String> colName = new TableColumn<>("Full Name");
        TableColumn<PatientRow, String> colEmail = new TableColumn<>("Email");
        TableColumn<PatientRow, Integer> colAge = new TableColumn<>("Age");

        // gold header text
        colId.setStyle("-fx-alignment: CENTER; -fx-text-fill: teal;");
        colName.setStyle("-fx-alignment: CENTER; -fx-text-fill: teal;");
        colEmail.setStyle("-fx-alignment: CENTER; -fx-text-fill: teal;");
        colAge.setStyle("-fx-alignment: CENTER; -fx-text-fill: teal;");

        colId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));

        // expand evenly
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(colId, colName, colEmail, colAge);

        // LOAD DATA
        try (Connection conn = sqlconnector.connect()) {
            String sql = "SELECT patient_id, full_name, email, age FROM patients";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                table.getItems().add(new PatientRow(
                        rs.getString("patient_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getInt("age")
                ));
            }

        } catch (SQLException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Error loading patients: " + ex.getMessage());
            a.show();
        }

        Button back = new Button("Back");
        back.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
        back.setOnAction(e -> s.close());

        VBox v = new VBox(10, header, table, back);
        v.setPadding(new Insets(10));
        v.setStyle("-fx-background-color: black;");

        s.setScene(new Scene(v, 650, 480));
        s.show();
    }

// MODEL CLASS FOR TABLE
    public static class PatientRow {

        private final String patientId;
        private final String fullName;
        private final String email;
        private final Integer age;

        public PatientRow(String patientId, String fullName, String email, Integer age) {
            this.patientId = patientId;
            this.fullName = fullName;
            this.email = email;
            this.age = age;
        }

        public String getPatientId() {
            return patientId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public Integer getAge() {
            return age;
        }
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

        TableView<AppointmentRow> table = new TableView<>();
        table.setStyle("-fx-background-color: black; -fx-border-color: gold; -fx-border-width: 2;");

        TableColumn<AppointmentRow, Integer> colId = new TableColumn<>("ID");
        TableColumn<AppointmentRow, String> colPatient = new TableColumn<>("Patient");
        TableColumn<AppointmentRow, String> colDate = new TableColumn<>("Date/Time");
        TableColumn<AppointmentRow, String> colStatus = new TableColumn<>("Status");

        // gold header text
        colId.setStyle("-fx-alignment: CENTER; -fx-text-fill: black;");
        colPatient.setStyle("-fx-alignment: CENTER; -fx-text-fill: black;");
        colDate.setStyle("-fx-alignment: CENTER; -fx-text-fill: black;");
        colStatus.setStyle("-fx-alignment: CENTER; -fx-text-fill: black;");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // make columns expand evenly
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(colId, colPatient, colDate, colStatus);

        // model class
        class AppointmentRow {

            private final Integer id;
            private final String patientId;
            private final String date;
            private final String status;

            AppointmentRow(Integer id, String patientId, String date, String status) {
                this.id = id;
                this.patientId = patientId;
                this.date = date;
                this.status = status;
            }

            public Integer getId() {
                return id;
            }

            public String getPatientId() {
                return patientId;
            }

            public String getDate() {
                return date;
            }

            public String getStatus() {
                return status;
            }
        }

        Button refresh = new Button("Refresh");
        Button actBtn = new Button("Accept / Decline / Reschedule");
        Button back = new Button("Back");

        refresh.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
        actBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
        back.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");

        // Refresh load: lists upcoming and pending appointments for this practitioner
        refresh.setOnAction(e -> {

            table.getItems().clear(); // CLEAR TABLE FIRST

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

                    // *** ADD TO TABLE HERE ***
                    table.getItems().add(
                            new PractitionerDashboard.AppointmentRow(
                                    id,
                                    pid,
                                    ts.toString(),
                                    status == null ? "Pending" : status
                            )
                    );
                }
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Error loading appointments: " + ex.getMessage()).show();
            }
        });

        // Action button: ask user for appointment id then choice, then perform DB update + email
        actBtn.setOnAction(e -> {
            TextInputDialog idDialog = new TextInputDialog();
            idDialog.setHeaderText("Enter Appointment ID to manage");
            String idStr = idDialog.showAndWait().orElse("").trim();
            if (idStr.isEmpty()) {
                return;
            }

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
            if (action.isEmpty()) {
                return;
            }

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

                    if (patientEmail != null && !patientEmail.isEmpty()) {
                        EmailSender.sendEmail(
                                patientEmail,
                                "Appointment Accepted",
                                "Hello,\n\nYour appointment (ID: " + apptId
                                + ") has been accepted by Dr. " + getPractitionerName(practitionerId)
                                + ".\n\nFAB'S MEDICAL LAB"
                        );
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Appointment accepted and patient notified.").show();

                } else if (action.equalsIgnoreCase("Decline")) {
                    String u = "UPDATE appointment SET status='Declined' WHERE appointment_id=?";
                    PreparedStatement ups = conn.prepareStatement(u);
                    ups.setInt(1, apptId);
                    ups.executeUpdate();

                    if (patientEmail != null && !patientEmail.isEmpty()) {
                        EmailSender.sendEmail(
                                patientEmail,
                                "Appointment Declined",
                                "Hello,\n\nYour appointment (ID: " + apptId
                                + ") was declined.\nFAB'S MEDICAL LAB"
                        );
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Appointment declined and patient notified.").show();

                } else if (action.equalsIgnoreCase("Reschedule")) {
                    TextInputDialog dateDialog = new TextInputDialog();
                    dateDialog.setHeaderText("Enter new date & time (yyyy-MM-dd HH:mm)");
                    String newDate = dateDialog.showAndWait().orElse("").trim();
                    if (newDate.isEmpty()) {
                        return;
                    }

                    LocalDateTime dt = LocalDateTime.parse(newDate, fmt);
                    String u = "UPDATE appointment SET date_time=?, status='Rescheduled' WHERE appointment_id=?";
                    PreparedStatement ups = conn.prepareStatement(u);
                    ups.setTimestamp(1, Timestamp.valueOf(dt));
                    ups.setInt(2, apptId);
                    ups.executeUpdate();

                    if (patientEmail != null && !patientEmail.isEmpty()) {
                        EmailSender.sendEmail(
                                patientEmail,
                                "Appointment Rescheduled",
                                "Hello,\n\nYour appointment (ID: " + apptId
                                + ") has been rescheduled to: " + newDate + ".\nFAB'S MEDICAL LAB"
                        );
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Appointment rescheduled and patient notified.").show();
                }

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            }

            // reload list
            refresh.fire();
        });

        VBox root = new VBox(10, header, table, refresh, actBtn, back);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: black;");

        s.setScene(new Scene(root, 760, 520));
        s.show();

        // initial load
        refresh.fire();
    }

//=======================================
// STATIC AppointmentRow model class
//=======================================
    public static class AppointmentRow {

        private final Integer id;
        private final String patientId;
        private final String date;
        private final String status;

        public AppointmentRow(Integer id, String patientId, String date, String status) {
            this.id = id;
            this.patientId = patientId;
            this.date = date;
            this.status = status;
        }

        public Integer getId() {
            return id;
        }

        public String getPatientId() {
            return patientId;
        }

        public String getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }
    }

// helper method
    private void updateAppointment(Connection conn, int id, String status) throws SQLException {
        String sql = "UPDATE appointment SET status=? WHERE appointment_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    }
}
