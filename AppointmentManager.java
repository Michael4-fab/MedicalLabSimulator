import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentManager {

    // ===============================
    // MAIN FUNCTION — OPEN SCREEN
    // ===============================
    public void open(Stage stage, String patientId) {
        // Title
        Label title = new Label("Book an Appointment");
        title.setTextFill(Color.GOLD);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Dropdown (List of practitioners)
        ComboBox<String> practitionerDropdown = new ComboBox<>();
        practitionerDropdown.setPromptText("Select Practitioner");

        // Fill the dropdown with practitioner data
        ObservableList<String> practitionerList = FXCollections.observableArrayList();
        try (Connection conn = sqlconnector.connect()) {
            String sql = "SELECT practitioner_id, full_name, specialty, availability FROM practitioner";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String id = rs.getString("practitioner_id");
                String name = rs.getString("full_name");
                String specialty = rs.getString("specialty");
                String availability = rs.getString("availability");
                practitionerList.add(
                        id + " | " + name + " | " + specialty + " | " + availability
                );
            }

            practitionerDropdown.setItems(practitionerList);
        } catch (Exception ex) {
            System.out.println("Error loading practitioners: " + ex.getMessage());
        }

        // Date and time field
        TextField dateField = new TextField();
        dateField.setPromptText("Enter Date & Time (yyyy-MM-dd HH:mm)");

        // Book button
        Button bookBtn = new Button("Book Appointment");
        bookBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");

        // Back button
        Button backBtn = new Button("⬅ Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");
        backBtn.setOnAction(e -> new PatientDashboard(patientId).show(stage)); // Back to dashboard

        Label feedback = new Label();
        feedback.setTextFill(Color.LIGHTGREEN);

        // Practitioner list display
        Label listLabel = new Label("Available Practitioners:");
        listLabel.setTextFill(Color.GOLD);
        TextArea practitionerDisplay = new TextArea();
        practitionerDisplay.setEditable(false);
        practitionerDisplay.setPrefHeight(150);

        // Load all practitioners (available/unavailable)
        practitionerDisplay.setText(getPractitionerList());

        // Booking action
        bookBtn.setOnAction(e -> {
            try (Connection conn = sqlconnector.connect()) {
                String selected = practitionerDropdown.getValue();
                if (selected == null || selected.isEmpty()) {
                    feedback.setText("⚠️ Please select a practitioner.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                String practitionerId = selected.split("\\|")[0].trim();
                String availability = selected.split("\\|")[3].trim();

                if (!availability.equalsIgnoreCase("Available")) {
                    feedback.setText("⚠️ This practitioner is not available.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                // ===========================
                // ✔ FIX: Correct DateTime parsing
                // ===========================
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateTime;

                try {
                    dateTime = LocalDateTime.parse(dateField.getText().trim(), fmt);
                } catch (Exception ex2) {
                    feedback.setText("⚠️ Please use format: yyyy-MM-dd HH:mm");
                    feedback.setTextFill(Color.RED);
                    return;
                }
                // ===========================

                if (dateTime.isBefore(LocalDateTime.now())) {
                    feedback.setText("⚠️ You cannot book an appointment in the past.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                String sql = "INSERT INTO appointment (patient_id, practitioner_id, date_time, status) VALUES (?, ?, ?, 'Pending')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ps.setString(2, practitionerId);
                ps.setTimestamp(3, Timestamp.valueOf(dateTime));
                ps.executeUpdate();

                feedback.setTextFill(Color.LIGHTGREEN);
                feedback.setText("✅ Appointment booked successfully!");
                dateField.clear();
                practitionerDropdown.setValue(null);

            } catch (Exception ex) {
                feedback.setText("⚠️ Error: " + ex.getMessage());
                feedback.setTextFill(Color.RED);
            }
        });

        VBox layout = new VBox(15,
                title,
                listLabel, practitionerDisplay,
                practitionerDropdown, dateField,
                bookBtn, feedback, backBtn
        );
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(layout, 650, 600));
        stage.setTitle("Book Appointment");
        stage.show();
    }

    // ===============================
    // HELPER: GET PRACTITIONER LIST
    // ===============================
    private String getPractitionerList() {
        StringBuilder list = new StringBuilder();
        try (Connection conn = sqlconnector.connect()) {
            String sql = "SELECT practitioner_id, full_name, specialty, availability FROM practitioner";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.append(rs.getString("practitioner_id"))
                        .append(" | ")
                        .append(rs.getString("full_name"))
                        .append(" | ")
                        .append(rs.getString("specialty"))
                        .append(" | ")
                        .append(rs.getString("availability"))
                        .append("\n");
            }
        } catch (Exception e) {
            list.append("Error loading practitioners: ").append(e.getMessage());
        }
        return list.toString();
    }
}
