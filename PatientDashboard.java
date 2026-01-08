// JavaFX layout, positioning, and UI components
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

// JavaFX collections for table data handling
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// SQL classes for database interaction
import java.sql.*;

/**
 * PatientDashboard represents the main interface shown to a logged-in patient.
 * It provides access to appointments, booking, billing, and navigation actions.
 */
public class PatientDashboard {

    // Stores the unique ID of the currently logged-in patient
    private final String patientId;

    /**
     * Constructs the dashboard for a specific patient.
     *
     * @param patientId unique identifier of the logged-in patient
     */
    public PatientDashboard(String patientId) {
        this.patientId = patientId;
    }

    /**
     * Displays the patient dashboard interface.
     *
     * @param stage primary application stage
     */
    public void show(Stage stage) {

        // Dashboard title label
        Label title = new Label("PATIENT DASHBOARD");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.GOLD);

        // Displays currently logged-in patient information
        Label info = new Label("Logged in as: " + patientId);
        info.setTextFill(Color.WHITE);

        // Primary action buttons
        Button viewAppointments = new Button("View My Appointments");
        Button bookAppointment = new Button("Book Appointment");
        Button viewBilling = new Button("View Billing");

        // Navigation and session buttons
        Button back = new Button("Back to Main Menu");
        Button logout = new Button("Logout");

        // Apply consistent styling to main action buttons
        for (Button b : new Button[]{viewAppointments, bookAppointment, viewBilling}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        }

        // Styling for navigation-related buttons
        back.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        logout.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");

        // Label used for displaying feedback messages
        Label feedback = new Label();
        feedback.setTextFill(Color.WHITE);

        // Opens a window displaying the patient's appointments in a table
        viewAppointments.setOnAction(e -> {

            Stage apptStage = new Stage();
            apptStage.setTitle("My Appointments");

            // Table for displaying appointment records
            TableView<AppointmentRow> table = new TableView<>();

            // Column for appointment ID
            TableColumn<AppointmentRow, String> colId =
                    new TableColumn<>("Appointment ID");
            colId.setCellValueFactory(
                    new PropertyValueFactory<>("appointmentId"));
            colId.setPrefWidth(130);

            // Column for practitioner ID
            TableColumn<AppointmentRow, String> colPract =
                    new TableColumn<>("Practitioner ID");
            colPract.setCellValueFactory(
                    new PropertyValueFactory<>("practitionerId"));
            colPract.setPrefWidth(150);

            // Column for appointment date and time
            TableColumn<AppointmentRow, String> colDate =
                    new TableColumn<>("Date & Time");
            colDate.setCellValueFactory(
                    new PropertyValueFactory<>("dateTime"));
            colDate.setPrefWidth(200);

            // Column for appointment status
            TableColumn<AppointmentRow, String> colStatus =
                    new TableColumn<>("Status");
            colStatus.setCellValueFactory(
                    new PropertyValueFactory<>("status"));
            colStatus.setPrefWidth(120);

            // Attach all columns to the table
            table.getColumns().addAll(colId, colPract, colDate, colStatus);

            // Observable list to hold appointment records
            ObservableList<AppointmentRow> rows =
                    FXCollections.observableArrayList();

            // Retrieve appointment records for the logged-in patient
            try (Connection conn = sqlconnector.connect()) {

                String sql = "SELECT * FROM appointment WHERE patient_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();

                // Convert each database row into a table row object
                while (rs.next()) {
                    rows.add(new AppointmentRow(
                            rs.getString("appointment_id"),
                            rs.getString("practitioner_id"),
                            rs.getTimestamp("date_time").toString(),
                            rs.getString("status")
                    ));
                }

                // Notify user if no appointments exist
                if (rows.isEmpty()) {
                    Alert alert =
                            new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("No Appointments");
                    alert.setContentText(
                            "You have no booked appointments.");
                    alert.show();
                    return;
                }

            } catch (Exception ex) {
                Alert alert =
                        new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error Loading Appointments");
                alert.setContentText(ex.getMessage());
                alert.show();
                return;
            }

            // Populate table with appointment data
            table.setItems(rows);

            // Button to close the appointment window
            Button backBtn = new Button("Back");
            backBtn.setStyle(
                    "-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
            backBtn.setOnAction(ev -> apptStage.close());

            // Heading label for the appointments window
            Label heading = new Label("My Appointments");
            heading.setTextFill(Color.GOLD);
            heading.setStyle(
                    "-fx-font-size: 18px; -fx-font-weight: bold;");

            // Layout container for appointment window
            VBox box = new VBox(15, heading, table, backBtn);
            box.setPadding(new Insets(15));
            box.setStyle("-fx-background-color: black;");

            apptStage.setScene(new Scene(box, 650, 450));
            apptStage.show();
        });

        // Opens the appointment booking interface
        bookAppointment.setOnAction(e -> {
            AppointmentManager manager =
                    new AppointmentManager();
            manager.open(stage, patientId);
        });

        // Displays billing records associated with the patient
        viewBilling.setOnAction(e -> {

            StringBuilder sb = new StringBuilder();

            try (Connection conn = sqlconnector.connect()) {

                String sql =
                        "SELECT * FROM billing WHERE patient_id=?";
                PreparedStatement ps =
                        conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();

                // Format billing records into readable text
                while (rs.next()) {
                    sb.append("Amount: ₦")
                            .append(rs.getDouble("amount"))
                            .append("\nDescription: ")
                            .append(rs.getString("description"))
                            .append("\nDate: ")
                            .append(rs.getTimestamp("date_created"))
                            .append("\n-----------------------------\n");
                }

                // Handle case where no billing records exist
                if (sb.length() == 0) {
                    sb.append("No billing records found.");
                }

            } catch (Exception ex) {
                sb.append("Error: ").append(ex.getMessage());
            }

            showTextWindow("Billing Records", sb.toString());
        });

        // Returns the user to the main menu
        back.setOnAction(e -> new MainMenu().start(stage));

        // Logs the user out and redirects to the main menu
        logout.setOnAction(e -> {
            MainMenu menu = new MainMenu();
            menu.start(stage);
        });

        // Main dashboard layout container
        VBox root = new VBox(
                12,
                title,
                info,
                viewAppointments,
                bookAppointment,
                viewBilling,
                feedback,
                back,
                logout
        );

        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(root, 600, 500));
        stage.setTitle("Patient Dashboard");
        stage.show();
    }

    /**
     * Displays a read-only popup window containing text content.
     *
     * @param title   window title
     * @param content text to display
     */
    private void showTextWindow(String title, String content) {

        Stage popup = new Stage();

        TextArea area = new TextArea(content);
        area.setEditable(false);

        Label heading = new Label(title);
        heading.setTextFill(Color.GOLD);
        heading.setFont(
                Font.font("Arial", FontWeight.BOLD, 16));

        VBox layout = new VBox(10, heading, area);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: black;");

        popup.setScene(new Scene(layout, 600, 400));
        popup.setTitle(title);
        popup.show();
    }

    /**
     * Model class representing a single appointment record
     * used by the TableView.
     */
    public static class AppointmentRow {

        // Unique appointment identifier
        private final String appointmentId;

        // Associated practitioner identifier
        private final String practitionerId;

        // Appointment date and time
        private final String dateTime;

        // Current appointment status
        private final String status;

        // Constructs a table row object
        public AppointmentRow(
                String appointmentId,
                String practitionerId,
                String dateTime,
                String status) {

            this.appointmentId = appointmentId;
            this.practitionerId = practitionerId;
            this.dateTime = dateTime;
            this.status = status;
        }

        public String getAppointmentId() {
            return appointmentId;
        }

        public String getPractitionerId() {
            return practitionerId;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getStatus() {
            return status;
        }
    }
}
