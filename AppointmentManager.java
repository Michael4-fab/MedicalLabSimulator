// JavaFX collection utilities for observable data structures
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// JavaFX layout and positioning utilities
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// SQL classes for database connectivity and operations
import java.sql.*;

// Date and time handling classes
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AppointmentManager handles appointment booking UI and database operations for
 * patients selecting practitioners and scheduling appointments.
 */
public class AppointmentManager {

    /**
     * Opens the appointment booking screen for a specific patient.
     *
     * @param stage the main application stage
     * @param patientId the logged-in patient's ID
     */
    public void open(Stage stage, String patientId) {

        // Screen title label
        Label title = new Label("Book an Appointment");
        title.setTextFill(Color.GOLD);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Dropdown menu for selecting practitioners
        ComboBox<String> practitionerDropdown = new ComboBox<>();
        practitionerDropdown.setPromptText("Select Practitioner");

        // Observable list to hold practitioner display strings
        ObservableList<String> practitionerList = FXCollections.observableArrayList();

        // Load practitioner data from the database for the dropdown
        try (Connection conn = sqlconnector.connect()) {

            String sql = "SELECT practitioner_id, full_name, specialty, availability FROM practitioner";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Combine practitioner details into a readable format
            while (rs.next()) {
                String id = rs.getString("practitioner_id");
                String name = rs.getString("full_name");
                String specialty = rs.getString("specialty");
                String availability = rs.getString("availability");

                practitionerList.add(
                        id + " | " + name + " | " + specialty + " | " + availability
                );
            }

            // Populate dropdown with practitioner options
            practitionerDropdown.setItems(practitionerList);

        } catch (Exception ex) {
            System.out.println("Error loading practitioners: " + ex.getMessage());
        }

        // Text field for entering appointment date and time
        TextField dateField = new TextField();
        dateField.setPromptText("Enter Date & Time (yyyy-MM-dd HH:mm)");

        // Button used to submit the appointment booking
        Button bookBtn = new Button("Book Appointment");
        bookBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");

        // Button to return to the patient dashboard
        Button backBtn = new Button("⬅ Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");
        backBtn.setOnAction(e -> new PatientDashboard(patientId).show(stage));

        // Label used to display success or error messages
        Label feedback = new Label();
        feedback.setTextFill(Color.LIGHTGREEN);

        // Label heading for practitioner table
        Label tableLabel = new Label("Available Practitioners");
        tableLabel.setTextFill(Color.GOLD);
        tableLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // TableView to visually display practitioner records
        TableView<Practitioner> table = new TableView<>();
        table.setPrefHeight(200);
        table.setStyle("-fx-background-color: black; -fx-border-color: gold;");

        // Column displaying serial numbers
        TableColumn<Practitioner, String> snCol = new TableColumn<>("S/N");
        snCol.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        snCol.setPrefWidth(60);

        // Column displaying practitioner IDs
        TableColumn<Practitioner, String> idCol = new TableColumn<>("Practitioner ID");
        idCol.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        idCol.setPrefWidth(130);

        // Column displaying practitioner names
        TableColumn<Practitioner, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(180);

        // Column displaying practitioner specialty
        TableColumn<Practitioner, String> specialtyCol = new TableColumn<>("Specialty");
        specialtyCol.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSpecialty()));
        specialtyCol.setPrefWidth(150);

        // Column displaying practitioner availability status
        TableColumn<Practitioner, String> availabilityCol = new TableColumn<>("Availability");
        availabilityCol.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAvailability()));
        availabilityCol.setPrefWidth(120);

        // Attach all columns to the table
        table.getColumns().addAll(snCol, idCol, nameCol, specialtyCol, availabilityCol);

        // Observable list holding practitioner objects for the table
        ObservableList<Practitioner> data = FXCollections.observableArrayList();

        // Load practitioner records from the database into the table
        try (Connection conn = sqlconnector.connect()) {

            String sql = "SELECT practitioner_id, full_name, specialty, availability FROM practitioner";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int index = 1;

            // Convert each database row into a Practitioner object
            while (rs.next()) {
                data.add(new Practitioner(
                        String.valueOf(index++),
                        rs.getString("practitioner_id"),
                        rs.getString("full_name"),
                        rs.getString("specialty"),
                        rs.getString("availability")
                ));
            }

            table.setItems(data);

        } catch (Exception ex) {
            System.out.println("Error loading practitioners: " + ex.getMessage());
        }

        // Handle appointment booking logic
        bookBtn.setOnAction(e -> {

            try (Connection conn = sqlconnector.connect()) {

                // Ensure a practitioner has been selected
                String selected = practitionerDropdown.getValue();
                if (selected == null || selected.isEmpty()) {
                    feedback.setText("Please select a practitioner.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                // Extract practitioner ID and availability from dropdown string
                String practitionerId = selected.split("\\|")[0].trim();
                String availability = selected.split("\\|")[3].trim();

                // Prevent booking if practitioner is unavailable
                if (!availability.equalsIgnoreCase("Available")) {
                    feedback.setText("This practitioner is not available.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                // Formatter for parsing user-entered date and time
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateTime;

                // Validate date-time input format
                try {
                    dateTime = LocalDateTime.parse(dateField.getText().trim(), fmt);
                } catch (Exception ex2) {
                    feedback.setText("Use format: yyyy-MM-dd HH:mm");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                // Prevent booking appointments in the past
                if (dateTime.isBefore(LocalDateTime.now())) {
                    feedback.setText("You cannot book an appointment in the past.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                // Insert appointment record into database
                String sql = "INSERT INTO appointment "
                        + "(patient_id, practitioner_id, date_time, status) "
                        + "VALUES (?, ?, ?, 'Pending')";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ps.setString(2, practitionerId);
                ps.setTimestamp(3, Timestamp.valueOf(dateTime));
                ps.executeUpdate();

                // Display success feedback and reset inputs
                feedback.setTextFill(Color.LIGHTGREEN);
                feedback.setText("Appointment booked successfully.");
                dateField.clear();
                practitionerDropdown.setValue(null);

            } catch (Exception ex) {
                feedback.setText("Error: " + ex.getMessage());
                feedback.setTextFill(Color.RED);
            }
        });

        // Main layout container for the screen
        VBox layout = new VBox(
                15,
                title,
                tableLabel,
                table,
                practitionerDropdown,
                dateField,
                bookBtn,
                feedback,
                backBtn
        );

        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        // Configure and display the scene
        stage.setScene(new Scene(layout, 650, 600));
        stage.setTitle("Book Appointment");
        stage.show();
    }

    /**
     * Practitioner model class used to populate the TableView.
     */
    public static class Practitioner {

        // Serial number displayed in the table
        private final String sn;

        // Unique practitioner identifier
        private final String id;

        // Practitioner's full name
        private final String name;

        // Medical specialty of the practitioner
        private final String specialty;

        // Availability status
        private final String availability;

        // Constructor used when creating practitioner records
        public Practitioner(String sn, String id, String name, String specialty, String availability) {
            this.sn = sn;
            this.id = id;
            this.name = name;
            this.specialty = specialty;
            this.availability = availability;
        }

        public String getSn() {
            return sn;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSpecialty() {
            return specialty;
        }

        public String getAvailability() {
            return availability;
        }
    }

    /**
     * Retrieves practitioner details as a formatted string list. This method
     * can be reused for logs, reports, or debugging.
     *
     * @return formatted practitioner list
     */
    private String getPractitionerList() {

        StringBuilder list = new StringBuilder();

        try (Connection conn = sqlconnector.connect()) {

            String sql = "SELECT practitioner_id, full_name, specialty, availability FROM practitioner";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Append each practitioner record to the output string
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
