
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

        // Practitioner Table
        Label tableLabel = new Label("Available Practitioners");
        tableLabel.setTextFill(Color.GOLD);
        tableLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

// Table Setup
        TableView<Practitioner> table = new TableView<>();
        table.setPrefHeight(200);
        table.setStyle("-fx-background-color: black; -fx-border-color: gold;");

// Columns
        TableColumn<Practitioner, String> snCol = new TableColumn<>("S/N");
        snCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        snCol.setPrefWidth(60);

        TableColumn<Practitioner, String> idCol = new TableColumn<>("Practitioner ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        idCol.setPrefWidth(130);

        TableColumn<Practitioner, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(180);

        TableColumn<Practitioner, String> specialtyCol = new TableColumn<>("Specialty");
        specialtyCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSpecialty()));
        specialtyCol.setPrefWidth(150);

        TableColumn<Practitioner, String> availabilityCol = new TableColumn<>("Availability");
        availabilityCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAvailability()));
        availabilityCol.setPrefWidth(120);

// Add columns to table
        table.getColumns().addAll(snCol, idCol, nameCol, specialtyCol, availabilityCol);

// Load Data From DB
        ObservableList<Practitioner> data = FXCollections.observableArrayList();

        try (Connection conn = sqlconnector.connect()) {
            String sql = "SELECT practitioner_id, full_name, specialty, availability FROM practitioner";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int index = 1;
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
                tableLabel, table,
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
    
    public static class Practitioner {
    private final String sn;
    private final String id;
    private final String name;
    private final String specialty;
    private final String availability;

    public Practitioner(String sn, String id, String name, String specialty, String availability) {
        this.sn = sn;
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.availability = availability;
    }

    public String getSn() { return sn; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getAvailability() { return availability; }
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
